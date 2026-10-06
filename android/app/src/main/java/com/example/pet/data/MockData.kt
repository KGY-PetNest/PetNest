package com.example.pet.data

import java.time.LocalDate

object MockData {

    const val CURRENT_VOLUNTEER_ID = "u1"

    private fun date(month: Int, day: Int, year: Int = 2027): LocalDate = LocalDate.of(year, month, day)

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
            start = date(5, 12), end = date(5, 19), district = "",
            address = "Москва, ул. Пушкина, 48", comment = "Ключи у соседки из 12 квартиры",
            traits = listOf(PetTrait.Medication, PetTrait.FearsNoise),
            features = "Таблетка утром вместе с едой, боится пылесоса",
            location = GeoPoint(55.7655, 37.605)
        ),
        PetRequest(
            id = "o2", petId = "p2", title = "Муся", petInfo = "Кошка, 1 год", kind = PetKind.Cat,
            start = date(6, 16), end = date(7, 5), district = "",
            address = "Москва, ул. Горького, 37", comment = "Кормить два раза в день",
            traits = listOf(PetTrait.Calm, PetTrait.SpecialDiet),
            features = "Ест только влажный корм, очень ласковая",
            location = GeoPoint(55.779, 37.615),
            status = RequestStatus.VolunteerChosen, chosenVolunteerId = "u3"
        ),
        PetRequest(
            id = "o3", petId = "p3", title = "Памперс", petInfo = "Пёс, 5 лет", kind = PetKind.Dog,
            start = date(3, 1, 2026), end = date(3, 6, 2026), district = "",
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
            start = date(5, 12), end = date(5, 19), district = "",
            address = "Москва, ул. Тверская, 10", comment = "Кот спокойный, нужен корм по расписанию",
            traits = listOf(PetTrait.Medication, PetTrait.FearsNoise),
            features = "Таблетка от давления утром, прячется при громких звуках",
            location = GeoPoint(55.7616, 37.609),
            ownerName = "Анна", ownerPhone = "9031112233"
        ),
        PetRequest(
            id = "v2", petId = "x2", title = "Собака, 5 лет", petInfo = "Собака, 5 лет", kind = PetKind.Dog,
            start = date(5, 10), end = date(5, 15), district = "",
            address = "Москва, ул. Лесная, 3", comment = "Очень любит долгие прогулки",
            traits = listOf(PetTrait.Active, PetTrait.NeedsWalks),
            features = "Очень энергичный, нужно минимум два часа прогулок в день",
            location = GeoPoint(55.7801, 37.5912),
            ownerName = "Игорь", ownerPhone = "9054445566"
        ),
        PetRequest(
            id = "v3", petId = "x3", title = "Кошка, 2 года", petInfo = "Кошка, 2 года", kind = PetKind.Cat,
            start = date(5, 8), end = date(5, 11), district = "",
            address = "Москва, проспект Мира, 21", comment = "Не любит, когда берут на руки",
            traits = listOf(PetTrait.Calm),
            features = "Спокойная, любит сидеть на подоконнике, не берите на руки",
            location = GeoPoint(55.781, 37.633),
            ownerName = "Ольга", ownerPhone = "9067778899"
        ),
        PetRequest(
            id = "v4", petId = "x4", title = "Кот, 4 года", petInfo = "Кот, 4 года", kind = PetKind.Cat,
            start = date(5, 15), end = date(5, 25), district = "",
            address = "Москва, Садовая-Кудринская улица, 7", comment = "Нужно давать лекарство вечером",
            traits = listOf(PetTrait.Medication, PetTrait.Senior),
            features = "Пожилой, лекарство вечером в паштете, мало двигается",
            location = GeoPoint(55.764, 37.587),
            ownerName = "Дмитрий", ownerPhone = "9161239876"
        ),
        PetRequest(
            id = "v5", petId = "x5", title = "Хомяк, 1 год", petInfo = "Хомяк, 1 год", kind = PetKind.Other,
            start = date(5, 20), end = date(5, 27), district = "",
            address = "Москва, ул. Арбат, 15", comment = "Клетку привезу сам",
            traits = listOf(PetTrait.Calm, PetTrait.SpecialDiet),
            features = "Корм только специальный, зерновую смесь не давать",
            location = GeoPoint(55.752, 37.593),
            ownerName = "Света", ownerPhone = "9257654321"
        ),
        PetRequest(
            id = "v6", petId = "x6", title = "Пёс, 7 лет", petInfo = "Пёс, 7 лет", kind = PetKind.Dog,
            start = date(6, 1), end = date(6, 4), district = "",
            address = "Москва, Кутузовский проспект, 2", comment = "Не ладит с другими собаками",
            traits = listOf(PetTrait.NotFriendlyWithAnimals, PetTrait.NeedsWalks),
            features = "Агрессивно реагирует на других собак, гулять на коротком поводке",
            location = GeoPoint(55.748, 37.565),
            ownerName = "Павел", ownerPhone = "9268887766"
        ),
        PetRequest(
            id = "v7", petId = "x7", title = "Кошка, 6 лет", petInfo = "Кошка, 6 лет", kind = PetKind.Cat,
            start = date(5, 3), end = date(5, 9), district = "",
            address = "Москва, Новослободская улица, 14", comment = "Корм и наполнитель оставлю",
            traits = listOf(PetTrait.Calm, PetTrait.SpecialDiet),
            features = "Ест только по расписанию, очень спокойная",
            location = GeoPoint(55.7835, 37.6),
            status = RequestStatus.VolunteerChosen, chosenVolunteerId = CURRENT_VOLUNTEER_ID, ownerName = "Наталья", ownerPhone = "9165553311"
        ),
        PetRequest(
            id = "v8", petId = "x8", title = "Пёс, 2 года", petInfo = "Пёс, 2 года", kind = PetKind.Dog,
            start = date(5, 18), end = date(5, 22), district = "",
            address = "Москва, Ленинградский проспект, 30", comment = "Нужны две прогулки в день",
            traits = listOf(PetTrait.Active, PetTrait.NeedsWalks),
            features = "Молодой и игривый, любит мяч",
            location = GeoPoint(55.79, 37.56),
            status = RequestStatus.VolunteerChosen, chosenVolunteerId = "u2", ownerName = "Сергей", ownerPhone = "9031234455"
        ),
        PetRequest(
            id = "v9", petId = "x9", title = "Кот, 8 лет", petInfo = "Кот, 8 лет", kind = PetKind.Cat,
            start = date(2, 10, 2026), end = date(2, 15, 2026), district = "",
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
                HomeConditionType.NoOtherPets,
                HomeConditionType.SomeoneHome
            ),
            acceptedPets = listOf(AcceptedPet.Cats, AcceptedPet.SmallDogs)
        ),
        Volunteer(
            id = "u2", name = "Иванов Алексей Петрович", experience = "5 лет",
            about = "Живу в частном доме с большим двором. Гуляю с собаками дважды в день.",
            homeConditions = listOf(
                HomeConditionType.House,
                HomeConditionType.Yard,
                HomeConditionType.HasOtherPets
            ),
            acceptedPets = listOf(AcceptedPet.SmallDogs, AcceptedPet.LargeDogs, AcceptedPet.Cats)
        ),
        Volunteer(
            id = "u3", name = "Соколова Екатерина Олеговна", experience = "2 года",
            about = "Спокойная квартира, есть опыт с пожилыми кошками.",
            homeConditions = listOf(
                HomeConditionType.Apartment,
                HomeConditionType.HasOtherPets,
                HomeConditionType.NoKids
            ),
            acceptedPets = listOf(AcceptedPet.Cats, AcceptedPet.Rodents)
        )
    )

    val reviews = listOf(
        Review("r1", "u1", "v9", "Ольга", 5, "Мария прекрасно позаботилась о нашем коте, присылала фото каждый день.", date(2, 16, 2026)),
        Review("r2", "u1", null, "Игорь", 5, "Всё отлично, собака вернулась довольная и спокойная.", date(1, 22, 2026)),
        Review("r3", "u1", null, "Анна", 4, "Хорошая передержка, но хотелось бы чаще получать новости.", date(12, 5, 2025)),
        Review("r4", "u2", null, "Света", 5, "Алексей гулял с собакой даже под дождём. Рекомендую!", date(2, 1, 2026)),
        Review("r5", "u2", null, "Павел", 4, "Всё хорошо, пёс набегался во дворе.", date(11, 14, 2025)),
        Review("r6", "u3", null, "Дмитрий", 5, "Катя очень бережно отнеслась к нашему пожилому коту.", date(1, 30, 2026))
    )

    val respondedIds = setOf("v2", "v7", "v8", "v9")
}