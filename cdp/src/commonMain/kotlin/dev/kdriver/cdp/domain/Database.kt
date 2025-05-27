package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.database: Database
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Database(this))

public class Database(
    private val cdp: CDP,
) : Domain {
    public val addDatabase: Flow<AddDatabaseParameter> = cdp
        .events
        .filter {
            it.method == "Database.addDatabase"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Disables database tracking, prevents database events from being sent to the client.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Database.disable", parameter)
    }

    /**
     * Enables database tracking, database events will now be delivered to the client.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("Database.enable", parameter)
    }

    public suspend fun executeSQL(args: ExecuteSQLParameter): ExecuteSQLReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Database.executeSQL", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun executeSQL(databaseId: String, query: String): ExecuteSQLReturn {
        val parameter = ExecuteSQLParameter(databaseId = databaseId, query = query)
        return executeSQL(parameter)
    }

    public suspend fun getDatabaseTableNames(args: GetDatabaseTableNamesParameter): GetDatabaseTableNamesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Database.getDatabaseTableNames", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun getDatabaseTableNames(databaseId: String): GetDatabaseTableNamesReturn {
        val parameter = GetDatabaseTableNamesParameter(databaseId = databaseId)
        return getDatabaseTableNames(parameter)
    }

    /**
     * Database object.
     */
    @Serializable
    public data class Database(
        /**
         * Database ID.
         */
        public val id: String,
        /**
         * Database domain.
         */
        public val domain: String,
        /**
         * Database name.
         */
        public val name: String,
        /**
         * Database version.
         */
        public val version: String,
    )

    /**
     * Database error.
     */
    @Serializable
    public data class Error(
        /**
         * Error message.
         */
        public val message: String,
        /**
         * Error code.
         */
        public val code: Int,
    )

    @Serializable
    public data class AddDatabaseParameter(
        public val database: Database,
    )

    @Serializable
    public data class ExecuteSQLParameter(
        public val databaseId: String,
        public val query: String,
    )

    @Serializable
    public data class ExecuteSQLReturn(
        public val columnNames: List<String>?,
        public val values: List<JsonElement>?,
        public val sqlError: Error?,
    )

    @Serializable
    public data class GetDatabaseTableNamesParameter(
        public val databaseId: String,
    )

    @Serializable
    public data class GetDatabaseTableNamesReturn(
        public val tableNames: List<String>,
    )
}
