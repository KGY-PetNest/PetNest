package com.example.pet.data.repository

import com.example.pet.data.Pet
import com.example.pet.data.PetRequest
import com.example.pet.data.Review
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