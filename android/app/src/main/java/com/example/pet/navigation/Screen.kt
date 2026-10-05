package com.example.pet.navigation

import com.example.pet.data.UserRole

enum class Screen {
    Welcome,
    Registration,
    Login,
    PetProfile,
    CreateRequest,
    EmailConfirm,
    Feed,
    Chat,
    Guide,
    Profile,
    Main,
    MapPicker,
    Responses,
    VolunteerProfile,
}

object Routes {
    const val ROLE_ARG = "role"
    const val REQUEST_ID_ARG = "requestId"
    const val VOLUNTEER_ID_ARG = "volunteerId"

    val MAIN = "${Screen.Main.name}/{$ROLE_ARG}"
    fun main(role: UserRole) = "${Screen.Main.name}/${role.name}"

    val RESPONSES = "${Screen.Responses.name}/{$REQUEST_ID_ARG}"
    fun responses(requestId: String) = "${Screen.Responses.name}/$requestId"

    val VOLUNTEER_PROFILE = "${Screen.VolunteerProfile.name}/{$VOLUNTEER_ID_ARG}"
    fun volunteerProfile(volunteerId: String) = "${Screen.VolunteerProfile.name}/$volunteerId"
}