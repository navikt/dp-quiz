import ch.qos.logback.core.util.OptionHelper.getEnv
import ch.qos.logback.core.util.OptionHelper.getSystemProperty
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

// Understands how to create a data source from environment variables
internal object DataSourceBuilder {
    const val DB_URL_KEY = "DB_URL_KEY"
    const val DB_USERNAME_KEY = "DB_USERNAME_KEY"
    const val DB_PASSWORD_KEY = "DB_PASSWORD_KEY"

    private val jdbcUrl by lazy {
        val jdbcUrl: String? = getEnv("DB_URL_KEY") ?: getSystemProperty("DB_URL_KEY")
        requireNotNull(jdbcUrl, { "Fant ingen jdbc url definert for nøkkel: $DB_URL_KEY" })
    }

    private val username by lazy {
        val jdbcUrl: String? = getEnv(DB_USERNAME_KEY) ?: getSystemProperty(DB_USERNAME_KEY)
        requireNotNull(jdbcUrl, { "Fant ingen jdbc url definert for nøkkel: $DB_USERNAME_KEY" })
    }

    private val password by lazy {
        val jdbcUrl: String? = getEnv(DB_PASSWORD_KEY) ?: getSystemProperty(DB_PASSWORD_KEY)
        requireNotNull(jdbcUrl, { "Fant ingen jdbc url definert for nøkkel: $DB_PASSWORD_KEY" })
    }

    val dataSource by lazy {
        HikariDataSource().apply {
            jdbcUrl = DataSourceBuilder.jdbcUrl
            username = DataSourceBuilder.username
            password = DataSourceBuilder.password
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
