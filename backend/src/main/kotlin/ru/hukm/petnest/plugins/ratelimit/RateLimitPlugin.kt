package ru.hukm.petnest.plugins.ratelimit

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import kotlin.time.Duration.Companion.seconds

fun Application.configureRateLimit() {
    install(RateLimit) {
        global { 
            rateLimiter(limit = 45, refillPeriod = 60.seconds)
            requestKey { it.request.origin.remoteHost }
        }
    }
}