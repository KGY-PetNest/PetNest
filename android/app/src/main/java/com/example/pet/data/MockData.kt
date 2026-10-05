package com.example.pet.data

import java.time.LocalDate

object MockData {

    private fun date(month: Int, day: Int): LocalDate = LocalDate.of(2027, month, day)

    val ownerRequests = listOf(
        PetRequest(
            id = "o1", title = "Барсик", petInfo = "Кот, 3 года", kind = PetKind.Cat,
            start = date(5, 12), end = date(5, 19), district = "Центральный район",
            responsesCount = 3
        ),
        PetRequest(
            id = "o2", title = "Муся", petInfo = "Кошка, 1 год", kind = PetKind.Cat,
            start = date(6, 16), end = date(7, 5), district = "Северный район",
            responsesCount = 1
        ),
        PetRequest(
            id = "o3", title = "Памперс", petInfo = "Пёс, 5 лет", kind = PetKind.Dog,
            start = date(5, 7), end = date(5, 11), district = "Южный район",
            responsesCount = 0
        )
    )

    val volunteerFeed = listOf(
        PetRequest(
            id = "v1", title = "Нужна передержка для кота", petInfo = "Кот, 3 года", kind = PetKind.Cat,
            start = date(5, 12), end = date(5, 19), district = "Центральный район",
            traits = listOf(PetTrait.Pills, PetTrait.FearsNoise)
        ),
        PetRequest(
            id = "v2", title = "Собака, 5 лет", petInfo = "Собака, 5 лет", kind = PetKind.Dog,
            start = date(5, 10), end = date(5, 15), district = "Северный район",
            traits = listOf(PetTrait.Active, PetTrait.NeedsWalks)
        ),
        PetRequest(
            id = "v3", title = "Кошка, 2 года", petInfo = "Кошка, 2 года", kind = PetKind.Cat,
            start = date(5, 8), end = date(5, 11), district = "Западный район",
            traits = listOf(PetTrait.Calm)
        ),
        PetRequest(
            id = "v4", title = "Кот, 4 года", petInfo = "Кот, 4 года", kind = PetKind.Cat,
            start = date(5, 15), end = date(5, 25), district = "Южный район",
            traits = listOf(PetTrait.Pills, PetTrait.FearsNoise)
        ),
        PetRequest(
            id = "v5", title = "Хомяк, 1 год", petInfo = "Хомяк, 1 год", kind = PetKind.Other,
            start = date(5, 20), end = date(5, 27), district = "Центральный район",
            traits = listOf(PetTrait.Calm, PetTrait.SpecialDiet)
        ),
        PetRequest(
            id = "v6", title = "Попугай, 2 года", petInfo = "Попугай, 2 года", kind = PetKind.Other,
            start = date(6, 1), end = date(6, 4), district = "Западный район",
            traits = listOf(PetTrait.Active)
        )
    )

    val districts: List<String>
        get() = volunteerFeed.map { it.district }.distinct().sorted()

    val volunteers = listOf(
        Volunteer(
            id = "u1", name = "Мария Петрова", rating = 4.9, reviewsCount = 12,
            experience = "3 года", homeShort = "без других животных",
            about = "Очень люблю животных. Опыт ухода за кошками и собаками. " +
                    "Есть опыт с таблетками и особым уходом.",
            homeConditions = listOf(
                HomeCondition(HomeConditionType.Apartment, "Квартира"),
                HomeCondition(HomeConditionType.NoOtherPets, "Без других животных"),
                HomeCondition(HomeConditionType.SomeoneHome, "Работаю из дома")
            ),
            acceptedPets = listOf("Кошки", "Мелкие собаки")
        ),
        Volunteer(
            id = "u2", name = "Алексей Иванов", rating = 4.8, reviewsCount = 9,
            experience = "5 лет", homeShort = "кошки, собаки",
            about = "Живу в частном доме с большим двором. Гуляю с собаками дважды в день.",
            homeConditions = listOf(
                HomeCondition(HomeConditionType.House, "Частный дом с двором"),
                HomeCondition(HomeConditionType.HasOtherPets, "Есть кошка и собака")
            ),
            acceptedPets = listOf("Собаки", "Кошки")
        ),
        Volunteer(
            id = "u3", name = "Екатерина Соколова", rating = 4.7, reviewsCount = 6,
            experience = "2 года", homeShort = "кошки",
            about = "Спокойная квартира, есть опыт с пожилыми кошками.",
            homeConditions = listOf(
                HomeCondition(HomeConditionType.Apartment, "Квартира"),
                HomeCondition(HomeConditionType.HasOtherPets, "Есть кошка")
            ),
            acceptedPets = listOf("Кошки")
        )
    )

    val currentVolunteer: Volunteer
        get() = volunteers.first()

    fun request(id: String): PetRequest? =
        (ownerRequests + volunteerFeed).firstOrNull { it.id == id }

    fun volunteer(id: String): Volunteer? = volunteers.firstOrNull { it.id == id }

    fun responsesFor(@Suppress("UNUSED_PARAMETER") requestId: String): List<Volunteer> = volunteers
}