package com.example.pet.data.repository

import com.example.pet.data.Chat
import com.example.pet.data.ChatMessage
import com.example.pet.data.MessageStatus
import com.example.pet.data.MockData
import com.example.pet.data.Pet
import com.example.pet.data.PetRequest
import com.example.pet.data.RequestStatus
import com.example.pet.data.Review
import com.example.pet.data.UserProfile
import com.example.pet.data.UserRole
import com.example.pet.data.Volunteer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

private const val FAKE_NETWORK_DELAY_MS = 400L
private const val FAKE_READ_DELAY_MS = 1500L
private const val FAKE_REPLY_DELAY_MS = 2500L

class FakeAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String, role: UserRole): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun confirmCode(code: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun requestPasswordReset(target: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun resetPassword(newPassword: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun logout() = Unit
}

class InMemoryProfileRepository : ProfileRepository {
    private val owner = MutableStateFlow(MockData.ownerProfile)
    private val volunteer = MutableStateFlow(MockData.volunteerProfile)

    override fun profile(role: UserRole): StateFlow<UserProfile> = when (role) {
        UserRole.Owner -> owner.asStateFlow()
        UserRole.Volunteer -> volunteer.asStateFlow()
    }

    override suspend fun updateProfile(role: UserRole, profile: UserProfile): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        when (role) {
            UserRole.Owner -> owner.value = profile
            UserRole.Volunteer -> volunteer.value = profile
        }
        return Result.success(Unit)
    }
}

class InMemoryPetRepository : PetRepository {
    private val state = MutableStateFlow(MockData.pets)
    override val pets: StateFlow<List<Pet>> = state.asStateFlow()

    override suspend fun save(pet: Pet): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        state.update { list ->
            if (list.any { it.id == pet.id }) list.map { if (it.id == pet.id) pet else it } else list + pet
        }
        return Result.success(Unit)
    }

    override suspend fun delete(petId: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        state.update { list -> list.filterNot { it.id == petId } }
        return Result.success(Unit)
    }
}

class InMemoryRequestRepository : RequestRepository {
    private val owner = MutableStateFlow(MockData.ownerRequests)
    private val feedState = MutableStateFlow(MockData.volunteerFeed)
    private val responded = MutableStateFlow(MockData.respondedIds)

    override val ownerRequests: StateFlow<List<PetRequest>> = owner.asStateFlow()
    override val feed: StateFlow<List<PetRequest>> = feedState.asStateFlow()
    override val respondedIds: StateFlow<Set<String>> = responded.asStateFlow()

    override suspend fun save(request: PetRequest): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        owner.update { list ->
            if (list.any { it.id == request.id }) {
                list.map { if (it.id == request.id) request else it }
            } else {
                listOf(request) + list
            }
        }
        return Result.success(Unit)
    }

    override suspend fun respond(requestId: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        responded.update { it + requestId }
        return Result.success(Unit)
    }

    override suspend fun cancelResponse(requestId: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        responded.update { it - requestId }
        return Result.success(Unit)
    }

    override suspend fun chooseVolunteer(requestId: String, volunteerId: String?): Result<Unit> {
        updateOwnerRequest(requestId) {
            it.copy(
                chosenVolunteerId = volunteerId,
                status = if (volunteerId == null) RequestStatus.Open else RequestStatus.VolunteerChosen
            )
        }
        return Result.success(Unit)
    }

    override suspend fun complete(requestId: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        updateOwnerRequest(requestId) { it.copy(status = RequestStatus.Completed) }
        return Result.success(Unit)
    }

    private fun updateOwnerRequest(requestId: String, transform: (PetRequest) -> PetRequest) {
        owner.update { list -> list.map { if (it.id == requestId) transform(it) else it } }
    }
}

class InMemoryVolunteerRepository : VolunteerRepository {
    private val state = MutableStateFlow(MockData.volunteers)
    override val volunteers: StateFlow<List<Volunteer>> = state.asStateFlow()

    override fun responsesFor(requestId: String): List<Volunteer> = state.value

    override suspend fun update(volunteer: Volunteer): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        state.update { list -> list.map { if (it.id == volunteer.id) volunteer else it } }
        return Result.success(Unit)
    }
}

class InMemoryReviewRepository : ReviewRepository {
    private val state = MutableStateFlow(MockData.reviews)
    override val reviews: StateFlow<List<Review>> = state.asStateFlow()

    override suspend fun add(review: Review): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        state.update { listOf(review) + it }
        return Result.success(Unit)
    }
}

private data class ChatThread(
    val side: UserRole,
    val chat: Chat,
    val messages: List<ChatMessage>,
    val unread: Int
) {
    fun toChat(): Chat = chat.copy(lastMessage = messages.lastOrNull(), unreadCount = unread)
}

class InMemoryChatRepository(
    private val requests: RequestRepository,
    private val volunteers: VolunteerRepository
) : ChatRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Any()
    private val threads = LinkedHashMap<String, ChatThread>()
    private val ownerChats = MutableStateFlow<List<Chat>>(emptyList())
    private val volunteerChats = MutableStateFlow<List<Chat>>(emptyList())
    private val messageFlows = HashMap<String, MutableStateFlow<List<ChatMessage>>>()
    private val replyJobs = HashMap<String, Job>()

    init {
        MockData.chats.forEach { seed ->
            threads[seed.chat.id] = ChatThread(seed.side, seed.chat, seed.messages, seed.unread)
        }
        publish()
    }

    override fun chats(role: UserRole): StateFlow<List<Chat>> = when (role) {
        UserRole.Owner -> ownerChats.asStateFlow()
        UserRole.Volunteer -> volunteerChats.asStateFlow()
    }

    override fun messages(chatId: String): StateFlow<List<ChatMessage>> = synchronized(lock) {
        messageFlows.getOrPut(chatId) { MutableStateFlow(threads[chatId]?.messages.orEmpty()) }.asStateFlow()
    }

    override suspend fun openChat(role: UserRole, requestId: String, volunteerId: String): Result<Chat> {
        val existing = synchronized(lock) {
            threads.values.firstOrNull {
                it.side == role && it.chat.requestId == requestId && it.chat.volunteerId == volunteerId
            }
        }
        if (existing != null) return Result.success(existing.toChat())

        delay(FAKE_NETWORK_DELAY_MS)
        val request = when (role) {
            UserRole.Owner -> requests.ownerRequests.value
            UserRole.Volunteer -> requests.feed.value
        }.firstOrNull { it.id == requestId }
        val volunteer = volunteers.volunteers.value.firstOrNull { it.id == volunteerId }
        if (request == null || volunteer == null) {
            return Result.failure(NoSuchElementException("Chat target not found"))
        }

        val chat = Chat(
            id = UUID.randomUUID().toString(),
            requestId = requestId,
            volunteerId = volunteerId,
            companionName = if (role == UserRole.Owner) volunteer.name else request.ownerName,
            companionAvatarUri = if (role == UserRole.Owner) volunteer.avatarUri else null,
            requestTitle = request.title,
            requestDates = request.dates,
            petPhotoUri = request.petPhotoUri
        )
        synchronized(lock) {
            threads[chat.id] = ChatThread(role, chat, emptyList(), 0)
            publish()
        }
        return Result.success(chat)
    }

    override suspend fun send(chatId: String, role: UserRole, text: String): Result<Unit> {
        val body = text.trim()
        if (body.isEmpty()) return Result.failure(IllegalArgumentException("Empty message"))
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderRole = role,
            text = body,
            sentAt = LocalDateTime.now(),
            status = MessageStatus.Sending
        )
        val added = update(chatId) { it.copy(messages = it.messages + message) }
        if (!added) return Result.failure(NoSuchElementException("Chat not found"))
        deliver(chatId, message.id, role)
        return Result.success(Unit)
    }

    override suspend fun resend(chatId: String, messageId: String): Result<Unit> {
        val senderRole = synchronized(lock) {
            threads[chatId]?.messages?.firstOrNull { it.id == messageId }?.senderRole
        } ?: return Result.failure(NoSuchElementException("Message not found"))
        setStatus(chatId, messageId, MessageStatus.Sending)
        deliver(chatId, messageId, senderRole)
        return Result.success(Unit)
    }

    private fun deliver(chatId: String, messageId: String, senderRole: UserRole) {
        scope.launch {
            delay(FAKE_NETWORK_DELAY_MS)
            setStatus(chatId, messageId, MessageStatus.Sent)
            scheduleReply(chatId, senderRole)
        }
    }

    override suspend fun markRead(chatId: String) {
        update(chatId) { if (it.unread == 0) it else it.copy(unread = 0) }
    }

    private fun setStatus(chatId: String, messageId: String, status: MessageStatus) {
        update(chatId) { thread ->
            thread.copy(messages = thread.messages.map { if (it.id == messageId) it.copy(status = status) else it })
        }
    }

    private fun scheduleReply(chatId: String, senderRole: UserRole) {
        val companionRole = if (senderRole == UserRole.Owner) UserRole.Volunteer else UserRole.Owner
        synchronized(lock) {
            replyJobs[chatId]?.cancel()
            replyJobs[chatId] = scope.launch {
                delay(FAKE_READ_DELAY_MS)
                update(chatId) { thread ->
                    thread.copy(messages = thread.messages.map {
                        if (it.senderRole == senderRole && it.status == MessageStatus.Sent) {
                            it.copy(status = MessageStatus.Read)
                        } else {
                            it
                        }
                    })
                }
                delay(FAKE_REPLY_DELAY_MS)
                val reply = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderRole = companionRole,
                    text = MockData.chatAutoReplies.random(),
                    sentAt = LocalDateTime.now()
                )
                update(chatId) { it.copy(messages = it.messages + reply, unread = it.unread + 1) }
            }
        }
    }

    private fun update(chatId: String, transform: (ChatThread) -> ChatThread): Boolean {
        synchronized(lock) {
            val current = threads[chatId] ?: return false
            val updated = transform(current)
            if (updated != current) {
                threads[chatId] = updated
                publish()
            }
            return true
        }
    }

    private fun publish() {
        fun listFor(side: UserRole): List<Chat> = threads.values
            .filter { it.side == side }
            .map { it.toChat() }
            .sortedByDescending { it.lastMessage?.sentAt }
        ownerChats.value = listFor(UserRole.Owner)
        volunteerChats.value = listFor(UserRole.Volunteer)
        messageFlows.forEach { (id, flow) -> threads[id]?.let { flow.value = it.messages } }
    }
}
