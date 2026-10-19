package ru.hukm.petnest.modules.requests

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import ru.hukm.petnest.modules.pets.Pet
import ru.hukm.petnest.modules.pets.PetKind
import ru.hukm.petnest.modules.pets.PetRepository
import ru.hukm.petnest.plugins.statuspages.ConflictException
import ru.hukm.petnest.plugins.statuspages.NotFoundException
import ru.hukm.petnest.plugins.validation.Validatable
import ru.hukm.petnest.plugins.validation.requireValid

@Serializable
data class RequestCreateRequest(
    val petId: Long,
    val start: LocalDate,
    val end: LocalDate,
    val district: String = "",
    val address: String,
    val addressDetails: String = "",
    val comment: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val format: CareFormat = CareFormat.AT_VOLUNTEER,
    val visitsPerDay: Int = 2,
) : Validatable {
    init { requireValid() }

    override fun validate() = buildList {
        if (start < LocalDate.parse(java.time.LocalDate.now().toString())) add("Заявка не может начинаться в прошлом")
        if (end < start) add("Дата окончания должна быть не раньше даты начала")
        if (address.isBlank() || address.length > ADDRESS_MAX) add("Укажите адрес (до $ADDRESS_MAX символов)")
        if (district.length > DISTRICT_MAX) add("Район — до $DISTRICT_MAX символов")
        if (addressDetails.length > ADDRESS_DETAILS_MAX) add("Уточнение адреса — до $ADDRESS_DETAILS_MAX символов")
        if (comment.length < COMMENT_MIN || comment.length > COMMENT_MAX) add("Комментарий — от $COMMENT_MIN до $COMMENT_MAX символов")
        if (format == CareFormat.AT_OWNER && (visitsPerDay < 1 || visitsPerDay > 10)) add("Визитов в день — от 1 до 10")
    }

    companion object {
        private const val ADDRESS_MAX = 255
        private const val DISTRICT_MAX = 100
        private const val ADDRESS_DETAILS_MAX = 100
        private const val COMMENT_MIN = 10
        private const val COMMENT_MAX = 1000
    }
}

@Serializable
data class RequestResponse(
    val id: Long,
    val petId: Long,
    val petName: String,
    val petKind: PetKind,
    val start: LocalDate,
    val end: LocalDate,
    val days: Int,
    val district: String,
    val address: String,
    val addressDetails: String,
    val comment: String,
    val lat: Double?,
    val lon: Double?,
    val format: CareFormat,
    val visitsPerDay: Int,
    val status: RequestStatus,
    val hasPhoto: Boolean,
)

object RequestService {
    suspend fun create(ownerId: Long, dto: RequestCreateRequest): RequestResponse {
        val pet = requireOwnedPet(ownerId, dto.petId)
        if (RequestRepository.hasDateOverlap(ownerId, dto.start, dto.end)) {
            throw ConflictException("Даты пересекаются с другой вашей открытой заявкой")
        }
        val request = RequestRepository.create(
            ownerId = ownerId,
            petId = dto.petId,
            start = dto.start,
            end = dto.end,
            district = dto.district.trim(),
            address = dto.address.trim(),
            addressDetails = dto.addressDetails.trim(),
            comment = dto.comment.trim(),
            lat = dto.lat,
            lon = dto.lon,
            format = dto.format,
            visitsPerDay = dto.visitsPerDay,
        )
        return toResponse(request, pet)
    }

    suspend fun list(ownerId: Long): List<RequestResponse> {
        return RequestRepository.findByOwner(ownerId).map { toResponse(it, requirePet(it)) }
    }

    suspend fun get(ownerId: Long, requestId: Long): RequestResponse {
        val request = requireOwnedRequest(ownerId, requestId)
        return toResponse(request, requirePet(request))
    }

    suspend fun update(ownerId: Long, requestId: Long, dto: RequestCreateRequest): RequestResponse {
        val request = requireOwnedRequest(ownerId, requestId)
        val pet = requireOwnedPet(ownerId, dto.petId)
        if (RequestRepository.hasDateOverlap(ownerId, dto.start, dto.end, requestId)) {
            throw ConflictException("Даты пересекаются с другой вашей открытой заявкой")
        }
        RequestRepository.update(
            request = request,
            petId = dto.petId,
            start = dto.start,
            end = dto.end,
            district = dto.district.trim(),
            address = dto.address.trim(),
            addressDetails = dto.addressDetails.trim(),
            comment = dto.comment.trim(),
            lat = dto.lat,
            lon = dto.lon,
            format = dto.format,
            visitsPerDay = dto.visitsPerDay,
        )
        return toResponse(request, pet)
    }

    suspend fun delete(ownerId: Long, requestId: Long) {
        RequestRepository.delete(requireOwnedRequest(ownerId, requestId))
    }

    private suspend fun requireOwnedRequest(ownerId: Long, requestId: Long): Request {
        val request = RequestRepository.findById(requestId) ?: throw NotFoundException("Заявка не найдена")
        if (request.ownerId.value != ownerId) throw NotFoundException("Заявка не найдена")
        return request
    }

    private suspend fun requireOwnedPet(ownerId: Long, petId: Long) =
        PetRepository.findById(petId)?.takeIf { it.ownerId.value == ownerId }
            ?: throw NotFoundException("Питомец не найден")

    private suspend fun requirePet(request: Request) =
        PetRepository.findById(request.petId.value) ?: throw NotFoundException("Питомец не найден")

    private fun toResponse(request: Request, pet: Pet) = RequestResponse(
        id = request.id.value,
        petId = request.petId.value,
        petName = pet.name,
        petKind = pet.kind,
        start = request.start,
        end = request.end,
        days = maxOf(1, request.start.daysUntil(request.end) + 1),
        district = request.district,
        address = request.address,
        addressDetails = request.addressDetails,
        comment = request.comment,
        lat = request.lat,
        lon = request.lon,
        format = request.format,
        visitsPerDay = request.visitsPerDay,
        status = request.status,
        hasPhoto = pet.photo != null,
    )
}
