package com.example.pet.data

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

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

enum class MyResponseStatus { Pending, Chosen, NotChosen, Expired, Completed }

val MyResponseStatus?.canChat: Boolean
    get() = this == MyResponseStatus.Pending || this == MyResponseStatus.Chosen || this == MyResponseStatus.Completed

data class GeoPoint(val lat: Double, val lon: Double) : Serializable

data class SavedLocation(val point: GeoPoint, val label: String, val isAuto: Boolean = false)

enum class HomeConditionGroup { Housing, Household, Care }

enum class HomeConditionType(val group: HomeConditionGroup) {
    Apartment(HomeConditionGroup.Housing),
    House(HomeConditionGroup.Housing),
    Yard(HomeConditionGroup.Housing),
    WindowNets(HomeConditionGroup.Housing),
    NoOtherPets(HomeConditionGroup.Household),
    HasOtherPets(HomeConditionGroup.Household),
    NoKids(HomeConditionGroup.Household),
    HasKids(HomeConditionGroup.Household),
    SomeoneHome(HomeConditionGroup.Care),
    CanWalk(HomeConditionGroup.Care),
    CanGiveMedication(HomeConditionGroup.Care)
}

val HomeConditionType.opposite: HomeConditionType?
    get() = when (this) {
        HomeConditionType.Apartment -> HomeConditionType.House
        HomeConditionType.House -> HomeConditionType.Apartment
        HomeConditionType.NoOtherPets -> HomeConditionType.HasOtherPets
        HomeConditionType.HasOtherPets -> HomeConditionType.NoOtherPets
        HomeConditionType.NoKids -> HomeConditionType.HasKids
        HomeConditionType.HasKids -> HomeConditionType.NoKids
        else -> null
    }

fun Set<HomeConditionType>.toggled(item: HomeConditionType): Set<HomeConditionType> =
    if (item in this) this - item else this - setOfNotNull(item.opposite) + item

enum class AcceptedPet { Cats, SmallDogs, LargeDogs, Rodents, Birds, Other }

data class UserProfile(
    val name: String,
    val phone: String,
    val email: String,
    val avatarUri: String? = null
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
    val addressDetails: String = "",
    val traits: List<PetTrait> = emptyList(),
    val features: String = "",
    val petPhotoUri: String? = null,
    val location: GeoPoint? = null,
    val status: RequestStatus = RequestStatus.Open,
    val chosenVolunteerId: String? = null,
    val ownerName: String = "",
    val ownerPhone: String = ""
) {
    val days: Int
        get() = ChronoUnit.DAYS.between(start, end).toInt().coerceAtLeast(1)

    val dates: String
        get() = "${start.format(DayMonthFormat)} – ${end.format(DayMonthFormat)}"

    val place: String
        get() = district.ifBlank { address }

    val publicPlace: String
        get() = district.ifBlank { approximateAddress(address) }

    val isExpired: Boolean
        get() = status == RequestStatus.Open && start.isBefore(LocalDate.now())

    val acceptsResponses: Boolean
        get() = status == RequestStatus.Open && chosenVolunteerId == null && !isExpired

    fun statusFor(volunteerId: String, respondedIds: Set<String>): MyResponseStatus? = when {
        chosenVolunteerId == volunteerId && status == RequestStatus.Completed -> MyResponseStatus.Completed
        chosenVolunteerId == volunteerId -> MyResponseStatus.Chosen
        id !in respondedIds -> null
        chosenVolunteerId != null -> MyResponseStatus.NotChosen
        isExpired -> MyResponseStatus.Expired
        else -> MyResponseStatus.Pending
    }
}

data class Volunteer(
    val id: String,
    val name: String,
    val experience: String,
    val about: String,
    val homeConditions: List<HomeConditionType>,
    val acceptedPets: List<AcceptedPet>,
    val avatarUri: String? = null,
    val phone: String = ""
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

enum class MessageStatus { Sending, Sent, Read, Failed }

enum class AttachmentKind { Image, Pdf }

data class ChatAttachment(
    val kind: AttachmentKind,
    val uri: String,
    val name: String,
    val sizeBytes: Long,
    val width: Int = 0,
    val height: Int = 0
)

enum class ChatEvent { VolunteerChosen, ChoiceCancelled, Completed, VolunteerWithdrew }

enum class ReportReason { Spam, Rude, Fraud, Inappropriate, Other }

data class ChatMessage(
    val id: String,
    val chatId: String,
    val senderRole: UserRole,
    val text: String,
    val sentAt: LocalDateTime,
    val status: MessageStatus = MessageStatus.Sent,
    val attachment: ChatAttachment? = null,
    val event: ChatEvent? = null
)

data class IncomingMessage(
    val role: UserRole,
    val chat: Chat,
    val message: ChatMessage
)

data class Chat(
    val id: String,
    val requestId: String,
    val volunteerId: String,
    val companionName: String,
    val requestTitle: String,
    val requestDates: String,
    val companionAvatarUri: String? = null,
    val petPhotoUri: String? = null,
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
    val blocked: Boolean = false
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

fun shortPersonName(fullName: String): String {
    val words = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return if (words.size >= 2) "${words[1]} ${words[0].first()}." else fullName.trim()
}

fun List<Review>.averageRating(): Double =
    if (isEmpty()) 0.0 else sumOf { it.rating }.toDouble() / size

fun approximateAddress(address: String): String {
    val parts = address.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        .filterNot { it.equals("Россия", ignoreCase = true) }
    val firstWithNumber = parts.indexOfFirst { part -> part.any(Char::isDigit) }
    val kept = if (firstWithNumber > 0) parts.take(firstWithNumber) else parts.take(1)
    return kept.joinToString(", ").ifBlank { address.trim() }
}

fun GeoPoint.distanceKmTo(other: GeoPoint): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(other.lat - lat)
    val dLon = Math.toRadians(other.lon - lon)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(other.lat)) * sin(dLon / 2).pow(2)
    return 2 * earthRadiusKm * asin(sqrt(a))
}

fun formatDistanceKm(km: Double): String = when {
    km < 1.0 -> "< 1 км"
    km < 10.0 -> String.format(Locale.forLanguageTag("ru"), "%.1f км", km)
    else -> "${km.roundToInt()} км"
}

fun phoneForDial(phone: String): String = "+7" + phone.filter(Char::isDigit).takeLast(10)
