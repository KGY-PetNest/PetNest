package com.example.pet.data

import java.time.LocalDate
import java.time.LocalDateTime

data class ChatSeed(
    val side: UserRole,
    val chat: Chat,
    val messages: List<ChatMessage>,
    val unread: Int
)

object MockData {

    const val CURRENT_VOLUNTEER_ID = "u1"

    private val today: LocalDate = LocalDate.now()

    private val now: LocalDateTime = LocalDateTime.now()

    private fun inDays(days: Long): LocalDate = today.plusDays(days)

    private fun daysAgo(days: Long): LocalDate = today.minusDays(days)

    val ownerProfile = UserProfile(
        name = "Петрова Елена Сергеевна",
        phone = "9161234567",
        email = "elena@example.com"
    )

    val volunteerProfile = UserProfile(
        name = "Петрова Мария Ивановна",
        phone = "9267654321",
        email = "maria@example.com"
    )

    val pets = listOf(
        Pet(
            id = "p1", name = "Барсик", animal = "Кот", age = 3,
            traits = listOf(PetTrait.Medication, PetTrait.FearsNoise),
            features = "Таблетка утром вместе с едой, боится пылесоса"
        ),
        Pet(
            id = "p2", name = "Муся", animal = "Кошка", age = 1,
            traits = listOf(PetTrait.Calm, PetTrait.SpecialDiet),
            features = "Ест только влажный корм, очень ласковая"
        ),
        Pet(
            id = "p3", name = "Памперс", animal = "Пёс", age = 5,
            traits = listOf(PetTrait.Active, PetTrait.NeedsWalks),
            features = "Гулять два раза в день, тянет поводок"
        )
    )

    val ownerRequests = listOf(
        PetRequest(
            id = "o1", petId = "p1", title = "Барсик", petInfo = "Кот, 3 года", kind = PetKind.Cat,
            start = inDays(6), end = inDays(13), district = "",
            address = "Москва, ул. Пушкина, 48", comment = "Ключи у соседки из 12 квартиры",
            traits = listOf(PetTrait.Medication, PetTrait.FearsNoise),
            features = "Таблетка утром вместе с едой, боится пылесоса",
            location = GeoPoint(55.7655, 37.605)
        ),
        PetRequest(
            id = "o2", petId = "p2", title = "Муся", petInfo = "Кошка, 1 год", kind = PetKind.Cat,
            start = inDays(40), end = inDays(59), district = "",
            address = "Москва, ул. Горького, 37", comment = "Кормить два раза в день",
            traits = listOf(PetTrait.Calm, PetTrait.SpecialDiet),
            features = "Ест только влажный корм, очень ласковая",
            location = GeoPoint(55.779, 37.615),
            status = RequestStatus.VolunteerChosen, chosenVolunteerId = "u3"
        ),
        PetRequest(
            id = "o3", petId = "p3", title = "Памперс", petInfo = "Пёс, 5 лет", kind = PetKind.Dog,
            start = daysAgo(40), end = daysAgo(35), district = "",
            address = "Москва, ул. Ленина, 5", comment = "Гулять утром и вечером",
            traits = listOf(PetTrait.Active, PetTrait.NeedsWalks),
            features = "Гулять два раза в день, тянет поводок",
            location = GeoPoint(55.73, 37.61),
            status = RequestStatus.Completed, chosenVolunteerId = "u2"
        )
    )

    val volunteerFeed = listOf(
        PetRequest(
            id = "v1", petId = "x1", title = "Нужна передержка для кота", petInfo = "Кот, 3 года", kind = PetKind.Cat,
            start = inDays(6), end = inDays(13), district = "",
            address = "Москва, ул. Тверская, 10", comment = "Кот спокойный, нужен корм по расписанию",
            traits = listOf(PetTrait.Medication, PetTrait.FearsNoise),
            features = "Таблетка от давления утром, прячется при громких звуках",
            location = GeoPoint(55.7616, 37.609),
            ownerName = "Анна", ownerPhone = "9031112233"
        ),
        PetRequest(
            id = "v2", petId = "x2", title = "Собака, 5 лет", petInfo = "Собака, 5 лет", kind = PetKind.Dog,
            start = inDays(4), end = inDays(9), district = "",
            address = "Москва, ул. Лесная, 3", comment = "Очень любит долгие прогулки",
            traits = listOf(PetTrait.Active, PetTrait.NeedsWalks),
            features = "Очень энергичный, нужно минимум два часа прогулок в день",
            location = GeoPoint(55.7801, 37.5912),
            ownerName = "Игорь", ownerPhone = "9054445566"
        ),
        PetRequest(
            id = "v3", petId = "x3", title = "Кошка, 2 года", petInfo = "Кошка, 2 года", kind = PetKind.Cat,
            start = inDays(2), end = inDays(5), district = "",
            address = "Москва, проспект Мира, 21", comment = "Не любит, когда берут на руки",
            traits = listOf(PetTrait.Calm),
            features = "Спокойная, любит сидеть на подоконнике, не берите на руки",
            location = GeoPoint(55.781, 37.633),
            ownerName = "Ольга", ownerPhone = "9067778899"
        ),
        PetRequest(
            id = "v4", petId = "x4", title = "Кот, 4 года", petInfo = "Кот, 4 года", kind = PetKind.Cat,
            start = inDays(9), end = inDays(19), district = "",
            address = "Москва, Садовая-Кудринская улица, 7", comment = "Нужно давать лекарство вечером",
            traits = listOf(PetTrait.Medication, PetTrait.Senior),
            features = "Пожилой, лекарство вечером в паштете, мало двигается",
            location = GeoPoint(55.764, 37.587),
            ownerName = "Дмитрий", ownerPhone = "9161239876"
        ),
        PetRequest(
            id = "v5", petId = "x5", title = "Хомяк, 1 год", petInfo = "Хомяк, 1 год", kind = PetKind.Other,
            start = inDays(14), end = inDays(21), district = "",
            address = "Москва, ул. Арбат, 15", comment = "Клетку привезу сам",
            traits = listOf(PetTrait.Calm, PetTrait.SpecialDiet),
            features = "Корм только специальный, зерновую смесь не давать",
            location = GeoPoint(55.752, 37.593),
            ownerName = "Света", ownerPhone = "9257654321"
        ),
        PetRequest(
            id = "v6", petId = "x6", title = "Пёс, 7 лет", petInfo = "Пёс, 7 лет", kind = PetKind.Dog,
            start = inDays(26), end = inDays(29), district = "",
            address = "Москва, Кутузовский проспект, 2", comment = "Не ладит с другими собаками",
            traits = listOf(PetTrait.NotFriendlyWithAnimals, PetTrait.NeedsWalks),
            features = "Агрессивно реагирует на других собак, гулять на коротком поводке",
            location = GeoPoint(55.748, 37.565),
            ownerName = "Павел", ownerPhone = "9268887766"
        ),
        PetRequest(
            id = "v7", petId = "x7", title = "Кошка, 6 лет", petInfo = "Кошка, 6 лет", kind = PetKind.Cat,
            start = inDays(3), end = inDays(9), district = "",
            address = "Москва, Новослободская улица, 14", comment = "Корм и наполнитель оставлю",
            addressDetails = "кв. 27, подъезд 2, этаж 5",
            traits = listOf(PetTrait.Calm, PetTrait.SpecialDiet),
            features = "Ест только по расписанию, очень спокойная",
            location = GeoPoint(55.7835, 37.6),
            status = RequestStatus.VolunteerChosen, chosenVolunteerId = CURRENT_VOLUNTEER_ID, ownerName = "Наталья", ownerPhone = "9165553311"
        ),
        PetRequest(
            id = "v8", petId = "x8", title = "Пёс, 2 года", petInfo = "Пёс, 2 года", kind = PetKind.Dog,
            start = inDays(12), end = inDays(16), district = "",
            address = "Москва, Ленинградский проспект, 30", comment = "Нужны две прогулки в день",
            traits = listOf(PetTrait.Active, PetTrait.NeedsWalks),
            features = "Молодой и игривый, любит мяч",
            location = GeoPoint(55.79, 37.56),
            status = RequestStatus.VolunteerChosen, chosenVolunteerId = "u2", ownerName = "Сергей", ownerPhone = "9031234455"
        ),
        PetRequest(
            id = "v9", petId = "x9", title = "Кот, 8 лет", petInfo = "Кот, 8 лет", kind = PetKind.Cat,
            start = daysAgo(60), end = daysAgo(55), district = "",
            address = "Москва, Тверская улица, 22", comment = "Лекарство утром",
            traits = listOf(PetTrait.Medication, PetTrait.Senior),
            features = "Пожилой, таблетка утром в корме",
            location = GeoPoint(55.768, 37.599),
            status = RequestStatus.Completed, chosenVolunteerId = CURRENT_VOLUNTEER_ID, ownerName = "Ольга", ownerPhone = "9269991122"
        )
    )

    val volunteers = listOf(
        Volunteer(
            id = "u1", name = "Петрова Мария Ивановна", experience = "3 года",
            about = "Очень люблю животных. Опыт ухода за кошками и собаками. " +
                    "Есть опыт с таблетками и особым уходом.",
            homeConditions = listOf(
                HomeConditionType.Apartment,
                HomeConditionType.WindowNets,
                HomeConditionType.NoOtherPets,
                HomeConditionType.SomeoneHome,
                HomeConditionType.CanGiveMedication
            ),
            acceptedPets = listOf(AcceptedPet.Cats, AcceptedPet.SmallDogs),
            phone = "9267654321"
        ),
        Volunteer(
            id = "u2", name = "Иванов Алексей Петрович", experience = "5 лет",
            about = "Живу в частном доме с большим двором. Гуляю с собаками дважды в день.",
            homeConditions = listOf(
                HomeConditionType.House,
                HomeConditionType.Yard,
                HomeConditionType.HasOtherPets,
                HomeConditionType.HasKids,
                HomeConditionType.CanWalk
            ),
            acceptedPets = listOf(AcceptedPet.SmallDogs, AcceptedPet.LargeDogs, AcceptedPet.Cats),
            phone = "9035557788"
        ),
        Volunteer(
            id = "u3", name = "Соколова Екатерина Олеговна", experience = "2 года",
            about = "Спокойная квартира, есть опыт с пожилыми кошками.",
            homeConditions = listOf(
                HomeConditionType.Apartment,
                HomeConditionType.WindowNets,
                HomeConditionType.HasOtherPets,
                HomeConditionType.NoKids
            ),
            acceptedPets = listOf(AcceptedPet.Cats, AcceptedPet.Rodents),
            phone = "9154442211"
        )
    )

    val reviews = listOf(
        Review("r1", "u1", "v9", "Ольга", 5, "Мария прекрасно позаботилась о нашем коте, присылала фото каждый день.", daysAgo(54)),
        Review("r2", "u1", null, "Игорь", 5, "Всё отлично, собака вернулась довольная и спокойная.", daysAgo(80)),
        Review("r3", "u1", null, "Анна", 4, "Хорошая передержка, но хотелось бы чаще получать новости.", daysAgo(120)),
        Review("r4", "u2", null, "Света", 5, "Алексей гулял с собакой даже под дождём. Рекомендую!", daysAgo(95)),
        Review("r5", "u2", null, "Павел", 4, "Всё хорошо, пёс набегался во дворе.", daysAgo(140)),
        Review("r6", "u3", null, "Дмитрий", 5, "Катя очень бережно отнеслась к нашему пожилому коту.", daysAgo(85))
    )

    val respondedIds = setOf("v2", "v7", "v8", "v9")

    val ownerResponses = mapOf(
        "o1" to listOf("u1", "u2", "u3"),
        "o2" to listOf("u3", "u1"),
        "o3" to listOf("u2")
    )

    val simulatedResponders = listOf("u2", "u3", "u1")

    private fun message(
        chatId: String,
        index: Int,
        from: UserRole,
        text: String,
        ago: Long,
        status: MessageStatus = MessageStatus.Read,
        event: ChatEvent? = null
    ) = ChatMessage(
        id = "$chatId-$index",
        chatId = chatId,
        senderRole = from,
        text = text,
        sentAt = now.minusMinutes(ago),
        status = status,
        event = event
    )

    private fun ownerChat(id: String, requestId: String, volunteerId: String): Chat {
        val request = ownerRequests.first { it.id == requestId }
        val companion = volunteers.first { it.id == volunteerId }
        return Chat(
            id = id,
            requestId = requestId,
            volunteerId = volunteerId,
            companionName = companion.name,
            companionAvatarUri = companion.avatarUri,
            requestTitle = request.title,
            requestDates = request.dates,
            petPhotoUri = request.petPhotoUri
        )
    }

    private fun volunteerChat(id: String, requestId: String): Chat {
        val request = volunteerFeed.first { it.id == requestId }
        return Chat(
            id = id,
            requestId = requestId,
            volunteerId = CURRENT_VOLUNTEER_ID,
            companionName = request.ownerName,
            requestTitle = request.title,
            requestDates = request.dates,
            petPhotoUri = request.petPhotoUri
        )
    }

    private val ownerSide = UserRole.Owner
    private val volunteerSide = UserRole.Volunteer

    val chats = listOf(
        ChatSeed(
            side = ownerSide,
            chat = ownerChat("c1", "o1", "u1"),
            messages = listOf(
                message("c1", 1, volunteerSide, "Здравствуйте! Увидела заявку на Барсика, могу взять его на эти даты", 26L * 60),
                message("c1", 2, ownerSide, "Здравствуйте! Он принимает таблетку утром, получится давать?", 25L * 60),
                message("c1", 3, volunteerSide, "Да, конечно. Можно спрятать в паштет, у меня был такой опыт", 40L)
            ),
            unread = 1
        ),
        ChatSeed(
            side = ownerSide,
            chat = ownerChat("c2", "o2", "u3"),
            messages = listOf(
                message("c2", 0, ownerSide, "", 3L * 24 * 60 + 125, event = ChatEvent.VolunteerChosen),
                message("c2", 1, ownerSide, "Екатерина, добрый день! Когда вам удобно забрать Мусю?", 3L * 24 * 60 + 120),
                message("c2", 2, volunteerSide, "Добрый! Давайте в пятницу после 18:00", 3L * 24 * 60 + 90),
                message("c2", 3, ownerSide, "Отлично, договорились", 3L * 24 * 60 + 80)
            ),
            unread = 0
        ),
        ChatSeed(
            side = volunteerSide,
            chat = volunteerChat("c3", "v7"),
            messages = listOf(
                message("c3", 0, ownerSide, "", 5L * 60 + 2, event = ChatEvent.VolunteerChosen),
                message("c3", 1, ownerSide, "Мария, здравствуйте! Я выбрала вас для передержки моей кошки", 5L * 60),
                message("c3", 2, volunteerSide, "Спасибо! Буду рада помочь. Когда удобно передать?", 4L * 60 + 50),
                message("c3", 3, ownerSide, "Ключи оставлю у консьержа, корм и наполнитель в прихожей", 12L),
                message("c3", 4, ownerSide, "Позвоните, когда будете подъезжать", 11L)
            ),
            unread = 2
        ),
        ChatSeed(
            side = volunteerSide,
            chat = volunteerChat("c4", "v2"),
            messages = listOf(
                message("c4", 1, volunteerSide, "Здравствуйте! Сколько примерно гулять с собакой?", 2L * 24 * 60 + 300),
                message("c4", 2, ownerSide, "Минимум два часа в день, лучше утром и вечером", 2L * 24 * 60 + 250)
            ),
            unread = 0
        )
    )

    val chatAutoReplies = listOf(
        "Хорошо, договорились!",
        "Спасибо, сейчас посмотрю",
        "Да, конечно",
        "Отлично, тогда до встречи",
        "Поняла, спасибо за подробности"
    )
}
