package com.example.pet.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pet.R
import com.example.pet.data.AcceptedPet
import com.example.pet.data.HomeConditionType
import com.example.pet.data.PetTrait
import com.example.pet.data.PetTraitGroup
import com.example.pet.data.RequestStatus

@get:StringRes
val PetTrait.label: Int
    get() = when (this) {
        PetTrait.Medication -> R.string.common_trait_medication
        PetTrait.SpecialDiet -> R.string.common_trait_special_diet
        PetTrait.Allergy -> R.string.common_trait_allergy
        PetTrait.Senior -> R.string.common_trait_senior
        PetTrait.NotNeutered -> R.string.common_trait_not_neutered
        PetTrait.NeedsWalks -> R.string.common_trait_needs_walks
        PetTrait.NotHouseTrained -> R.string.common_trait_not_house_trained
        PetTrait.CantBeAlone -> R.string.common_trait_cant_be_alone
        PetTrait.Calm -> R.string.common_trait_calm
        PetTrait.Active -> R.string.common_trait_active
        PetTrait.FearsNoise -> R.string.common_trait_fears_noise
        PetTrait.MayBite -> R.string.common_trait_may_bite
        PetTrait.NotFriendlyWithAnimals -> R.string.common_trait_not_friendly
        PetTrait.NotGoodWithKids -> R.string.common_trait_not_good_with_kids
    }

@get:StringRes
val PetTraitGroup.label: Int
    get() = when (this) {
        PetTraitGroup.Health -> R.string.common_trait_group_health
        PetTraitGroup.Care -> R.string.common_trait_group_care
        PetTraitGroup.Behavior -> R.string.common_trait_group_behavior
    }

@get:StringRes
val HomeConditionType.label: Int
    get() = when (this) {
        HomeConditionType.Apartment -> R.string.common_home_apartment
        HomeConditionType.House -> R.string.common_home_house
        HomeConditionType.Yard -> R.string.common_home_yard
        HomeConditionType.NoOtherPets -> R.string.common_home_no_pets
        HomeConditionType.HasOtherPets -> R.string.common_home_has_pets
        HomeConditionType.SomeoneHome -> R.string.common_home_someone_home
        HomeConditionType.NoKids -> R.string.common_home_no_kids
    }

val HomeConditionType.icon: ImageVector
    get() = when (this) {
        HomeConditionType.Apartment -> Icons.Default.Apartment
        HomeConditionType.House -> Icons.Default.Home
        HomeConditionType.Yard -> Icons.Default.Grass
        HomeConditionType.NoOtherPets, HomeConditionType.HasOtherPets -> Icons.Default.Pets
        HomeConditionType.SomeoneHome -> Icons.Default.Person
        HomeConditionType.NoKids -> Icons.Default.ChildCare
    }

@get:StringRes
val AcceptedPet.label: Int
    get() = when (this) {
        AcceptedPet.Cats -> R.string.common_accept_cats
        AcceptedPet.SmallDogs -> R.string.common_accept_small_dogs
        AcceptedPet.LargeDogs -> R.string.common_accept_large_dogs
        AcceptedPet.Rodents -> R.string.common_accept_rodents
        AcceptedPet.Birds -> R.string.common_accept_birds
        AcceptedPet.Other -> R.string.common_accept_other
    }

@get:StringRes
val RequestStatus.label: Int
    get() = when (this) {
        RequestStatus.Open -> R.string.common_status_open
        RequestStatus.VolunteerChosen -> R.string.common_status_chosen
        RequestStatus.Completed -> R.string.common_status_completed
    }