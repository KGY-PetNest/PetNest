# PetNest

## Структура

| Папка      | Что внутри                  |
|------------|-----------------------------|
| `backend/` | API-сервер на Ktor (Kotlin) |

## Backend

Стек: Kotlin 2.2, Ktor 3.1 (Netty), kotlinx.serialization, JDK 21.

### Запуск

```bash
cd backend
./gradlew run
```

Сервер поднимается на `http://localhost:8080`. Порт можно переопределить переменной окружения `PORT`.

### Эндпоинты

| Метод | Путь      | Описание            |
|-------|-----------|---------------------|
| GET   | `/`       | Приветствие         |
| GET   | `/health` | Проверка живости    |

### Тесты и сборка

```bash
./gradlew test        # тесты
./gradlew installDist # сборка в build/install/backend
```
