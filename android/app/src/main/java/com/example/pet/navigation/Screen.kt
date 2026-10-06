package com.example.pet.navigation

import android.net.Uri
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
    Reviews,
    RequestDetails,
    EditProfile,
    ChangePassword,
    ForgotPassword,
    ResetCode,
    ResetPassword,
    Settings,
}

object Routes {
    const val ROLE_ARG = "role"
    const val REQUEST_ID_ARG = "requestId"
    const val VOLUNTEER_ID_ARG = "volunteerId"
    const val PET_ID_ARG = "petId"
    const val TARGET_ARG = "target"

    val MAIN = "${Screen.Main.name}/{$ROLE_ARG}"
    fun main(role: UserRole) = "${Screen.Main.name}/${role.name}"

    val RESPONSES = "${Screen.Responses.name}/{$REQUEST_ID_ARG}"
    fun responses(requestId: String) = "${Screen.Responses.name}/$requestId"

    val VOLUNTEER_PROFILE = "${Screen.VolunteerProfile.name}/{$VOLUNTEER_ID_ARG}"
    fun volunteerProfile(volunteerId: String) = "${Screen.VolunteerProfile.name}/$volunteerId"

    val REVIEWS = "${Screen.Reviews.name}/{$VOLUNTEER_ID_ARG}"
    fun reviews(volunteerId: String) = "${Screen.Reviews.name}/$volunteerId"

    val REQUEST_DETAILS = "${Screen.RequestDetails.name}/{$REQUEST_ID_ARG}"
    fun requestDetails(requestId: String) = "${Screen.RequestDetails.name}/$requestId"

    val PET_PROFILE = "${Screen.PetProfile.name}?$PET_ID_ARG={$PET_ID_ARG}"
    fun petProfile(petId: String? = null) =
        if (petId == null) Screen.PetProfile.name else "${Screen.PetProfile.name}?$PET_ID_ARG=$petId"

    val CREATE_REQUEST = "${Screen.CreateRequest.name}?$REQUEST_ID_ARG={$REQUEST_ID_ARG}"
    fun createRequest(requestId: String? = null) =
        if (requestId == null) Screen.CreateRequest.name else "${Screen.CreateRequest.name}?$REQUEST_ID_ARG=$requestId"

    val EDIT_PROFILE = "${Screen.EditProfile.name}/{$ROLE_ARG}"
    fun editProfile(role: UserRole) = "${Screen.EditProfile.name}/${role.name}"

    val SETTINGS = "${Screen.Settings.name}/{$ROLE_ARG}"
    fun settings(role: UserRole) = "${Screen.Settings.name}/${role.name}"

    val RESET_CODE = "${Screen.ResetCode.name}/{$TARGET_ARG}"
    fun resetCode(target: String) = "${Screen.ResetCode.name}/${Uri.encode(target)}"
}