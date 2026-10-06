package com.example.pet.data

import com.example.pet.data.repository.AuthRepository
import com.example.pet.data.repository.FakeAuthRepository
import com.example.pet.data.repository.InMemoryPetRepository
import com.example.pet.data.repository.InMemoryProfileRepository
import com.example.pet.data.repository.InMemoryRequestRepository
import com.example.pet.data.repository.InMemoryReviewRepository
import com.example.pet.data.repository.InMemoryVolunteerRepository
import com.example.pet.data.repository.PetRepository
import com.example.pet.data.repository.ProfileRepository
import com.example.pet.data.repository.RequestRepository
import com.example.pet.data.repository.ReviewRepository
import com.example.pet.data.repository.VolunteerRepository

object AppContainer {
    val auth: AuthRepository = FakeAuthRepository()
    val profiles: ProfileRepository = InMemoryProfileRepository()
    val pets: PetRepository = InMemoryPetRepository()
    val requests: RequestRepository = InMemoryRequestRepository()
    val volunteers: VolunteerRepository = InMemoryVolunteerRepository()
    val reviews: ReviewRepository = InMemoryReviewRepository()
}