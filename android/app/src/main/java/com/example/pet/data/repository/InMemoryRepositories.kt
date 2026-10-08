package com.example.pet.data.repository

import com.example.pet.data.Chat
import com.example.pet.data.ChatAttachment
import com.example.pet.data.ChatEvent
import com.example.pet.data.ChatMessage
import com.example.pet.data.MessageStatus
import com.example.pet.data.IncomingMessage
import com.example.pet.data.MockData
import com.example.pet.data.canChat
import com.example.pet.data.Pet
import com.example.pet.data.PetRequest
import com.example.pet.data.ReportReason
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
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

class InMemoryPetRepository(private val requests: InMemoryRequestRepository) : PetRepository {
    private val state = MutableStateFlow(MockData.pets)
    override val pets: StateFlow<List<Pet>> = state.asStateFlow()

    override suspend fun save(pet: Pet): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        state.update { list ->
            if (list.any { it.id == pet.id }) list.map { if (it.id == pet.id) pet else it } else list + pet
        }
        requests.syncPet(pet)
        return Result.success(Unit)
    }

    override suspend fun delete(petId: String): Result<Unit> {
        val inUse = requests.ownerRequests.value.any { it.petId == petId && it.status != RequestStatus.Completed }
        if (inUse) return Result.failure(PetInUseException())
        delay(FAKE_NETWORK_DELAY_MS)
        state.update { list -> list.filterNot { it.id == petId } }
        return Result.success(Unit)
    }
}

class InMemoryRequestRepository : RequestRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val owner = MutableStateFlow(MockData.ownerRequests)
    private val feedState = MutableStateFlow(MockData.volunteerFeed)
    private val responded = MutableStateFlow(MockData.respondedIds)
    private val responsesState = MutableStateFlow(MockData.ownerResponses)
    private val favoritesState = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    override val ownerRequests: StateFlow<List<PetRequest>> = owner.asStateFlow()
    override val feed: StateFlow<List<PetRequest>> = feedState.asStateFlow()
    override val respondedIds: StateFlow<Set<String>> = responded.asStateFlow()
    override val responses: StateFlow<Map<String, List<String>>> = responsesState.asStateFlow()
    override val favorites: StateFlow<Map<String, Set<String>>> = favoritesState.asStateFlow()

    override suspend fun save(request: PetRequest): Result<Unit> {
        val current = owner.value.firstOrNull { it.id == request.id }
        if (current != null && current.status != RequestStatus.Open) {
            return Result.failure(RequestClosedException())
        }
        if (request.start.isBefore(LocalDate.now()) || request.end.isBefore(request.start)) {
            return Result.failure(IllegalArgumentException("Invalid dates"))
        }
        val overlaps = owner.value.any { other ->
            other.id != request.id &&
                    other.petId == request.petId &&
                    other.status != RequestStatus.Completed &&
                    !request.start.isAfter(other.end) &&
                    !request.end.isBefore(other.start)
        }
        if (overlaps) return Result.failure(RequestOverlapException())
        delay(FAKE_NETWORK_DELAY_MS)
        owner.update { list ->
            if (current != null) {
                list.map { if (it.id == request.id) request else it }
            } else {
                listOf(request) + list
            }
        }
        val termsChanged = current != null &&
                (current.petId != request.petId || current.start != request.start || current.end != request.end)
        if (termsChanged) {
            responsesState.update { it - request.id }
            favoritesState.update { it - request.id }
        }
        if (current == null || termsChanged) simulateResponses(request.id)
        return Result.success(Unit)
    }

    override suspend fun respond(requestId: String): Result<Unit> {
        val request = feedState.value.firstOrNull { it.id == requestId }
            ?: return Result.failure(NoSuchElementException("Request not found"))
        if (!request.acceptsResponses) return Result.failure(RequestClosedException())
        delay(FAKE_NETWORK_DELAY_MS)
        responded.update { it + requestId }
        return Result.success(Unit)
    }

    override suspend fun cancelResponse(requestId: String): Result<Unit> {
        val request = feedState.value.firstOrNull { it.id == requestId }
        if (request?.chosenVolunteerId == MockData.CURRENT_VOLUNTEER_ID) {
            return Result.failure(IllegalStateException("Use withdraw for a chosen volunteer"))
        }
        delay(FAKE_NETWORK_DELAY_MS)
        responded.update { it - requestId }
        return Result.success(Unit)
    }

    override suspend fun withdraw(requestId: String): Result<Unit> {
        val request = feedState.value.firstOrNull { it.id == requestId }
            ?: return Result.failure(NoSuchElementException("Request not found"))
        if (request.chosenVolunteerId != MockData.CURRENT_VOLUNTEER_ID ||
            request.status != RequestStatus.VolunteerChosen
        ) {
            return Result.failure(IllegalStateException("Nothing to withdraw from"))
        }
        delay(FAKE_NETWORK_DELAY_MS)
        responded.update { it - requestId }
        feedState.update { list ->
            list.map {
                if (it.id == requestId) it.copy(status = RequestStatus.Open, chosenVolunteerId = null) else it
            }
        }
        return Result.success(Unit)
    }

    override suspend fun chooseVolunteer(requestId: String, volunteerId: String?): Result<Unit> {
        val request = owner.value.firstOrNull { it.id == requestId }
            ?: return Result.failure(NoSuchElementException("Request not found"))
        val allowed = when {
            request.status == RequestStatus.Completed -> false
            volunteerId == null -> true
            request.start.isBefore(LocalDate.now()) -> false
            else -> volunteerId in responsesState.value[requestId].orEmpty()
        }
        if (!allowed) return Result.failure(RequestClosedException())
        delay(FAKE_NETWORK_DELAY_MS)
        updateOwnerRequest(requestId) {
            it.copy(
                chosenVolunteerId = volunteerId,
                status = if (volunteerId == null) RequestStatus.Open else RequestStatus.VolunteerChosen
            )
        }
        return Result.success(Unit)
    }

    override suspend fun complete(requestId: String): Result<Unit> {
        val request = owner.value.firstOrNull { it.id == requestId }
            ?: return Result.failure(NoSuchElementException("Request not found"))
        if (request.status != RequestStatus.VolunteerChosen || LocalDate.now().isBefore(request.start)) {
            return Result.failure(IllegalStateException("Request cannot be completed yet"))
        }
        delay(FAKE_NETWORK_DELAY_MS)
        updateOwnerRequest(requestId) { it.copy(status = RequestStatus.Completed) }
        return Result.success(Unit)
    }

    override suspend fun delete(requestId: String): Result<Unit> {
        val request = owner.value.firstOrNull { it.id == requestId }
            ?: return Result.failure(NoSuchElementException("Request not found"))
        if (request.status != RequestStatus.Open) {
            return Result.failure(RequestClosedException())
        }
        delay(FAKE_NETWORK_DELAY_MS)
        owner.update { list -> list.filterNot { it.id == requestId } }
        responsesState.update { it - requestId }
        favoritesState.update { it - requestId }
        return Result.success(Unit)
    }

    override fun toggleFavorite(requestId: String, volunteerId: String) {
        favoritesState.update { map ->
            val current = map[requestId].orEmpty()
            val updated = if (volunteerId in current) current - volunteerId else current + volunteerId
            map + (requestId to updated)
        }
    }

    fun syncPet(pet: Pet) {
        owner.update { list ->
            list.map { request ->
                if (request.petId == pet.id && request.status != RequestStatus.Completed) {
                    request.copy(
                        title = pet.name,
                        petInfo = pet.info,
                        kind = pet.kind,
                        traits = pet.traits,
                        features = pet.features,
                        petPhotoUri = pet.photoUri
                    )
                } else {
                    request
                }
            }
        }
    }

    private fun simulateResponses(requestId: String) {
        scope.launch {
            MockData.simulatedResponders.forEachIndexed { index, volunteerId ->
                delay(SIMULATED_RESPONSE_DELAY_MS * (index + 1))
                val request = owner.value.firstOrNull { it.id == requestId } ?: return@launch
                if (!request.acceptsResponses) return@launch
                responsesState.update { map ->
                    val current = map[requestId].orEmpty()
                    if (volunteerId in current) map else map + (requestId to current + volunteerId)
                }
            }
        }
    }

    private fun updateOwnerRequest(requestId: String, transform: (PetRequest) -> PetRequest) {
        owner.update { list -> list.map { if (it.id == requestId) transform(it) else it } }
    }

    private companion object {
        const val SIMULATED_RESPONSE_DELAY_MS = 6000L
    }
}

class InMemoryVolunteerRepository : VolunteerRepository {
    private val state = MutableStateFlow(MockData.volunteers)
    override val volunteers: StateFlow<List<Volunteer>> = state.asStateFlow()

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
        val duplicate = review.requestId != null && state.value.any { it.requestId == review.requestId }
        if (duplicate) return Result.failure(IllegalStateException("Review already exists"))
        delay(FAKE_NETWORK_DELAY_MS)
        var added = false
        state.update { list ->
            if (review.requestId != null && list.any { it.requestId == review.requestId }) {
                list
            } else {
                added = true
                listOf(review) + list
            }
        }
        return if (added) Result.success(Unit) else Result.failure(IllegalStateException("Review already exists"))
    }
}

private data class ChatThread(
    val side: UserRole,
    val chat: Chat,
    val messages: List<ChatMessage>,
    val unread: Int,
    val blocked: Boolean = false
) {
    fun toChat(): Chat = chat.copy(lastMessage = messages.lastOrNull(), unreadCount = unread, blocked = blocked)
}

class FakePushRepository : PushRepository {
    private var token: String? = null

    override suspend fun registerToken(token: String): Result<Unit> {
        this.token = token
        return Result.success(Unit)
    }

    override suspend fun unregister(): Result<Unit> {
        token = null
        return Result.success(Unit)
    }
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
    private val incomingFlow = MutableSharedFlow<IncomingMessage>(extraBufferCapacity = INCOMING_BUFFER)

    override val incoming: SharedFlow<IncomingMessage> = incomingFlow.asSharedFlow()

    init {
        MockData.chats.forEach { seed ->
            threads[seed.chat.id] = ChatThread(seed.side, seed.chat, seed.messages, seed.unread)
        }
        publish()
        scope.launch {
            var previous = requests.ownerRequests.value.associateBy { it.id }
            requests.ownerRequests.collect { list ->
                val current = list.associateBy { it.id }
                current.values.forEach { request ->
                    previous[request.id]?.let { old ->
                        onOwnerRequestChanged(old, request)
                        if (old.title != request.title || old.dates != request.dates || old.petPhotoUri != request.petPhotoUri) {
                            refreshThreads { thread ->
                                if (thread.side == UserRole.Owner && thread.chat.requestId == request.id) {
                                    thread.copy(
                                        chat = thread.chat.copy(
                                            requestTitle = request.title,
                                            requestDates = request.dates,
                                            petPhotoUri = request.petPhotoUri
                                        )
                                    )
                                } else {
                                    thread
                                }
                            }
                        }
                    }
                }
                val removed = previous.keys - current.keys
                if (removed.isNotEmpty()) removeOwnerThreads(removed)
                previous = current
            }
        }
        scope.launch {
            volunteers.volunteers.collect { list ->
                val byId = list.associateBy { it.id }
                refreshThreads { thread ->
                    val volunteer = byId[thread.chat.volunteerId]
                    if (thread.side == UserRole.Owner && volunteer != null &&
                        (thread.chat.companionName != volunteer.name || thread.chat.companionAvatarUri != volunteer.avatarUri)
                    ) {
                        thread.copy(chat = thread.chat.copy(companionName = volunteer.name, companionAvatarUri = volunteer.avatarUri))
                    } else {
                        thread
                    }
                }
            }
        }
        scope.launch {
            var previous = requests.feed.value.associateBy { it.id }
            requests.feed.collect { list ->
                val current = list.associateBy { it.id }
                current.values.forEach { request ->
                    val old = previous[request.id]
                    if (old != null &&
                        old.chosenVolunteerId == MockData.CURRENT_VOLUNTEER_ID &&
                        request.chosenVolunteerId == null &&
                        request.status == RequestStatus.Open
                    ) {
                        postEvent(UserRole.Volunteer, request, MockData.CURRENT_VOLUNTEER_ID, ChatEvent.VolunteerWithdrew)
                    }
                }
                previous = current
            }
        }
    }

    override fun chats(role: UserRole): StateFlow<List<Chat>> = when (role) {
        UserRole.Owner -> ownerChats.asStateFlow()
        UserRole.Volunteer -> volunteerChats.asStateFlow()
    }

    override fun messages(chatId: String): StateFlow<List<ChatMessage>> = synchronized(lock) {
        messageFlows.getOrPut(chatId) { MutableStateFlow(threads[chatId]?.messages.orEmpty()) }.asStateFlow()
    }

    override suspend fun openChat(role: UserRole, requestId: String, volunteerId: String): Result<Chat> {
        val existing = findThread(role, requestId, volunteerId)
        if (existing != null) return Result.success(existing.toChat())

        delay(FAKE_NETWORK_DELAY_MS)
        val chat = createThread(role, requestId, volunteerId)
            ?: return Result.failure(NoSuchElementException("Chat target not found"))
        return Result.success(chat.toChat())
    }

    override suspend fun send(
        chatId: String,
        role: UserRole,
        text: String,
        attachment: ChatAttachment?
    ): Result<Unit> {
        val body = text.trim()
        if (body.isEmpty() && attachment == null) return Result.failure(IllegalArgumentException("Empty message"))
        val thread = synchronized(lock) { threads[chatId] }
            ?: return Result.failure(NoSuchElementException("Chat not found"))
        if (thread.blocked) return Result.failure(IllegalStateException("Chat is blocked"))
        if (!canWrite(thread)) return Result.failure(IllegalStateException("No access to chat"))
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderRole = role,
            text = body,
            sentAt = LocalDateTime.now(),
            status = MessageStatus.Sending,
            attachment = attachment
        )
        val added = update(chatId) { it.copy(messages = it.messages + message) }
        if (!added) return Result.failure(NoSuchElementException("Chat not found"))
        deliver(chatId, message.id, role)
        return Result.success(Unit)
    }

    override suspend fun resend(chatId: String, messageId: String): Result<Unit> {
        val thread = synchronized(lock) { threads[chatId] }
            ?: return Result.failure(NoSuchElementException("Chat not found"))
        if (thread.blocked) return Result.failure(IllegalStateException("Chat is blocked"))
        if (!canWrite(thread)) return Result.failure(IllegalStateException("No access to chat"))
        val senderRole = thread.messages.firstOrNull { it.id == messageId }?.senderRole
            ?: return Result.failure(NoSuchElementException("Message not found"))
        setStatus(chatId, messageId, MessageStatus.Sending)
        deliver(chatId, messageId, senderRole)
        return Result.success(Unit)
    }

    override suspend fun markRead(chatId: String) {
        update(chatId) { if (it.unread == 0) it else it.copy(unread = 0) }
    }

    override suspend fun block(chatId: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        synchronized(lock) { replyJobs.remove(chatId)?.cancel() }
        val updated = update(chatId) { it.copy(blocked = true) }
        return if (updated) Result.success(Unit) else Result.failure(NoSuchElementException("Chat not found"))
    }

    override suspend fun unblock(chatId: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        val updated = update(chatId) { it.copy(blocked = false) }
        return if (updated) Result.success(Unit) else Result.failure(NoSuchElementException("Chat not found"))
    }

    override suspend fun report(chatId: String, reason: ReportReason, comment: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        val exists = synchronized(lock) { chatId in threads }
        return if (exists) Result.success(Unit) else Result.failure(NoSuchElementException("Chat not found"))
    }

    private fun canWrite(thread: ChatThread): Boolean {
        if (thread.side == UserRole.Owner) return true
        val status = requests.feed.value.firstOrNull { it.id == thread.chat.requestId }
            ?.statusFor(thread.chat.volunteerId, requests.respondedIds.value)
        return status.canChat
    }

    private fun findThread(role: UserRole, requestId: String, volunteerId: String): ChatThread? =
        synchronized(lock) {
            threads.values.firstOrNull {
                it.side == role && it.chat.requestId == requestId && it.chat.volunteerId == volunteerId
            }
        }

    private fun createThread(role: UserRole, requestId: String, volunteerId: String): ChatThread? {
        val request = when (role) {
            UserRole.Owner -> requests.ownerRequests.value
            UserRole.Volunteer -> requests.feed.value
        }.firstOrNull { it.id == requestId }
        val volunteer = volunteers.volunteers.value.firstOrNull { it.id == volunteerId }
        if (request == null || volunteer == null) return null

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
            threads.values.firstOrNull {
                it.side == role && it.chat.requestId == requestId && it.chat.volunteerId == volunteerId
            }?.let { return it }
            val thread = ChatThread(role, chat, emptyList(), 0)
            threads[chat.id] = thread
            publish()
            return thread
        }
    }

    private fun onOwnerRequestChanged(old: PetRequest, new: PetRequest) {
        if (old.chosenVolunteerId != new.chosenVolunteerId) {
            old.chosenVolunteerId?.let { postEvent(UserRole.Owner, new, it, ChatEvent.ChoiceCancelled) }
            new.chosenVolunteerId?.let { postEvent(UserRole.Owner, new, it, ChatEvent.VolunteerChosen) }
        }
        if (old.status != RequestStatus.Completed && new.status == RequestStatus.Completed) {
            new.chosenVolunteerId?.let { postEvent(UserRole.Owner, new, it, ChatEvent.Completed) }
        }
    }

    private fun refreshThreads(transform: (ChatThread) -> ChatThread) {
        synchronized(lock) {
            var changed = false
            threads.entries.forEach { entry ->
                val updated = transform(entry.value)
                if (updated != entry.value) {
                    entry.setValue(updated)
                    changed = true
                }
            }
            if (changed) publish()
        }
    }

    private fun removeOwnerThreads(requestIds: Set<String>) {
        synchronized(lock) {
            val ids = threads.values
                .filter { it.side == UserRole.Owner && it.chat.requestId in requestIds }
                .map { it.chat.id }
            if (ids.isEmpty()) return
            ids.forEach { id ->
                replyJobs.remove(id)?.cancel()
                threads.remove(id)
                messageFlows.remove(id)
            }
            publish()
        }
    }

    private fun postEvent(side: UserRole, request: PetRequest, volunteerId: String, event: ChatEvent) {
        val thread = findThread(side, request.id, volunteerId)
            ?: (if (event == ChatEvent.VolunteerChosen) createThread(side, request.id, volunteerId) else null)
            ?: return
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = thread.chat.id,
            senderRole = side,
            text = "",
            sentAt = LocalDateTime.now(),
            status = MessageStatus.Read,
            event = event
        )
        update(thread.chat.id) { it.copy(messages = it.messages + message) }
    }

    private fun deliver(chatId: String, messageId: String, senderRole: UserRole) {
        scope.launch {
            delay(FAKE_NETWORK_DELAY_MS)
            setStatus(chatId, messageId, MessageStatus.Sent)
            scheduleReply(chatId, senderRole)
        }
    }

    private fun setStatus(chatId: String, messageId: String, status: MessageStatus) {
        update(chatId) { thread ->
            thread.copy(messages = thread.messages.map { if (it.id == messageId) it.copy(status = status) else it })
        }
    }

    private fun scheduleReply(chatId: String, senderRole: UserRole) {
        val companionRole = if (senderRole == UserRole.Owner) UserRole.Volunteer else UserRole.Owner
        synchronized(lock) {
            if (threads[chatId]?.blocked == true) return
            replyJobs[chatId]?.cancel()
            val job = scope.launch {
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
                var delivered: ChatThread? = null
                update(chatId) { thread ->
                    if (thread.blocked) {
                        thread
                    } else {
                        thread.copy(messages = thread.messages + reply, unread = thread.unread + 1)
                            .also { delivered = it }
                    }
                }
                delivered?.let { incomingFlow.tryEmit(IncomingMessage(it.side, it.toChat(), reply)) }
            }
            replyJobs[chatId] = job
            job.invokeOnCompletion {
                synchronized(lock) { if (replyJobs[chatId] === job) replyJobs.remove(chatId) }
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

    private companion object {
        const val INCOMING_BUFFER = 16
    }
}
