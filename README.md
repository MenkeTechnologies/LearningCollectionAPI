```
 ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
 ░  ▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄  ░
 ░  █                                                                        █  ░
 ░  █  ██╗     ███████╗ █████╗ ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗      █  ░
 ░  █  ██║     ██╔════╝██╔══██╗██╔══██╗████╗  ██║██║████╗  ██║██╔════╝      █  ░
 ░  █  ██║     █████╗  ███████║██████╔╝██╔██╗ ██║██║██╔██╗ ██║██║  ███╗     █  ░
 ░  █  ██║     ██╔══╝  ██╔══██║██╔══██╗██║╚██╗██║██║██║╚██╗██║██║   ██║     █  ░
 ░  █  ███████╗███████╗██║  ██║██║  ██║██║ ╚████║██║██║ ╚████║╚██████╔╝     █  ░
 ░  █  ╚══════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝  █  ░
 ░  █                                                                        █  ░
 ░  █   ██████╗ ██████╗ ██╗     ██╗     ███████╗ ██████╗████████╗██╗ █████╗  █  ░
 ░  █  ██╔════╝██╔═══██╗██║     ██║     ██╔════╝██╔════╝╚══██╔══╝██║██╔══██╗ █  ░
 ░  █  ██║     ██║   ██║██║     ██║     █████╗  ██║        ██║   ██║██║  ██║ █  ░
 ░  █  ██║     ██║   ██║██║     ██║     ██╔══╝  ██║        ██║   ██║██║  ██║ █  ░
 ░  █  ╚██████╗╚██████╔╝███████╗███████╗███████╗╚██████╗   ██║   ██║╚█████╔╝ █  ░
 ░  █   ╚═════╝ ╚═════╝ ╚══════╝╚══════╝╚══════╝ ╚═════╝   ╚═╝   ╚═╝╚════╝  █  ░
 ░  █                                                                        █  ░
 ░  █▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄█  ░
 ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
         ▓▓▓ NEURAL KNOWLEDGE REPOSITORY // API v4.0.6 // JACKED INTO JVM ▓▓▓
```

<div align="center">

[![CI](https://github.com/MenkeTechnologies/LearningCollectionAPI/actions/workflows/ci.yml/badge.svg)](https://github.com/MenkeTechnologies/LearningCollectionAPI/actions/workflows/ci.yml)

### `// NEURAL KNOWLEDGE REPOSITORY v4.0.6`

> _A Spring Boot REST API for managing a personal collection of learning notes._
> _Built with Kotlin. Jacked into the JVM._

**`[ CREATED BY ]`** [MenkeTechnologies](https://github.com/MenkeTechnologies)

---

```
╔══════════════════════════════════════════════════════════╗
║  STATUS: ONLINE  //  PORT: 8000  //  PROFILE: dev       ║
╚══════════════════════════════════════════════════════════╝
```

</div>

---

## `> SYSTEM_SPECS.dat`

```
┌──────────────────────────────────────────┐
│  RUNTIME    :: Kotlin 2.3.20 + Spring Boot 4.0.4 │
│  DATASTORE  :: MySQL via Spring Data JPA         │
│  INTERFACE  :: Spring Data REST (CRUD)           │
│  QUERY_ENG  :: QueryDSL 5.1.0 (type-safe)       │
│  API_DOCS   :: SpringDoc OpenAPI 1.8.0           │
│  BUILD_SYS  :: Gradle 9.4.1                     │
│  JDK        :: 17 (toolchain)                    │
└──────────────────────────────────────────┘
```

---

## `> BOOT_SEQUENCE.init`

### Prerequisites

```
[!] JDK 17+ implant required (toolchain locked to 17)
[!] Kotlin 2.3.20 runtime (managed by Gradle — no manual install)
[!] Gradle 9.4.1 (use included wrapper — no manual install)
[!] MySQL daemon must be live on localhost:3306
[!] Database 'root' must exist (default schema target)
```

### Jack In

```bash
# >> DEFAULT BOOT — binds to port defined in active profile <<
./gradlew bootRun

# >> BOOT WITH DEV PROFILE — port 8000, SQL logging ON <<
./gradlew bootRun --args='--spring.profiles.active=dev'

# >> BOOT WITH PRODUCTION PROFILE <<
./gradlew bootRun --args='--spring.profiles.active=sr5'
```

### Compile Firmware

```bash
./gradlew build          # compile + run diagnostics
./gradlew bootJar        # package executable construct
./gradlew bootBuildImage # forge OCI Docker container
```

### Run Compiled Artifact

```bash
# >> EXECUTE JAR DIRECTLY <<
java -jar build/libs/LearningCollectionAPI-0.0.1-SNAPSHOT.jar

# >> EXECUTE WITH SPECIFIC PROFILE <<
java -jar build/libs/LearningCollectionAPI-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

---

## `> ACCESS_PROTOCOLS.net`

### `:: DATA_INJECT & SEARCH_QUERY ::`

| Protocol | Endpoint | Function |
|:--------:|----------|----------|
| `GET` | `/add?learning=<text>` | Upload new data fragment |
| `GET` | `/filter?learning=<text>` | Query the knowledge matrix |

### `:: RECENT_MEMORY_ACCESS ::`

| Protocol | Endpoint | Function |
|:--------:|----------|----------|
| `GET` | `/recents` | Retrieve last 20 fragments (compressed) |
| `GET` | `/recents/{count}` | Retrieve last N fragments (compressed) |
| `GET` | `/recent/{count}` | Retrieve last N fragments (full decrypt) |

### `:: RANDOM_ACCESS_MEMORY ::`

| Protocol | Endpoint | Function |
|:--------:|----------|----------|
| `GET` | `/random` | Pull 1 random fragment (full decrypt) |
| `GET` | `/randoms` | Pull 1 random fragment (compressed) |
| `GET` | `/randoms/{count}` | Pull N random fragments (compressed) |
| `GET` | `/random/{count}` | Pull N random fragments (full decrypt) |

### `:: SYSTEM_UTILS ::`

| Protocol | Endpoint | Function |
|:--------:|----------|----------|
| `GET` | `/dump` | Full MySQL memory extraction |
| `---` | `/learning` | Auto-generated CRUD interface |

---

## `> DIRECTORY_MAP.sys`

```
src/main/kotlin/com/menketechnologies/learningcollection/
│
├── LearningCollectionApplication.kt ─── // MAIN CORTEX BOOTLOADER
├── LearningController.kt ───────────── // REST ENDPOINT ROUTER
├── LearningCollection.kt ───────────── // JPA ENTITY SCHEMA [id|learning|category|dateAdded]
├── LCRepo.kt ───────────────────────── // DATA ACCESS LAYER
├── Consts.kt ────────────────────────── // HARDCODED CONSTANTS
└── WebConfig.kt ─────────────────────── // CORS & REST CONFIGURATION
```

---

## `> RUN_DIAGNOSTICS.exe`

```bash
./gradlew test
# >> INITIATING NEURAL INTEGRITY SCAN <<
```

> `19 test modules online` — unit tests, integration tests, boundary analysis, stress tests, serialization checks, property-based fuzzing, repo contracts, idempotency checks, and application entry tests.

---

## `> ENV_PROFILES.cfg`

```
┌───────────┬──────────────────────────────────┬────────────────────────────┐
│  PROFILE  │  CONFIG FILE                     │  NOTES                     │
├───────────┼──────────────────────────────────┼────────────────────────────┤
│  default  │  application.properties          │  MySQL localhost, quiet    │
│  dev      │  application-dev.properties      │  Port 8000, SQL logging ON │
│  sr5      │  application-sr5.properties      │  Production-grade config   │
│  sr5-2    │  application-sr5-2.properties    │  Alt production config     │
└───────────┴──────────────────────────────────┴────────────────────────────┘
```

---

<div align="center">

```
 ░▒▓ END OF LINE ▓▒░
```

</div>
