# Суть проекта
Система брони номеров в отеле: пользователи могут бронировать вакантные номера
на определённые дни, делая их занятыми.

# Структура
- В `materials/` находится дамп БД и другие полезные файлы.

Весь код находится в `src/main/java/lab/booking`:
- Взаимодействие с БД происходит через репозитории в `repositories/`, сцепленные вместе через
`BookingService`, всё взаимодействие с которым происходит через API-контроллер `BookingController`
- Модели из БД находятся в `models/`
- Кастомные исключения в `exceptions/`
- Тесты в `src/test/...`

# Запуск проекта
#### Инициализировать БД:
- Зайти в инструмент `Database` в Ij Idea
- Нажать `+` -> `Data Source` -> `PostgreSQL`
- Подключиться к своему серверу Postgres
- На странице инструмента `Database` нажать `ПКМ` -> `SQL Scripts` -> `Run SQL Script` -> выбрать
`materials/create_db_schema.sql`
#### Запустить сервер:
- Запустить `BookingApplication`
- Перейти на страницу Swagger по `http://localhost:8080/swagger-ui/index.html`
- Тесты можно запустить в `BookingControllerTest`
