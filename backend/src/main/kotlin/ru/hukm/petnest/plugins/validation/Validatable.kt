package ru.hukm.petnest.plugins.validation

interface Validatable {
    fun validate(): List<String>
}

class ValidationException(val errors: List<String>) : RuntimeException(errors.joinToString())

fun Validatable.requireValid() {
    val errors = validate()
    if (errors.isNotEmpty()) throw ValidationException(errors)
}
