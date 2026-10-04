package ru.hukm.petnest.plugins.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private val tables = arrayOf<Table>()

fun Application.configureDatabase() {
    val config = environment.config
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.property("database.url").getString()
        username = config.property("database.user").getString()
        password = System.getenv("POSTGRES_PASSWORD") ?: error("POSTGRES_PASSWORD is not set")
        maximumPoolSize = 10
    })

    monitor.subscribe(ApplicationStopped) {
        dataSource.close()
    }

    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(*tables)
    }
}
