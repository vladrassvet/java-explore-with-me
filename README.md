# java-explore-with-me

Дипломный проект.

Приложение ExploreWithMe предоставляет возможность пользователям делиться информацией об интересных событиях и находить компанию для участия в них.

---
- Приложение позволяет авторизованным пользователям (***users***) создавать мероприятия (a.k.a. события) ([***events***](#Events))
- Для события должна быть указана категория (***category***). Категории создаются и редактируются админами. Просматривать категории может любой пользователь.
- События могут быть сгруппированы по подборкам событий (***compilations***). Одно событие может входить в несколько подборок.
- Только что созданное мероприятие должно быть одобрено админом и только тогда оно становится видно пользователям.

- Авторизованные пользователи могут создавать запросы на участие в мероприятиях ([***requests***](#Requests)).
В зависимости от настроек мероприятия запросы на участие могут требовать одобрения со стороны автора мероприятия, либо  одобряться автоматически.

API сервиса разделено на три части:

1. **Public** - доступно без регистрации для любого пользователя
2. **Private** - доступно только для авторизованных пользователей
3. **Admin** - для администраторов сервиса

# Endpoints

## Подборки событий

### Public

- :one: GET /compilations
- :two: GET /compilations/{compId}

### Admin

- :one: POST /admin/compilations
- :two: PATCH /admin/compilations/{compId}
- :three: DELETE /admin/compilations/{compId}

## Категории событий

### Public

- :one: GET /categories
- :two: GET / categories/{compId}

### Admin

- :one: POST /admin/categories
- :two: PATCH /admin/categories/{catId}
- :three: DELETE /admin/categories/{catId}

## Events (мероприятия)

### Public : events

- :one: GET /events
- :two: GET /events/{id}

### Private : events

- :one: GET /users/{userId}/events
- :two: POST /users/{userId}/events
- :three: GET /users/{userId}/events/{eventsId}
- :four: PATCH /users/{userId}/events/{eventId}
- :five: GET /users/{userId}/events/{eventId}/requests
- :six: PATCH /users/{userId}/events/{eventId}/requests

### Admin : events

- :one: GET /admin/events
- :two: PATCH /admin/events/{eventId}

## Пользователи

### Admin : users

- :one: GET /admin/users
- :two: POST /admin/users
- :three: PATCH /admin/users/{userId}

## Запросы на участие в событиях(#Requests)

### Private : requests

- :one: GET /users/{userId}/requests
- :two: POST /users/{userId}/requests
- :three: PATCH /users/{userId}/requests/{requestId}/cancel

## Комментарии

### Public

- :one: GET /events/{eventId}/comments

### Private

- :one: POST /users/{userId}/comments/events/{eventId}
- :two: GET /users/{userId}/comments/{id}
- :three: GET /users/{userId}/comments/
- :four: PATCH /users/{userId}/comments/{id}
- :five: DELETE /users/{userId}/comments/{id}

### Admin

- :one: GET /events/{eventId}/comments
