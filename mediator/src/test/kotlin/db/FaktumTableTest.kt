package db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import soknad.Prototype

internal class FaktumTableTest {
    @Test
    fun `Bygg faktum tabell`() {
        System.setProperty(DataSourceBuilder.DB_URL_KEY, PostgresTest.PostgresContainer.instance.jdbcUrl)
        System.setProperty(DataSourceBuilder.DB_PASSWORD, PostgresTest.PostgresContainer.instance.password)
        System.setProperty(DataSourceBuilder.DB_USERNAME, PostgresTest.PostgresContainer.instance.username)

        DataSourceBuilder.runMigration()

        Prototype()
    }
}