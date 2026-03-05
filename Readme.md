# Reyga Starter Foundation

Library modular untuk standardisasi backend service berbasis Spring, dengan fokus pada:
1. konsistensi response dan error handling,
2. reusable service flow,
3. utilitas JDBC/query builder,
4. logging terstruktur dan terkonfigurasi.

Repository: https://github.com/ReygaFitra/reyga-starter-foundation

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Framework 7
- Gradle Kotlin DSL (multi-module)
- Spring JDBC / JdbcTemplate
- Resilience4j
- Logback + Janino

## Struktur Module

### `common`
Tujuan: fondasi utilitas umum lintas module.

Spesifikasi utama:
- shared config (`CommonConfig`)
- enum standar service/header/response
- helper util (`DateUtil`, `MapperUtil`, `ServiceUtil`)
- base logging helper (`CustomLogger`, `HttpBuilder`, dll)
- DTO request logging

### `common-database`
Tujuan: helper akses database berbasis JDBC dengan pattern reusable.

Spesifikasi utama:
- `BaseJdbcRepository` sebagai base repository
- dynamic SQL builder (`QueryBuilder`, `QueryFunction`)
- query execution abstraction (`QueryProcessor`)
- mapper util (`JdbcResultSetMapper`)
- annotation mapping kolom (`@MapperColumn`) + processor

Dependency internal:
- bergantung pada module `common`

### `core`
Tujuan: kerangka utama service layer, validasi, dan error handling.

Spesifikasi utama:
- base service abstraction (`BaseService`, `FoundationService`, builder variant)
- process pipeline/builder (`ProcessBuilder`, `ProcessNode`, `IfContext`)
- base controller + response builder
- base request/response/content DTO
- custom validation annotation + processor
- global/default exception handler
- transactional executor abstraction
- konfigurasi properti core

Dependency internal:
- bergantung pada module `common`

### `logging`
Tujuan: logging terstruktur untuk request/response dan eksekusi method.

Spesifikasi utama:
- AOP logging (`AspectLogging`, `@AspectLogExecution`)
- interceptor logging HTTP
- console/rolling/summary log filters
- async logging config + MDC-aware executor
- properties-based logging configuration

Dependency internal:
- bergantung pada module `common`

## Dependency Antar Module

- `common`: base module
- `common-database` -> `common`
- `core` -> `common`
- `logging` -> `common`

## Build & Publish

Semua module sudah dikonfigurasi sebagai Java library + Maven publish:
- `common`
- `common-database`
- `core`
- `logging`

Koordinat artifact:
- `group`: `com.reyga-dev.starter`
- `version`: dikontrol terpusat via `gradle.properties` (`projectVersion`)

## Menjalankan Build

```bash
./gradlew build
```

Untuk melihat struktur project:

```bash
./gradlew projects
```

## Catatan Konfigurasi Versi

Semua versi dependency/plugin dipusatkan di `gradle.properties`, termasuk:
- `springBootVersion`
- `springBootPluginVersion`
- `javaVersion`
- versi library pendukung lain (lombok, resilience4j, caffeine, logback, dll)
