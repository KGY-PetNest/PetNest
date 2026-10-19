package ru.hukm.petnest.modules.requests

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.date
import kotlinx.datetime.LocalDate
import ru.hukm.petnest.modules.pets.PetsTable
import ru.hukm.petnest.modules.users.UsersTable

@Serializable
enum class CareFormat {
    AT_VOLUNTEER, AT_OWNER
}

@Serializable
enum class RequestStatus {
    OPEN, VOLUNTEER_CHOSEN, COMPLETED
}

object RequestsTable : LongIdTable("requests") {
    val ownerId = reference("owner_id", UsersTable)
    val petId = reference("pet_id", PetsTable)
    val start = date("start")
    val end = date("end")
    val district = varchar("district", 100).default("")
    val address = varchar("address", 255)
    val addressDetails = varchar("address_details", 100).default("")
    val comment = varchar("comment", 1000)
    val lat = double("lat").nullable()
    val lon = double("lon").nullable()
    val format = enumerationByName("format", 20, CareFormat::class)
    val visitsPerDay = integer("visits_per_day").default(2)
    val status = enumerationByName("status", 20, RequestStatus::class).default(RequestStatus.OPEN)
}

class Request(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Request>(RequestsTable)

    var ownerId by RequestsTable.ownerId
    var petId by RequestsTable.petId
    var start by RequestsTable.start
    var end by RequestsTable.end
    var district by RequestsTable.district
    var address by RequestsTable.address
    var addressDetails by RequestsTable.addressDetails
    var comment by RequestsTable.comment
    var lat by RequestsTable.lat
    var lon by RequestsTable.lon
    var format by RequestsTable.format
    var visitsPerDay by RequestsTable.visitsPerDay
    var status by RequestsTable.status
}
