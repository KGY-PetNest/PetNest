package com.example.pet.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Window
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pet.R
import com.example.pet.data.AcceptedPet
import com.example.pet.data.ChatEvent
import com.example.pet.data.HomeConditionGroup
import com.example.pet.data.HomeConditionType
import com.example.pet.data.MyResponseStatus
import com.example.pet.data.PetTrait
import com.example.pet.data.PetTraitGroup
import com.example.pet.data.ReportReason
import com.example.pet.data.RequestStatus
import com.example.pet.data.UserRole

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
val HomeConditionGroup.label: Int
    get() = when (this) {
        HomeConditionGroup.Housing -> R.string.common_home_group_housing
        HomeConditionGroup.Household -> R.string.common_home_group_household
        HomeConditionGroup.Care -> R.string.common_home_group_care
    }

@get:StringRes
val HomeConditionType.label: Int
    get() = when (this) {
        HomeConditionType.Apartment -> R.string.common_home_apartment
        HomeConditionType.House -> R.string.common_home_house
        HomeConditionType.Yard -> R.string.common_home_yard
        HomeConditionType.WindowNets -> R.string.common_home_window_nets
        HomeConditionType.NoOtherPets -> R.string.common_home_no_pets
        HomeConditionType.HasOtherPets -> R.string.common_home_has_pets
        HomeConditionType.NoKids -> R.string.common_home_no_kids
        HomeConditionType.HasKids -> R.string.common_home_has_kids
        HomeConditionType.SomeoneHome -> R.string.common_home_someone_home
        HomeConditionType.CanWalk -> R.string.common_home_can_walk
        HomeConditionType.CanGiveMedication -> R.string.common_home_can_medicate
    }

val HomeConditionType.icon: ImageVector
    get() = when (this) {
        HomeConditionType.Apartment -> Icons.Default.Apartment
        HomeConditionType.House -> Icons.Default.Home
        HomeConditionType.Yard -> Icons.Default.Grass
        HomeConditionType.WindowNets -> Icons.Default.Window
        HomeConditionType.NoOtherPets, HomeConditionType.HasOtherPets -> Icons.Default.Pets
        HomeConditionType.NoKids -> Icons.Default.ChildCare
        HomeConditionType.HasKids -> Icons.Default.ChildFriendly
        HomeConditionType.SomeoneHome -> Icons.Default.Person
        HomeConditionType.CanWalk -> Icons.AutoMirrored.Filled.DirectionsWalk
        HomeConditionType.CanGiveMedication -> Icons.Default.Medication
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

@get:StringRes
val MyResponseStatus.label: Int
    get() = when (this) {
        MyResponseStatus.Pending -> R.string.text_12_38
        MyResponseStatus.Chosen -> R.string.text_12_39
        MyResponseStatus.NotChosen -> R.string.text_12_40
        MyResponseStatus.Expired -> R.string.text_12_49
        MyResponseStatus.Completed -> R.string.text_12_41
    }

@get:StringRes
val ReportReason.label: Int
    get() = when (this) {
        ReportReason.Spam -> R.string.text_10_41
        ReportReason.Rude -> R.string.text_10_42
        ReportReason.Fraud -> R.string.text_10_43
        ReportReason.Inappropriate -> R.string.text_10_44
        ReportReason.Other -> R.string.text_10_45
    }

@StringRes
fun ChatEvent.labelFor(role: UserRole): Int = when (this) {
    ChatEvent.VolunteerChosen -> if (role == UserRole.Owner) R.string.text_10_26 else R.string.text_10_27
    ChatEvent.ChoiceCancelled -> if (role == UserRole.Owner) R.string.text_10_28 else R.string.text_10_29
    ChatEvent.Completed -> R.string.text_10_30
    ChatEvent.VolunteerWithdrew -> if (role == UserRole.Owner) R.string.text_10_54 else R.string.text_10_55
}
