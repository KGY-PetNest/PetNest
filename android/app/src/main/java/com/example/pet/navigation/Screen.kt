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
    Conversation,
    ChatInfo,
    Attachment,
    AppGuide,
}

object Routes {
    const val ROLE_ARG = "role"
    const val REQUEST_ID_ARG = "requestId"
    const val VOLUNTEER_ID_ARG = "volunteerId"
    const val PET_ID_ARG = "petId"
    const val TARGET_ARG = "target"
    const val FOR_VOLUNTEER_ARG = "forVolunteer"
    const val CHAT_ID_ARG = "chatId"
    const val MESSAGE_ID_ARG = "messageId"
    const val FIRST_RUN_ARG = "firstRun"
    const val BECOME_ARG = "become"

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

    val EDIT_PROFILE = "${Screen.EditProfile.name}/{$ROLE_ARG}?$BECOME_ARG={$BECOME_ARG}"
    fun editProfile(role: UserRole, become: Boolean = false) =
        if (become) "${Screen.EditProfile.name}/${role.name}?$BECOME_ARG=true" else "${Screen.EditProfile.name}/${role.name}"

    val SETTINGS = "${Screen.Settings.name}/{$ROLE_ARG}"
    fun settings(role: UserRole) = "${Screen.Settings.name}/${role.name}"

    val CONVERSATION = "${Screen.Conversation.name}/{$ROLE_ARG}/{$CHAT_ID_ARG}"
    fun conversation(chatId: String, role: UserRole) = "${Screen.Conversation.name}/${role.name}/$chatId"

    val CHAT_INFO = "${Screen.ChatInfo.name}/{$ROLE_ARG}/{$CHAT_ID_ARG}"
    fun chatInfo(chatId: String, role: UserRole) = "${Screen.ChatInfo.name}/${role.name}/$chatId"

    val ATTACHMENT = "${Screen.Attachment.name}/{$CHAT_ID_ARG}/{$MESSAGE_ID_ARG}"
    fun attachment(chatId: String, messageId: String) = "${Screen.Attachment.name}/$chatId/$messageId"

    val APP_GUIDE = "${Screen.AppGuide.name}/{$ROLE_ARG}?$FIRST_RUN_ARG={$FIRST_RUN_ARG}"
    fun appGuide(role: UserRole, firstRun: Boolean = false) =
        if (firstRun) "${Screen.AppGuide.name}/${role.name}?$FIRST_RUN_ARG=true" else "${Screen.AppGuide.name}/${role.name}"

    val MAP_PICKER = "${Screen.MapPicker.name}?$FOR_VOLUNTEER_ARG={$FOR_VOLUNTEER_ARG}"
    fun mapPicker(forVolunteer: Boolean = false) =
        if (forVolunteer) "${Screen.MapPicker.name}?$FOR_VOLUNTEER_ARG=true" else Screen.MapPicker.name

    val RESET_CODE = "${Screen.ResetCode.name}/{$TARGET_ARG}"
    fun resetCode(target: String) = "${Screen.ResetCode.name}/${Uri.encode(target)}"
}
