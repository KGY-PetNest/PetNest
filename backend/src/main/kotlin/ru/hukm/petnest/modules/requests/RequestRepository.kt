package ru.hukm.petnest.modules.requests

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.dao.DaoEntityID
import kotlinx.datetime.LocalDate
import ru.hukm.petnest.modules.pets.PetsTable
import ru.hukm.petnest.modules.users.UsersTable
import ru.hukm.petnest.plugins.database.dbQuery

object RequestRepository {
    suspend fun create(
        ownerId: Long,
        petId: Long,
        start: LocalDate,
        end: LocalDate,
        district: String,
        address: String,
        addressDetails: String,
        comment: String,
        lat: Double?,
        lon: Double?,
        format: CareFormat,
        visitsPerDay: Int,
    ): Request = dbQuery {
        Request.new {
            this.ownerId = DaoEntityID(ownerId, UsersTable)
            this.petId = DaoEntityID(petId, PetsTable)
            this.start = start
            this.end = end
            this.district = district
            this.address = address
            this.addressDetails = addressDetails
            this.comment = comment
            this.lat = lat
            this.lon = lon
            this.format = format
            this.visitsPerDay = visitsPerDay
        }
    }

    suspend fun findById(id: Long): Request? = dbQuery { Request.findById(id) }

    suspend fun findByOwner(ownerId: Long): List<Request> = dbQuery {
        Request.find { RequestsTable.ownerId eq ownerId }.toList()
    }

    suspend fun update(
        request: Request,
        petId: Long,
        start: LocalDate,
        end: LocalDate,
        district: String,
        address: String,
        addressDetails: String,
        comment: String,
        lat: Double?,
        lon: Double?,
        format: CareFormat,
        visitsPerDay: Int,
    ) = dbQuery {
        request.petId = DaoEntityID(petId, PetsTable)
        request.start = start
        request.end = end
        request.district = district
        request.address = address
        request.addressDetails = addressDetails
        request.comment = comment
        request.lat = lat
        request.lon = lon
        request.format = format
        request.visitsPerDay = visitsPerDay
    }

    suspend fun delete(request: Request) = dbQuery {
        request.delete()
    }

    suspend fun hasDateOverlap(ownerId: Long, start: LocalDate, end: LocalDate, excludeId: Long? = null): Boolean = dbQuery {
        Request.find {
            (RequestsTable.ownerId eq ownerId) and
                (RequestsTable.status eq RequestStatus.OPEN) and
                (RequestsTable.start lessEq end) and
                (RequestsTable.end greaterEq start)
        }.any { it.id.value != excludeId }
    }
}
