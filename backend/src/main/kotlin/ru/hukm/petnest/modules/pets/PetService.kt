package ru.hukm.petnest.modules.pets

import kotlinx.serialization.Serializable
import ru.hukm.petnest.plugins.statuspages.NotFoundException
import ru.hukm.petnest.plugins.validation.Validatable
import ru.hukm.petnest.plugins.validation.ValidationException
import ru.hukm.petnest.plugins.validation.requireValid
import java.util.Base64

@Serializable
data class PetCreateRequest(
    val name: String,
    val animal: String,
    val kind: PetKind,
    val age: Int,
    val noTraits: Boolean = false,
    val traits: List<PetTrait> = emptyList(),
    val traitsOther: String = "",
    val features: String,
    val photo: String? = null,
) : Validatable {
    init { requireValid() }

    override fun validate() = buildList {
        if (name.isBlank() || name.length > NAME_MAX) add("Укажите имя питомца (до $NAME_MAX символов)")
        if (animal.isBlank() || animal.length > ANIMAL_MAX) add("Укажите вид животного (до $ANIMAL_MAX символов)")
        if (age < 1 || age > 99) add("Возраст должен быть от 1 до 99 лет")
        if (!noTraits && traits.isEmpty() && traitsOther.isBlank()) add("Выберите особенности или отметьте «Без особенностей»")
        if (traitsOther.length > TRAITS_OTHER_MAX) add("Описание особенностей — до $TRAITS_OTHER_MAX символов")
        if (features.length < FEATURES_MIN || features.length > FEATURES_MAX) add("Описание особенностей ухода — от $FEATURES_MIN до $FEATURES_MAX символов")
    }

    companion object {
        private const val NAME_MAX = 60
        private const val ANIMAL_MAX = 40
        private const val TRAITS_OTHER_MAX = 200
        private const val FEATURES_MIN = 10
        private const val FEATURES_MAX = 1000
    }
}

@Serializable
data class PetResponse(
    val id: Long,
    val name: String,
    val animal: String,
    val kind: PetKind,
    val age: Int,
    val traits: List<PetTrait>,
    val traitsOther: String,
    val features: String,
    val hasPhoto: Boolean,
)

@Serializable
data class PetDetailResponse(
    val id: Long,
    val name: String,
    val animal: String,
    val kind: PetKind,
    val age: Int,
    val traits: List<PetTrait>,
    val traitsOther: String,
    val features: String,
    val photo: String?,
    val photoContentType: String?,
)

object PetService {
    private const val MAX_PHOTO_BYTES = 10 * 1024 * 1024

    suspend fun create(ownerId: Long, dto: PetCreateRequest): PetDetailResponse {
        val photo = decodePhoto(dto)
        val pet = PetRepository.create(
            ownerId = ownerId,
            name = dto.name.trim(),
            animal = dto.animal.trim(),
            kind = dto.kind,
            age = dto.age,
            traitsOther = if (dto.noTraits) "" else dto.traitsOther.trim(),
            features = dto.features.trim(),
            traits = if (dto.noTraits) emptyList() else dto.traits,
        )
        if (photo != null) PetRepository.updatePhoto(pet, photo)
        return toDetail(pet)
    }

    suspend fun list(ownerId: Long): List<PetResponse> {
        return PetRepository.findByOwner(ownerId).map { toResponse(it) }
    }

    suspend fun get(ownerId: Long, petId: Long): PetDetailResponse {
        return toDetail(requireOwnedPet(ownerId, petId))
    }

    suspend fun update(ownerId: Long, petId: Long, dto: PetCreateRequest): PetDetailResponse {
        val photo = decodePhoto(dto)
        val pet = requireOwnedPet(ownerId, petId)
        PetRepository.update(
            pet = pet,
            name = dto.name.trim(),
            animal = dto.animal.trim(),
            kind = dto.kind,
            age = dto.age,
            traitsOther = if (dto.noTraits) "" else dto.traitsOther.trim(),
            features = dto.features.trim(),
            traits = if (dto.noTraits) emptyList() else dto.traits,
        )
        if (photo != null) PetRepository.updatePhoto(pet, photo)
        return toDetail(pet)
    }

    suspend fun delete(ownerId: Long, petId: Long) {
        PetRepository.delete(requireOwnedPet(ownerId, petId))
    }

    private fun decodePhoto(dto: PetCreateRequest): ByteArray? {
        val raw = dto.photo ?: return null
        val bytes = try {
            Base64.getDecoder().decode(raw)
        } catch (e: IllegalArgumentException) {
            throw ValidationException(listOf("Некорректное фото"))
        }
        if (bytes.isEmpty()) throw ValidationException(listOf("Файл фото пустой"))
        if (bytes.size > MAX_PHOTO_BYTES) throw ValidationException(listOf("Фото слишком большое (максимум 10 МБ)"))
        if (detectPhotoType(bytes) == null) throw ValidationException(listOf("Фото должно быть в формате JPEG, PNG или WebP"))
        return bytes
    }

    private fun detectPhotoType(bytes: ByteArray): String? = when {
        bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() -> "image/jpeg"
        bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 'P'.toByte() && bytes[2] == 'N'.toByte() && bytes[3] == 'G'.toByte() -> "image/png"
        bytes.size >= 12 && bytes[0] == 'R'.toByte() && bytes[1] == 'I'.toByte() && bytes[2] == 'F'.toByte() && bytes[3] == 'F'.toByte()
            && bytes[8] == 'W'.toByte() && bytes[9] == 'E'.toByte() && bytes[10] == 'B'.toByte() && bytes[11] == 'P'.toByte() -> "image/webp"
        else -> null
    }

    private suspend fun requireOwnedPet(ownerId: Long, petId: Long): Pet {
        val pet = PetRepository.findById(petId) ?: throw NotFoundException("Питомец не найден")
        if (pet.ownerId.value != ownerId) throw NotFoundException("Питомец не найден")
        return pet
    }

    private suspend fun toResponse(pet: Pet) = PetResponse(
        id = pet.id.value,
        name = pet.name,
        animal = pet.animal,
        kind = pet.kind,
        age = pet.age,
        traits = PetRepository.traitsOf(pet.id.value),
        traitsOther = pet.traitsOther,
        features = pet.features,
        hasPhoto = pet.photo != null,
    )

    private suspend fun toDetail(pet: Pet) = PetDetailResponse(
        id = pet.id.value,
        name = pet.name,
        animal = pet.animal,
        kind = pet.kind,
        age = pet.age,
        traits = PetRepository.traitsOf(pet.id.value),
        traitsOther = pet.traitsOther,
        features = pet.features,
        photo = pet.photo?.let { Base64.getEncoder().encodeToString(it) },
        photoContentType = pet.photo?.let { detectPhotoType(it) },
    )
}
