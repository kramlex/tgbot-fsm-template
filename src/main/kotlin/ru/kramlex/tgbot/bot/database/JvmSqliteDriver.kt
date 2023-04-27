/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.*

internal class JvmSqliteDriver @JvmOverloads internal constructor(
    schema: SqlDriver.Schema,
    path: String,
    properties: Properties = Properties(),
    downgradeHandler: JvmSqliteDriver.(databaseVersion: Int) -> Unit = {
        throw IllegalStateException(
            "Downgrading the database isn't supported out of the box! Database is at version $it whereas the schema is at version ${schema.version}"
        )
    }
) : SqlDriver by JdbcSqliteDriver(normalize(path), properties) {

    init {
        val databaseVersion = databaseSchemaVersion()
        when {
            databaseVersion == 0 -> {
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

    private fun databaseSchemaVersion(): Int = executeQuery(
        identifier = 0,
        sql = "PRAGMA user_version",
        mapper = { it.getLong(0)?.toInt() ?: throw IllegalStateException("Could not get schema version from db") },
        parameters = 0
    )

    private fun setDatabaseSchemaVersion(newVersion: Int) {
        // we don't save this statement, i.e. identifier = null, since it will be used only once anyway
        execute(identifier = null, "PRAGMA user_version = $newVersion", 0)
    }

    companion object {
        /**
         * A simple string to create a in memory DB. Pass as path parameter in [JvmSqliteDriver] constructor.
         */
        const val IN_MEMORY: String = ""

        private val normalizationRegex = "^(?:jdbc:)?(?:sqlite:)?(.*)$".toRegex()
        internal fun normalize(path: String): String =
            "jdbc:sqlite:${normalizationRegex.matchEntire(path)?.groupValues?.get(1) ?: throw IllegalArgumentException("Could not normalize database path")}"
    }
}
