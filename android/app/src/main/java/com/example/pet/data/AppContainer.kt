package com.example.pet.data

import android.content.Context
import com.example.pet.data.repository.AuthRepository
import com.example.pet.data.repository.ChatRepository
import com.example.pet.data.repository.FakeAuthRepository
import com.example.pet.data.repository.FakePushRepository
import com.example.pet.data.repository.InMemoryChatRepository
import com.example.pet.data.repository.InMemoryPetRepository
import com.example.pet.data.repository.InMemoryProfileRepository
import com.example.pet.data.repository.InMemoryRequestRepository
import com.example.pet.data.repository.InMemoryReviewRepository
import com.example.pet.data.repository.InMemoryVolunteerRepository
import com.example.pet.data.repository.PetRepository
import com.example.pet.data.repository.PrefsSettingsRepository
import com.example.pet.data.repository.ProfileRepository
import com.example.pet.data.repository.PushRepository
import com.example.pet.data.repository.RequestRepository
import com.example.pet.data.repository.ReviewRepository
import com.example.pet.data.repository.SettingsRepository
import com.example.pet.data.repository.VolunteerRepository

object AppContainer {
    val profiles: ProfileRepository = InMemoryProfileRepository()
    private val volunteerStore = InMemoryVolunteerRepository()
    val volunteers: VolunteerRepository = volunteerStore
    private val requestStore = InMemoryRequestRepository(volunteerStore)
    val requests: RequestRepository = requestStore
    val pets: PetRepository = InMemoryPetRepository(requestStore)
    val reviews: ReviewRepository = InMemoryReviewRepository()
    val chats: ChatRepository = InMemoryChatRepository(requests, volunteers)
    val push: PushRepository = FakePushRepository()

    lateinit var settings: SettingsRepository
        private set

    lateinit var auth: AuthRepository
        private set

    fun init(context: Context) {
        val appContext = context.applicationContext
        settings = PrefsSettingsRepository(appContext)
        auth = FakeAuthRepository(appContext, settings.sessionRole)
    }
}
