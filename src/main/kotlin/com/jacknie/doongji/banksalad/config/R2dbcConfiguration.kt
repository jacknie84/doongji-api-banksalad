package com.jacknie.doongji.banksalad.config

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.DatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.data.relational.core.mapping.NamingStrategy

@Configuration
class R2dbcConfiguration: AbstractR2dbcConfiguration() {

    override fun connectionFactory(): ConnectionFactory = ConnectionFactories.get(builder()
            .option(DRIVER, "h2")
            .option(PROTOCOL, "file")
            .option(DATABASE, "~/h2/testdb")
            .option(USER, "sa")
            .build())

    @Bean fun connectionFactoryInitializer(): ConnectionFactoryInitializer {
        val connectionFactoryInitializer = ConnectionFactoryInitializer()
        connectionFactoryInitializer.setConnectionFactory(connectionFactory())
        connectionFactoryInitializer.setDatabaseCleaner(databaseCleaner())
        connectionFactoryInitializer.setDatabasePopulator(databasePopulator())
        connectionFactoryInitializer.setEnabled(true)
        return connectionFactoryInitializer
    }

    @Bean fun databaseCleaner(): DatabasePopulator = ResourceDatabasePopulator(ClassPathResource("/initializer/clean-db.sql"))

    @Bean fun databasePopulator(): DatabasePopulator {
        val databasePopulator = CompositeDatabasePopulator()
        databasePopulator.addPopulators(ResourceDatabasePopulator(ClassPathResource("/initializer/db-schema.sql")))
        return databasePopulator
    }

    @Bean fun namingStrategy(): NamingStrategy {
        return PrefixNamingStrategy("doongji")
    }

}

class PrefixNamingStrategy(private val schemaPrefix: String): NamingStrategy {

    override fun getTableName(type: Class<*>): String {
        return "${schemaPrefix}_${super.getTableName(type)}"
    }
}