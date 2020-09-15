package no.nav.dagpenger

import com.natpryce.konfig.Configuration
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.flywaydb.core.Flyway
import javax.sql.DataSource

// Understands how to create a data source from environment variables
internal class DataSourceBuilder(config: Configuration) {
    private val databaseName = config[database.name]

    private val vaultMountPath = config[vault.mountpath]
    private val shouldGetCredentialsFromVault = vaultMountPath != null

    // username and password is only needed when vault is not enabled,
    // since we rotate credentials automatically when vault is enabled
    private val hikariConfig = HikariConfig().apply {
        jdbcUrl = String.format(
            config[database.jdbcUrl],
            config[database.host],
            config[database.port],
            databaseName,
            config[database.username].let { "?user=$it" }
        )

        config[database.username]?.let { this.username = it }
        config[database.password]?.let { this.password = it }

        maximumPoolSize = 3
        minimumIdle = 1
        idleTimeout = 10001
        connectionTimeout = 1000
        maxLifetime = 30001
    }

    fun getDataSource(role: Role = Role.User): DataSource {
        if (!shouldGetCredentialsFromVault) return HikariDataSource(hikariConfig)
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
            hikariConfig,
            vaultMountPath,
            "$databaseName-$role"
        )
    }

    fun migrate() {
        var initSql: String? = null
        if (shouldGetCredentialsFromVault) {
            initSql = "SET ROLE \"$databaseName-${Role.Admin}\""
        }

        runMigration(getDataSource(Role.Admin), initSql)
    }

    private fun runMigration(dataSource: DataSource, initSql: String? = null) =
        Flyway.configure()
            .dataSource(dataSource)
            .initSql(initSql)
            .load()
            .migrate()

    enum class Role {
        Admin, User, ReadOnly;

        override fun toString() = name.toLowerCase()
    }
}
