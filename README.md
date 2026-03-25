# LearningCollectionAPI

A Spring Boot REST API for managing a personal collection of learning notes, built with Kotlin.

Created by [MenkeTechnologies](https://github.com/MenkeTechnologies)

## Tech Stack

- **Kotlin** + **Spring Boot 4.0.4**
- **Spring Data JPA** with Hibernate (MySQL)
- **Spring Data REST** for auto-generated CRUD endpoints
- **QueryDSL** for type-safe queries
- **SpringDoc OpenAPI** for API documentation
- **Gradle 9.2.1**

## Getting Started

### Prerequisites

- JDK 17+
- MySQL running on `localhost:3306`

### Run

```bash
./gradlew bootRun
```

The server starts on port **8000** (dev profile).

### Build

```bash
./gradlew build        # compile + test
./gradlew bootJar      # executable JAR
./gradlew bootBuildImage  # OCI Docker image
```

## API Endpoints

### Add & Search

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/add?learning=<text>` | Add a new learning item |
| GET | `/filter?learning=<text>` | Search items containing query text |

### Recent Items

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/recents` | Last 20 items (short format) |
| GET | `/recents/{count}` | Last N items (short format) |
| GET | `/recent/{count}` | Last N items (full format) |

### Random Items

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/random` | One random item (full format) |
| GET | `/randoms` | One random item (short format) |
| GET | `/randoms/{count}` | N random items (short format) |
| GET | `/random/{count}` | N random items (full format) |

### Utility

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dump` | MySQL database dump |
| — | `/learning` | Auto-generated CRUD (Spring Data REST) |

## Project Structure

```
src/main/kotlin/com/menketechnologies/learningcollection/
  LearningCollectionApplication.kt   # Entry point
  LearningController.kt              # REST endpoints
  LearningCollection.kt              # JPA entity (id, learning, category, dateAdded)
  LCRepo.kt                          # Spring Data repository
  Consts.kt                          # Constants
  WebConfig.kt                       # CORS & REST config
```

## Testing

```bash
./gradlew test
```

Comprehensive test suite with 16 test files covering unit tests, integration tests, boundary/edge cases, stress tests, serialization, and property-based tests.

## Configuration

Spring profiles are available for different environments:

| Profile | File | Notes |
|---------|------|-------|
| default | `application.properties` | MySQL localhost, SQL logging off |
| dev | `application-dev.properties` | Port 8000, SQL logging on |
| sr5 | `application-sr5.properties` | Production-like config |
