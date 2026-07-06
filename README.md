# SportBuddy

[![CI](https://github.com/IVedutin/SportBuddy/actions/workflows/ci.yml/badge.svg)](https://github.com/IVedutin/SportBuddy/actions/workflows/ci.yml)

SportBuddy is a website where you can find friends who share your hobbies.

## Stack

Java 21 · Spring Boot 3.5 · Spring Security (BCrypt, roles USER/PREMIUM/ADMIN) ·
Spring Data JPA · PostgreSQL · Thymeleaf · gRPC client to GigaChat.

## Configuration

Secrets are **not** stored in the repo. `application.properties` reads them from
environment variables. Copy the template and fill in real values:

```bash
cp .env.example .env      # then edit .env
```

Required variables are listed in [.env.example](.env.example):
`SPRING_DATASOURCE_*`, `SPRING_MAIL_*`, `GIGACHAT_*`, `POSTGRES_*`.

## Run with Docker

```bash
docker compose up -d                     # PostgreSQL + backend (http://localhost:8080)
docker compose --profile frontend up -d  # also the frontend (needs ../sportbuddy-hub)
```

## Tests & coverage

```bash
./mvnw test
```

Unit tests run with JUnit 5 + Mockito; integration tests boot a real PostgreSQL
via Testcontainers (Docker required). A JaCoCo coverage report is written to
`target/site/jacoco/index.html`.

## Load test

A [k6](https://k6.io) script lives in [load-test/script.js](load-test/script.js):

```bash
k6 run load-test/script.js
```
