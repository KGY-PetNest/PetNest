package com.example.pet.data.repository

import com.example.pet.data.MockData
import com.example.pet.data.Pet
import com.example.pet.data.PetRequest
import com.example.pet.data.RequestStatus
import com.example.pet.data.Review
import com.example.pet.data.UserProfile
import com.example.pet.data.UserRole
import com.example.pet.data.Volunteer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val FAKE_NETWORK_DELAY_MS = 400L

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