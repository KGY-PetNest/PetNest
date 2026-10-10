package com.example.pet.data.repository

import com.example.pet.data.Account
import com.example.pet.data.Chat
import com.example.pet.data.ChatAttachment
import com.example.pet.data.ChatMessage
import com.example.pet.data.IncomingMessage
import com.example.pet.data.Pet
import com.example.pet.data.PetRequest
import com.example.pet.data.ReportReason
import com.example.pet.data.Review
import com.example.pet.data.SavedLocation
import com.example.pet.data.ThemeMode
import com.example.pet.data.UserProfile
import com.example.pet.data.UserRole
import com.example.pet.data.Volunteer
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class RequestOverlapException : IllegalStateException()

class PetInUseException : IllegalStateException()

class RequestClosedException : IllegalStateException()

class WrongPasswordException : IllegalArgumentException()

class AccountNotFoundException : NoSuchElementException()

class AccountExistsException : IllegalStateException()

interface AuthRepository {
    val account: StateFlow<Account?>
    val demoLogin: String?
        get() = null
    suspend fun login(email: String, password: String): Result<UserRole>
    suspend fun register(name: String, phone: String, email: String, password: String, role: UserRole): Result<Unit>
    suspend fun addRole(role: UserRole): Result<Unit>
    suspend fun switchRole(role: UserRole): Result<Unit>
    suspend fun changeEmail(email: String): Result<Unit>
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
    val responses: StateFlow<Map<String, List<String>>>
    val favorites: StateFlow<Map<String, Set<String>>>
    suspend fun save(request: PetRequest): Result<Unit>
    suspend fun respond(requestId: String): Result<Unit>
    suspend fun cancelResponse(requestId: String): Result<Unit>
    suspend fun withdraw(requestId: String): Result<Unit>
    suspend fun chooseVolunteer(requestId: String, volunteerId: String?): Result<Unit>
    suspend fun complete(requestId: String): Result<Unit>
    suspend fun delete(requestId: String): Result<Unit>
    fun toggleFavorite(requestId: String, volunteerId: String)
}

interface VolunteerRepository {
    val volunteers: StateFlow<List<Volunteer>>
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
    suspend fun send(
        chatId: String,
        role: UserRole,
        text: String,
        attachment: ChatAttachment? = null
    ): Result<Unit>
    suspend fun resend(chatId: String, messageId: String): Result<Unit>
    suspend fun markRead(chatId: String)
    val incoming: SharedFlow<IncomingMessage>
    suspend fun block(chatId: String): Result<Unit>
    suspend fun unblock(chatId: String): Result<Unit>
    suspend fun report(chatId: String, reason: ReportReason, comment: String): Result<Unit>
}

interface PushRepository {
    suspend fun registerToken(token: String): Result<Unit>
    suspend fun unregister(): Result<Unit>
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
    val notificationsPrompted: Boolean
    fun markNotificationsPrompted()
    fun guideSeen(role: UserRole): Boolean
    fun markGuideSeen(role: UserRole)
    val onboardingSeen: Boolean
    fun markOnboardingSeen()
    fun clearVolunteerLocation()
}
