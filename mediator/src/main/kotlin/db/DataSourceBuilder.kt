import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

// Understands how to create a data source from environment variables
internal object DataSourceBuilder {
    val dataSource = HikariConfig().apply {
    }.dataSource

    fun clean(dataSource: HikariDataSource) = Flyway.configure().dataSource(dataSource).load().clean()

    internal fun runMigration(dataSource: DataSource, initSql: String? = null) =
        Flyway.configure()
            .dataSource(dataSource)
            .initSql(initSql)
            .load()
            .migrate()
}
