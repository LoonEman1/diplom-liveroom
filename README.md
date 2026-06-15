# LiveRoom Android

**LiveRoom** — нативное Android-приложение для дистанционного образовательного взаимодействия. Приложение позволяет пользователям проходить авторизацию, работать с учебными серверами, обмениваться сообщениями в каналах, подключаться к голосовым занятиям и просматривать аналитику активности.

Проект разработан как мобильный клиент для дипломной работы и демонстрирует реализацию клиентской части системы удалённого обучения: пользовательская авторизация, образовательные сообщества, текстовые каналы, realtime-обмен сообщениями, голосовые вызовы и аналитика посещаемости.

## Основные возможности

- регистрация и авторизация пользователя;
- сохранение пользовательской сессии и токенов через DataStore;
- автоматическое добавление access token к сетевым запросам;
- обновление токена через refresh token;
- просмотр и редактирование профиля пользователя;
- загрузка и обновление аватара пользователя;
- создание, редактирование и удаление учебных серверов;
- загрузка и обновление аватаров серверов;
- приглашение пользователей на сервер по никнейму;
- создание invite-token и присоединение к серверу по токену;
- принятие и отклонение входящих приглашений;
- просмотр списка серверов и участников;
- работа с каналами/диалогами внутри сервера;
- создание, редактирование и удаление каналов;
- загрузка, отправка, редактирование и удаление сообщений;
- realtime-обновления через WebSocket/STOMP;
- голосовые занятия на базе WebRTC;
- обработка сигналинга звонков через WebSocket;
- просмотр активных звонков;
- просмотр аналитики по занятиям, сессиям и пользователям.


## Скриншоты приложения

Ниже можно разместить изображения основных экранов приложения. Для добавления скриншотов создайте в корне репозитория папку `docs/images` и замените пути к файлам на актуальные.

### Авторизация и регистрация

<img width="376" height="838" alt="image" src="https://github.com/user-attachments/assets/c9eccb23-60cd-48c2-bb57-932c64c6ee96" />

*Рисунок 1 — экран входа или регистрации пользователя.*

### Главный экран и список серверов

<img width="380" height="840" alt="image" src="https://github.com/user-attachments/assets/834d6cf8-977b-479a-9758-5e9950c08130" />


*Рисунок 2 — главный экран приложения со списком учебных серверов и навигацией.*

### Чаты и обмен сообщениями

<img width="377" height="837" alt="image" src="https://github.com/user-attachments/assets/bfb8b5d0-a765-4314-a3a7-fe7b573987d2" />


*Рисунок 3 — экран канала с историей сообщений и realtime-обновлениями.*

### Экран учебного сервера

<img width="379" height="836" alt="image" src="https://github.com/user-attachments/assets/529f2134-8e0d-42ce-8551-fa8bcf1b7be6" />

*Рисунок 4 — экран сервера, в котором можно выбрать текстовый чат или экран аналитики.*

### Экран аналитики

<img width="374" height="833" alt="image" src="https://github.com/user-attachments/assets/a59dece3-38f8-4328-a347-b3d7dd2eae41" />

*Рисунок 5 — экран аналитики по звонкам.*

<img width="377" height="837" alt="image" src="https://github.com/user-attachments/assets/42808ffc-bd5d-4649-a126-5a8cb9140953" />

*Рисунок 6 — экран аналитики по участникам.*

## Стек технологий

- **Kotlin** — основной язык разработки;
- **Jetpack Compose** — декларативный UI;
- **Material 3** — компоненты интерфейса;
- **Navigation Compose** — навигация между экранами;
- **MVVM** — разделение UI-слоя, состояния экрана и слоя данных;
- **Coroutines / Flow** — асинхронная обработка данных и реактивное состояние;
- **Hilt** — dependency injection;
- **Retrofit** — работа с REST API;
- **OkHttp** — HTTP-клиент, interceptors и WebSocket;
- **Gson Converter** — сериализация/десериализация JSON;
- **DataStore Preferences** — локальное хранение токенов и пользовательских данных;
- **Coil** — загрузка изображений;
- **WebSocket / STOMP-like protocol** — realtime-события;
- **WebRTC** — голосовые вызовы;
- **Gradle Kotlin DSL** — конфигурация сборки.

## Архитектура проекта

Проект построен вокруг MVVM-подхода. UI-слой отображает состояние и отправляет действия пользователя во ViewModel. ViewModel управляет состоянием экранов, вызывает repository-слой и обрабатывает результат. Repository инкапсулирует работу с REST API, локальным хранилищем и сетевыми источниками данных.

Упрощённая схема:

```text
Compose Screen
     ↓
ViewModel
     ↓
Repository
     ↓
Retrofit / OkHttp / WebSocket / DataStore
     ↓
Backend API
```

Такое разделение позволяет не смешивать пользовательский интерфейс с сетевой логикой, хранением токенов и обработкой realtime-событий.

## Структура проекта

```text
app/src/main/java/com/example/liveroom
├── data
│   ├── factory          # фабрика Coil ImageLoader
│   ├── local            # DataStore, TokenManager, WebSocketManager
│   ├── model            # локальные модели и события
│   ├── remote
│   │   ├── api          # Retrofit API interfaces и interceptor
│   │   └── dto          # DTO-модели для REST API
│   ├── repository       # repositories для auth, user, server, token
│   └── webrtc           # WebRTC manager и состояние звонков
├── di                   # Hilt-модули и конфигурация приложения
├── ui
│   ├── components       # общие UI-компоненты
│   ├── navigation       # NavigationGraph и routes
│   ├── theme            # тема, цвета, типографика
│   ├── view             # Compose-экраны
│   └── viewmodel        # ViewModel-классы
└── util                 # валидация и обработка ошибок
```

## Основные экраны

### Авторизация и регистрация

Пользователь может создать аккаунт или войти в существующий. После успешной авторизации приложение сохраняет данные сессии и открывает основной экран.

Используемые компоненты:

- `LoginView`
- `RegistrationView`
- `AuthViewModel`
- `AuthRepository`
- `AuthService`
- `TokenManager`

### Главный экран

Основная часть приложения объединяет домашний экран, профиль, приглашения, список серверов, каналы, чат и аналитику.

Используемые компоненты:

- `MainView`
- `MainLayout`
- `HomeComponent`
- `Profile`
- `Invites`
- `ServerComponent`
- `ChatScreen`
- `AnalyticsScreen`

### Профиль пользователя

Экран профиля позволяет просматривать данные пользователя, редактировать профиль и обновлять аватар.

### Серверы и приглашения

Пользователь может создавать учебные серверы, редактировать их, загружать аватар, приглашать других пользователей, принимать входящие приглашения и присоединяться по invite-token.

### Чаты и каналы

Внутри сервера пользователь может работать с каналами и сообщениями. Поддерживаются загрузка истории сообщений, отправка новых сообщений, редактирование и удаление.

### Голосовые занятия

Для голосовых занятий используется WebRTC. WebSocket применяется для обмена signaling-событиями: offer, answer, ICE candidates, события старта и завершения звонка.

### Аналитика

Приложение отображает данные по учебным сессиям, деталям сессии, периодной статистике и активности пользователей.

## Сетевое взаимодействие

REST API настроен через Retrofit и OkHttp.

Базовый URL backend-сервера находится в файле:

```kotlin
app/src/main/java/com/example/liveroom/di/AppConfig.kt
```

```kotlin
object AppConfig {
    const val BASE_URL = "https://nighthunting23.ru/"
    const val IMAGE_BASE_URL = "https://nighthunting23.ru"
}
```

Для изменения адреса сервера достаточно заменить значения `BASE_URL` и `IMAGE_BASE_URL`.

Основные API-интерфейсы:

- `AuthService` — регистрация, вход, обновление токена;
- `UserApiService` — профиль, logout, аватар пользователя;
- `ServerApiService` — серверы, приглашения, каналы, сообщения и аналитика.

## Авторизация и хранение токенов

Токены и пользовательские данные сохраняются в DataStore Preferences. За это отвечают:

- `DataStoreManager`
- `TokenManager`
- `TokenRefreshInterceptor`

`TokenRefreshInterceptor` добавляет access token к запросам и используется в сетевом слое вместе с OkHttp.

Локально сохраняются:

- access token;
- refresh token;
- id пользователя;
- nickname;
- состояние `remember me`.

## Realtime через WebSocket

Realtime-взаимодействие реализовано в `WebSocketManager`.

Используется подключение к:

```text
wss://nighthunting23.ru/ws
```

WebSocketManager выполняет:

- подключение с передачей JWT-токена;
- отправку STOMP `CONNECT` frame;
- подписку на нужные topics;
- повторную подписку после reconnect;
- heartbeat;
- автоматические попытки переподключения;
- отправку сообщений через STOMP `SEND` frame;
- получение realtime-событий для чатов и звонков.

## WebRTC-звонки

Голосовые вызовы реализованы через `WebRtcManager` и `CallStateManager`.

`WebRtcManager` отвечает за:

- инициализацию `PeerConnectionFactory`;
- создание локального audio track;
- создание offer/answer;
- установку remote/local description;
- обмен ICE candidates;
- управление peer connections;
- освобождение ресурсов звонка.

Для ICE используется публичный STUN-сервер:

```text
stun:stun.l.google.com:19302
```

## Разрешения Android

В `AndroidManifest.xml` используются следующие разрешения:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
```

Назначение разрешений:

- `INTERNET` — сетевые запросы и WebSocket;
- `ACCESS_NETWORK_STATE` — проверка сетевого состояния;
- `RECORD_AUDIO` — голосовые звонки через WebRTC;
- media/storage permissions — выбор и загрузка изображений профиля или сервера.

## Требования для запуска

- Android Studio;
- JDK 17 или совместимый с используемой версией Android Gradle Plugin;
- Android SDK;
- устройство или эмулятор с Android 8.0+;
- доступ к backend-серверу LiveRoom.

Параметры проекта:

```text
minSdk: 26
targetSdk: 36
compileSdk: 36
Kotlin: 2.1.0
Android Gradle Plugin: 8.11.2
```

## Запуск проекта

1. Склонировать репозиторий:

```bash
git clone <repository-url>
```

2. Открыть проект в Android Studio.

3. Дождаться Gradle Sync.

4. Проверить адрес backend-сервера в `AppConfig.kt`.

5. Запустить приложение на эмуляторе или физическом устройстве.

Также можно собрать debug-версию через Gradle:

```bash
./gradlew assembleDebug
```

Для Windows:

```bash
gradlew.bat assembleDebug
```

## Важные замечания

- Для полноценной работы приложения требуется доступный backend-сервер.
- Для голосовых звонков необходимо разрешение на использование микрофона.
- Для загрузки изображений необходимо разрешение на доступ к медиафайлам в зависимости от версии Android.
- URL backend-сервера сейчас задан напрямую в `AppConfig.kt`.
- Проект использует DataStore для локального хранения пользовательских данных. Room в текущей версии проекта не используется.

## Возможные направления развития

- добавить защищённое хранение токенов через Android Keystore;
- вынести URL сервера в build config или product flavors;
- добавить unit-тесты для ViewModel и repository;
- добавить UI-тесты для основных пользовательских сценариев;
- улучшить обработку ошибок сети и WebSocket-соединения;
- добавить offline-кэширование сообщений;
- расширить аналитику по образовательным сессиям;
- добавить поддержку push-уведомлений;
- доработать права доступа и роли на клиентской стороне.

## Автор

**Никита Плотников**

Дипломный проект по направлению «Программная инженерия».
