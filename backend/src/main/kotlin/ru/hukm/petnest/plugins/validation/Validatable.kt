package ru.hukm.petnest.plugins.validation

interface Validatable {
    fun validate(): List<String>
}
