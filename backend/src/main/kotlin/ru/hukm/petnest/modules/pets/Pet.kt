package ru.hukm.petnest.modules.pets

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import ru.hukm.petnest.modules.users.UsersTable

@Serializable
enum class PetKind {
    CAT, DOG, RODENT, BIRD, OTHER
}

@Serializable
enum class PetTrait {
    MEDICATION, SPECIAL_DIET, ALLERGY, SENIOR, NOT_NEUTERED,
    NEEDS_WALKS, NOT_HOUSE_TRAINED, CANT_BE_ALONE,
    CALM, ACTIVE, FEARS_NOISE, MAY_BITE, NOT_FRIENDLY_WITH_ANIMALS, NOT_GOOD_WITH_KIDS
}

object PetsTable : LongIdTable("pets") {
    val ownerId = reference("owner_id", UsersTable)
    val name = varchar("name", 60)
    val animal = varchar("animal", 40)
    val kind = enumerationByName("kind", 20, PetKind::class)
    val age = integer("age")
    val traitsOther = varchar("traits_other", 200).default("")
    val features = varchar("features", 1000)
    val photo = binary("photo").nullable()
}

object PetTraitsTable : Table("pet_traits") {
    val pet = reference("pet_id", PetsTable)
    val trait = enumerationByName("trait", 40, PetTrait::class)

    override val primaryKey = PrimaryKey(pet, trait)
}

class Pet(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Pet>(PetsTable)

    var ownerId by PetsTable.ownerId
    var name by PetsTable.name
    var animal by PetsTable.animal
    var kind by PetsTable.kind
    var age by PetsTable.age
    var traitsOther by PetsTable.traitsOther
    var features by PetsTable.features
    var photo by PetsTable.photo
}
