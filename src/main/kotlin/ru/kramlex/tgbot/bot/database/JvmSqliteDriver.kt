/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.*

internal class JvmSqliteDriver @JvmOverloads internal constructor(
    schema: SqlSchema<QueryResult.Value<Unit>>,
    path: String,
    properties: Properties = Properties(),
    downgradeHandler: JvmSqliteDriver.(databaseVersion: Long) -> Unit = {
        throw IllegalStateException(
            "Downgrading the database isn't supported out of the box! Database is at version $it whereas the schema is at version ${schema.version}"
        )
    }
) : SqlDriver by JdbcSqliteDriver(normalize(path), properties) {

    init {
        val databaseVersion = databaseSchemaVersion()
        when {
            databaseVersion == 0L -> {
                schema.create(this)
                setDatabaseSchemaVersion(schema.version)
            }
            databaseVersion < schema.version -> {
                schema.migrate(this, databaseVersion, schema.version)
                setDatabaseSchemaVersion(schema.version)
            }
            databaseVersion > schema.version -> {
                downgradeHandler(this, databaseVersion)
                setDatabaseSchemaVersion(schema.version)
            }
        }
    }

    /**
     * Return the current database schema version. Useful when migrating,
     * but should always be the newest version. Recreate this driver to migrate.
     *
     * @return the current schema version
     */

    private fun databaseSchemaVersion(): Long = executeQuery(
        identifier = 0,
        sql = "PRAGMA user_version",
        mapper = { cursor -> cursor.getLong(0)?.let { QueryResult.Value(it) }
            ?: throw IllegalStateException("Could not get schema version from db") },
        parameters = 0
    ).value

    private fun setDatabaseSchemaVersion(newVersion: Long) {
        // we don't save this statement, i.e. identifier = null, since it will be used only once anyway
        execute(identifier = null, "PRAGMA user_version = ${newVersion.toInt()}", 0)
    }

    companion object {

        private val normalizationRegex = "^(?:jdbc:)?(?:sqlite:)?(.*)$".toRegex()
        internal fun normalize(path: String): String =
            "jdbc:sqlite:${normalizationRegex.matchEntire(path)?.groupValues?.get(1) 
                ?: throw IllegalArgumentException("Could not normalize database path")}"
    }
}
