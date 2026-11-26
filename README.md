# Jobflow: Job Application Tracker API

REST API for tracking job applications: add a vacancy, follow its status through the hiring process, and keep your data private per user.

## Features

- Registration and login with JWT (BCrypt-hashed passwords)
- CRUD for job applications with statuses: `APPLIED`, `INTERVIEW`, `REJECTED`, `OFFER`
- Filtering by status, pagination and sorting
- Data isolation: a user can only see and change their own applications
- Request validation with field-level error messages
- Unit tests (JUnit 5, Mockito)
- Database migrations with Flyway

## Tech stack

Java 21, Spring Boot 4, Spring Security, Spring Data JPA (Hibernate), PostgreSQL 17, JWT (jjwt), Docker Compose, JUnit 5, Mockito

## Getting started

Requirements: JDK 21, Docker.

1. Create your local config files from the examples:
    - `docker-compose.yml.example` -> `docker-compose.yml`
    - `src/main/resources/application.properties.example` -> `src/main/resources/application.properties`

   Set the database password (the same value in both files) and a long random `jwt.secret` (at least 32 characters). Both real files are git-ignored.

2. Start PostgreSQL:

   ```
   docker-compose up -d
   ```

3. Run the application:

   ```
   mvnw.cmd spring-boot:run      # Windows
   ./mvnw spring-boot:run        # Linux / macOS
   ```

The API is available at `http://127.0.0.1:8080`.

## API

| Method | Endpoint | Description | Success |
|---|---|---|---|
| POST | `/api/auth/register` | Register, returns a JWT | 201 |
| POST | `/api/auth/login` | Log in, returns a JWT | 200 |
| POST | `/api/applications` | Create an application | 201 |
| GET | `/api/applications` | List own applications (`?status=INTERVIEW&page=0&size=10&sort=appliedDate,desc`) | 200 |
| GET | `/api/applications/{id}` | Get one application | 200 |
| PUT | `/api/applications/{id}` | Update an application | 200 |
| DELETE | `/api/applications/{id}` | Delete an application | 204 |

All `/api/applications` endpoints require the header `Authorization: Bearer <token>`.

Example request body for creating an application:

```json
{
  "companyName": "Kaspi",
  "jobTitle": "Java Intern",
  "jobUrl": "https://example.com/job/1",
  "appliedDate": "2026-11-20",
  "notes": "Applied via the company website"
}
```

## Design decisions

- **401 vs 403:** a missing or invalid token returns 401.
- **Foreign resources return 404, not 403**, so the API does not reveal which application ids exist.
- **Login errors are identical** for an unknown email and a wrong password, so registered emails cannot be enumerated.
- **DTOs** separate the API from database entities; a global exception handler produces consistent error responses.
- **Stateless authentication:** no server-side sessions, every request carries its JWT.

## Tests

```
mvnw.cmd test -Dtest="JwtUtilTest,JobApplicationServiceTest"
```

## Roadmap

- Integration tests with Testcontainers
- Statistics endpoint (applications per status, interview and offer rates)
- Resume storage and upload
- AI-based resume-to-vacancy matching and missing-skills suggestions
- Interview reminders