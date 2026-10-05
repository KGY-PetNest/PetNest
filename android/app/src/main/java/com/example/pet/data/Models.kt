package com.example.pet.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class UserRole { Owner, Volunteer }

enum class PetKind { Cat, Dog, Other }

enum class PetTrait {
    Pills,
    SpecialCare,
    FearsNoise,
    Calm,
    Active,
    NeedsWalks,
    NotFriendlyWithAnimals,
    SpecialDiet
}

data class PetRequest(
    val id: String,
    val title: String,
    val petInfo: String,
    val kind: PetKind,
    val start: LocalDate,
    val end: LocalDate,
    val district: String,
    val traits: List<PetTrait> = emptyList(),
    val responsesCount: Int = 0
) {
    val days: Int
        get() = ChronoUnit.DAYS.between(start, end).toInt().coerceAtLeast(1)

    val dates: String
        get() = "${start.format(DayMonthFormat)} – ${end.format(DayMonthFormat)}"
}

val DayMonthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru"))

enum class HomeConditionType { Apartment, House, NoOtherPets, HasOtherPets, SomeoneHome }

data class HomeCondition(val type: HomeConditionType, val text: String)

data class Volunteer(
    val id: String,
    val name: String,
    val rating: Double,
    val reviewsCount: Int,
    val experience: String,
    val homeShort: String,
    val about: String,
    val homeConditions: List<HomeCondition>,
    val acceptedPets: List<String>
)