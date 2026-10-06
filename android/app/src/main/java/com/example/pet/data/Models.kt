package com.example.pet.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class UserRole { Owner, Volunteer }

enum class ThemeMode { System, Light, Dark }

enum class PetKind { Cat, Dog, Other }

enum class PetTraitGroup { Health, Care, Behavior }

enum class PetTrait(val group: PetTraitGroup) {
    Medication(PetTraitGroup.Health),
    SpecialDiet(PetTraitGroup.Health),
    Allergy(PetTraitGroup.Health),
    Senior(PetTraitGroup.Health),
    NotNeutered(PetTraitGroup.Health),
    NeedsWalks(PetTraitGroup.Care),
    NotHouseTrained(PetTraitGroup.Care),
    CantBeAlone(PetTraitGroup.Care),
    Calm(PetTraitGroup.Behavior),
    Active(PetTraitGroup.Behavior),
    FearsNoise(PetTraitGroup.Behavior),
    MayBite(PetTraitGroup.Behavior),
    NotFriendlyWithAnimals(PetTraitGroup.Behavior),
    NotGoodWithKids(PetTraitGroup.Behavior)
}

enum class RequestStatus { Open, VolunteerChosen, Completed }

enum class HomeConditionType { Apartment, House, Yard, NoOtherPets, HasOtherPets, SomeoneHome, NoKids }

enum class AcceptedPet { Cats, SmallDogs, LargeDogs, Rodents, Birds, Other }

data class UserProfile(
    val name: String,
    val phone: String,
    val email: String
)

data class Pet(
    val id: String,
    val name: String,
    val animal: String,
    val age: Int,
    val traits: List<PetTrait>,
    val features: String,
    val photoUri: String? = null
) {
    val kind: PetKind
        get() = kindFromAnimal(animal)

    val info: String
        get() = "$animal, ${yearsText(age)}"
}

data class PetRequest(
    val id: String,
    val petId: String,
    val title: String,
    val petInfo: String,
    val kind: PetKind,
    val start: LocalDate,
    val end: LocalDate,
    val district: String,
    val address: String,
    val comment: String,
    val traits: List<PetTrait> = emptyList(),
    val features: String = "",
    val status: RequestStatus = RequestStatus.Open,
    val chosenVolunteerId: String? = null,
    val ownerName: String = ""
) {
    val days: Int
        get() = ChronoUnit.DAYS.between(start, end).toInt().coerceAtLeast(1)

    val dates: String
        get() = "${start.format(DayMonthFormat)} – ${end.format(DayMonthFormat)}"

    val place: String
        get() = district.ifBlank { address }
}

data class Volunteer(
    val id: String,
    val name: String,
    val experience: String,
    val about: String,
    val homeConditions: List<HomeConditionType>,
    val acceptedPets: List<AcceptedPet>
)

data class Review(
    val id: String,
    val volunteerId: String,
    val requestId: String?,
    val authorName: String,
    val rating: Int,
    val text: String,
    val date: LocalDate
)

val DayMonthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru"))

val DayMonthYearFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))

fun kindFromAnimal(animal: String): PetKind {
    val text = animal.lowercase(Locale.forLanguageTag("ru"))
    return when {
        listOf("кот", "кош", "cat").any { it in text } -> PetKind.Cat
        listOf("пёс", "пес", "собак", "щен", "dog").any { it in text } -> PetKind.Dog
        else -> PetKind.Other
    }
}

fun yearsText(years: Int): String {
    val mod10 = years % 10
    val mod100 = years % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "год"
        mod10 in 2..4 && mod100 !in 12..14 -> "года"
        else -> "лет"
    }
    return "$years $word"
}

fun List<Review>.averageRating(): Double =
    if (isEmpty()) 0.0 else sumOf { it.rating }.toDouble() / size