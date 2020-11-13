import ch.qos.logback.core.util.OptionHelper.getEnv
import ch.qos.logback.core.util.OptionHelper.getSystemProperty
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

// Understands how to create a data source from environment variables
internal object PostgresDataSourceBuilder {
    const val DB_URL_KEY = "DB_URL"
    const val DB_USERNAME_KEY = "DB_USERNAME"
    const val DB_PASSWORD_KEY = "DB_PASSWORD"
    const val DB_DATABASE_KEY = "DB_DATABASE"
    const val DB_HOST_KEY = "DB_HOST"
    const val DB_PORT_KEY = "DB_PORT"

    private val jdbcUrl by lazy {
        val host: String? = getEnv(DB_HOST_KEY) ?: getSystemProperty(DB_HOST_KEY)
        val port: String? = getEnv(DB_PORT_KEY) ?: getSystemProperty(DB_PORT_KEY)
        val databaseNavn: String? = getEnv(DB_DATABASE_KEY) ?: getSystemProperty(DB_DATABASE_KEY)
        "jdbc:postgresql://$host:$port/$databaseNavn"
    }

    private val username by lazy {
        val jdbcUrl: String? = getEnv(DB_USERNAME_KEY) ?: getSystemProperty(DB_USERNAME_KEY)
        requireNotNull(jdbcUrl, { "Fant ingen db.username definert for nøkkel: $DB_USERNAME_KEY" })
    }

    private val password by lazy {
        val jdbcUrl: String? = getEnv(DB_PASSWORD_KEY) ?: getSystemProperty(DB_PASSWORD_KEY)
        requireNotNull(jdbcUrl, { "Fant ingen db.passord definert for nøkkel: $DB_PASSWORD_KEY" })
    }

    val dataSource by lazy {
        HikariDataSource().apply {
            jdbcUrl = PostgresDataSourceBuilder.jdbcUrl
            username = PostgresDataSourceBuilder.username
            password = PostgresDataSourceBuilder.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }
    }

    fun clean() = Flyway.configure().dataSource(dataSource).load().clean()

    internal fun runMigration(initSql: String? = null) =
        Flyway.configure()
            .dataSource(dataSource)
            .initSql(initSql)
            .load()
            .migrate()
}
