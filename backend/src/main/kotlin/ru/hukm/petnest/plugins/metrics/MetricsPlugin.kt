package ru.hukm.petnest.plugins.metrics

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

fun Application.configureMetrics() {
    val config = environment.config
    val user = config.property("metrics.user").getString()
    val password = System.getenv("METRICS_PASSWORD") ?: error("METRICS_PASSWORD is not set")

    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    registry.config().commonTags("application", "petnest")

    install(MicrometerMetrics) {
        this.registry = registry
        distinctNotRegisteredRoutes = false
    }

    authentication {
        basic("metrics") {
            realm = "metrics"
            validate { credentials ->
                if (credentials.name == user && credentials.password == password) UserIdPrincipal(credentials.name) else null
            }
        }
    }

    routing {
        authenticate("metrics") {
            get("/metrics") {
                call.respondText(registry.scrape(), ContentType.parse("text/plain; version=0.0.4; charset=utf-8"))
            }
        }
    }
}
