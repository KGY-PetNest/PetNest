package ru.hukm.petnest.plugins.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

suspend fun <T> dbQuery(block: JdbcTransaction.() -> T): T = withContext(Dispatchers.IO) {
    transaction { block() }
}
