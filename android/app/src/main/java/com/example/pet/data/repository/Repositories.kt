package com.example.pet.data.repository

import com.example.pet.data.Chat
import com.example.pet.data.ChatMessage
import com.example.pet.data.Pet
import com.example.pet.data.PetRequest
import com.example.pet.data.Review
import com.example.pet.data.SavedLocation
import com.example.pet.data.ThemeMode
import com.example.pet.data.UserProfile
import com.example.pet.data.UserRole
import com.example.pet.data.Volunteer
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    suspend fun login(email: String, password: String, role: UserRole): Result<Unit>
    suspend fun register(name: String, phone: String, email: String, password: String, role: UserRole): Result<Unit>
    suspend fun confirmCode(code: String): Result<Unit>
    suspend fun requestPasswordReset(target: String): Result<Unit>
    suspend fun resetPassword(newPassword: String): Result<Unit>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun logout()
}

interface ProfileRepository {
    fun profile(role: UserRole): StateFlow<UserProfile>
    suspend fun updateProfile(role: UserRole, profile: UserProfile): Result<Unit>
}

interface PetRepository {
    val pets: StateFlow<List<Pet>>
    suspend fun save(pet: Pet): Result<Unit>
    suspend fun delete(petId: String): Result<Unit>
}

interface RequestRepository {
    val ownerRequests: StateFlow<List<PetRequest>>
    val feed: StateFlow<List<PetRequest>>
    val respondedIds: StateFlow<Set<String>>
    suspend fun save(request: PetRequest): Result<Unit>
    suspend fun respond(requestId: String): Result<Unit>
    suspend fun cancelResponse(requestId: String): Result<Unit>
    suspend fun chooseVolunteer(requestId: String, volunteerId: String?): Result<Unit>
    suspend fun complete(requestId: String): Result<Unit>
}

interface VolunteerRepository {
    val volunteers: StateFlow<List<Volunteer>>
    fun responsesFor(requestId: String): List<Volunteer>
    suspend fun update(volunteer: Volunteer): Result<Unit>
}

interface ReviewRepository {
    val reviews: StateFlow<List<Review>>
    suspend fun add(review: Review): Result<Unit>
}

interface ChatRepository {
    fun chats(role: UserRole): StateFlow<List<Chat>>
    fun messages(chatId: String): StateFlow<List<ChatMessage>>
    suspend fun openChat(role: UserRole, requestId: String, volunteerId: String): Result<Chat>
    suspend fun send(chatId: String, role: UserRole, text: String): Result<Unit>
    suspend fun resend(chatId: String, messageId: String): Result<Unit>
    suspend fun markRead(chatId: String)
}

interface SettingsRepository {
    val themeMode: StateFlow<ThemeMode>
    fun setThemeMode(mode: ThemeMode)
    val volunteerLocation: StateFlow<SavedLocation?>
    fun setVolunteerLocation(location: SavedLocation)
    val locationPrompted: Boolean
    fun markLocationPrompted()
    val sessionRole: UserRole?
    fun setSessionRole(role: UserRole?)
}
