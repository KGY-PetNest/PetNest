package ru.hukm.petnest.modules.pets

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.DaoEntityID
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import ru.hukm.petnest.modules.users.UsersTable
import ru.hukm.petnest.plugins.database.dbQuery

object PetRepository {
    suspend fun create(
        ownerId: Long,
        name: String,
        animal: String,
        kind: PetKind,
        age: Int,
        traitsOther: String,
        features: String,
        traits: List<PetTrait>,
    ): Pet = dbQuery {
        val pet = Pet.new {
            this.ownerId = DaoEntityID(ownerId, UsersTable)
            this.name = name
            this.animal = animal
            this.kind = kind
            this.age = age
            this.traitsOther = traitsOther
            this.features = features
        }
        saveTraits(pet.id.value, traits)
        pet
    }

    suspend fun findById(id: Long): Pet? = dbQuery { Pet.findById(id) }

    suspend fun findByOwner(ownerId: Long): List<Pet> = dbQuery {
        Pet.find { PetsTable.ownerId eq ownerId }.toList()
    }

    suspend fun update(
        pet: Pet,
        name: String,
        animal: String,
        kind: PetKind,
        age: Int,
        traitsOther: String,
        features: String,
        traits: List<PetTrait>,
    ) = dbQuery {
        pet.name = name
        pet.animal = animal
        pet.kind = kind
        pet.age = age
        pet.traitsOther = traitsOther
        pet.features = features
        saveTraits(pet.id.value, traits)
    }

    suspend fun updatePhoto(pet: Pet, photo: ByteArray) = dbQuery {
        pet.photo = photo
    }

    suspend fun delete(pet: Pet) = dbQuery {
        PetTraitsTable.deleteWhere { PetTraitsTable.pet eq pet.id }
        pet.delete()
    }

    suspend fun traitsOf(petId: Long): List<PetTrait> = dbQuery {
        PetTraitsTable.selectAll().where(PetTraitsTable.pet eq petId)
            .map { it[PetTraitsTable.trait] }
    }

    private fun saveTraits(petId: Long, traits: List<PetTrait>) {
        PetTraitsTable.deleteWhere { PetTraitsTable.pet eq petId }
        traits.distinct().forEach { traitValue ->
            PetTraitsTable.insert { row ->
                row[pet] = petId
                row[trait] = traitValue
            }
        }
    }
}
