# 🧠 Luma

**Luma** — Android-приложение для ментальных тренировок (тревога, сон, прокрастинация, стресс, фокус). 
Проект создан как демонстрация навыков современной Android-разработки для Junior-позиции.

## 📱 Скриншоты

<p align="center">
  <img src="https://github.com/vladosvorobev1-afk/Luma-App/blob/main/photo_2026-10-21_15-10-42.jpg?raw=true" width="220" />
  <img src="https://github.com/vladosvorobev1-afk/Luma-App/blob/main/photo_2026-10-21_15-10-55.jpg?raw=true" width="220" />
  <img src="https://github.com/vladosvorobev1-afk/Luma-App/blob/main/photo_2026-10-21_15-10-58.jpg?raw=true" width="220" />
  <img src="https://github.com/vladosvorobev1-afk/Luma-App/blob/main/photo_2026-10-21_15-11-01.jpg?raw=true" width="220" />
</p>

## 🛠 Стек технологий
- **Язык:** Kotlin 1.9.24
- **UI:** Jetpack Compose (BOM 2024.06.00), Material Design 3
- **Архитектура:** MVVM + Repository
- **Сеть:** Retrofit 2.11.0, OkHttp 4.12.0, Kotlinx Serialization 1.6.3
- **Локальное хранилище:** Room 2.6.1 (Cache-first), DataStore 1.1.1
- **Фон и уведомления:** WorkManager 2.9.0, Deep Links, NotificationChannel
- **Мультимедиа:** TTS (Text-to-Speech) для озвучки инструкций
- **Тестирование:** JUnit 4, Mockk 1.13.11

## 🚀 Ключевые особенности реализации
1. **Cache-first архитектура:** Приложение сначала берет данные из Room, если кэш пуст — из сети (Retrofit), а при отсутствии интернета использует fallback-данные.
2. **Статистика и Стрик (Streak):** Сложный SQL-запрос для подсчета активных дней. Логика подсчета текущей и лучшей серии (Streak) вынесена в чистую функцию и покрыта 8-ю юнит-тестами.
3. **Deep Links из уведомлений:** При клике на Push-уведомление от WorkManager открывается не главный экран, а сразу экран конкретного упражнения.
4. **Управляемая тема:** Поддержка System / Light / Dark тем через DataStore, с защитой от мигания через Splash Screen.
5. **Озвучка (TTS):** ProcessLifecycleOwner останавливает воспроизведение при уходе приложения в фон.

## 🧪 Тестирование
Проект покрыт 31 Unit-тестом (JUnit + Mockk):
- Мапперы DTO ↔ Domain (13 тестов).
- Обработка сетевых ошибок (NetworkErrorMapper) (10 тестов).
- Логика подсчета Streak (8 тестов).

## 👨‍💻 Автор
**Владислав Воробьев** — Android-разработчик (Junior).
- GitHub: [@vladosvorobev1-afk](https://github.com/vladosvorobev1-afk)
