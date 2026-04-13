# Ktor Многомодульный Backend

Масштабируемый backend-сервис с чистой архитектурой, написанный на Kotlin и Ktor.

## Стек Технологий
- **Ktor** (Веб Фреймворк)
- **PostgreSQL** (База данных)
- **Exposed** (ORM) & **Flyway** (Миграции БД)
- **Redis** (Кэширование)
- **RabbitMQ** (Очередь сообщений)
- **JWT** (Аутентификация)
- **Koin** (Внедрение зависимостей - DI)
- **Swagger UI** (Документация API)
- **Testcontainers & JUnit 5** (Тестирование)

## Функционал
- JWT Аутентификация (Регистрация / Вход)
- Управление товарами
- Управление заказами (Обеспечение целостности транзакций, списание стоков)
- Асинхронные сообщения (Слушатель RabbitMQ (воркер), который логирует события)
- Redis Кэш для быстрого получения данных о товарах
- Чистая Архитектура (Модули Domain, Service, Data, API)

## Запуск локально (Docker)

Убедитесь, что у вас установлены Docker и Docker Compose.

```bash
# Эта команда поднимет приложения Ktor, PostgreSQL, Redis и RabbitMQ
docker-compose up --build
```
После успешного запуска, документация Swagger API будет доступна по адресу:  
`http://localhost:8080/swagger-ui`

## Деплой в облако (Railway / Render / DigitalOcean)

**Подготовка**:
1. Запушьте данный проект в репозиторий GitHub.
2. Войдите в Railway ([railway.app](https://railway.app/)) или Render ([render.app](https://render.com/)).

**Деплой через Docker**:
1. На Railway/Render выберите "New Project" -> "Deploy from GitHub repo".
2. Выберите этот репозиторий.
3. Платформа должна автоматически найти файл `Dockerfile` и собрать образ.
4. **Настройте Базы данных:**
    - Разверните PostgreSQL.
    - Разверните Redis.
    - Разверните RabbitMQ (либо используйте внешний сервис, например CloudAMQP).
5. **Настройте переменные окружения** проекта в соответствии с базами:
    - `DB_URL` = `jdbc:postgresql://<host>:<port>/<dbname>`
    - `DB_USER` = `<username>`
    - `DB_PASSWORD` = `<password>`
    - `REDIS_HOST` = `<redis-host>`
    - `REDIS_PORT` = `<redis-port>`
    - `RABBITMQ_HOST` = `<rabbitmq-host>`
    - `RABBITMQ_PORT` = `<rabbitmq-port>`
6. Сохраните и разверните (Deploy)! Сервис автоматически выполнит миграции Flyway и безопасно запустится.

## Обзор API

Для получения данных о параметрах и ответах API, воспользуйтесь страницей `http://localhost:8080/swagger-ui`.

* `POST /auth/register` (Открыто)
* `POST /auth/login` (Открыто)
* `GET /products` (Открыто)
* `GET /products/{id}` (Открыто)
* `POST /products` (Только Admin)
* `PUT /products/{id}` (Только Admin)
* `DELETE /products/{id}` (Только Admin)
* `POST /orders` (Только авторизованные User)
* `GET /orders` (Только авторизованные User)
* `DELETE /orders/{id}` (Только авторизованные User)
* `GET /stats/orders` (Только Admin)
