# Reyga Starter Foundation

Library modular untuk standardisasi backend service berbasis Spring, dengan fokus pada:
1. konsistensi response dan error handling,
2. reusable service flow,
3. utilitas JDBC/query builder,
4. logging terstruktur dan terkonfigurasi.

Repository: https://github.com/ReygaFitra/reyga-starter-foundation

## Tech Stack

- Java 25
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
- operator predikat bertipe aman (`QueryOperator`) untuk WHERE dan JOIN/ON
- query execution abstraction (`QueryProcessor`)
- pagination berbasis Spring Data (`Page`, `Pageable`, `PageImpl`)
- mapper util (`JdbcResultSetMapper`)
- annotation mapping kolom (`@MapperColumn`) + processor

Dependency internal:
- bergantung pada module `common`

Panduan implementasi:
- [JDBC Repository Implementation Guide — 1.0.0](docs/VER1.0.0/%281.0.0%29%20REPOSITORY_IMPLEMENTATION_GUIDE.md)
- [Service Implementation Guide — 1.0.0](docs/VER1.0.0/%281.0.0%29%20SERVICE_IMPLEMENTATION_GUIDE.md)
- [Panduan Implementasi Controller — 1.0.0](docs/VER1.0.0/%281.0.0%29%20CONTROLLER_IMPLEMENTATION_GUIDE.md)

### `core`
Tujuan: kerangka utama service layer, validasi, dan error handling.

Spesifikasi utama:
- base service abstraction (`BaseService`, `FoundationService`)
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
- konfigurasi `META-INF/logback-spring.xml` dan filter konkret tersedia dalam
  artifact `logging`, sehingga inisialisasi Logback tidak memerlukan starter internal
- async logging config + MDC-aware executor
- properties-based logging configuration

Dependency internal:
- bergantung pada module `common`

## Panduan Konfigurasi

- [Foundation Configuration Guide — 1.0.0](docs/VER1.0.0/%281.0.0%29%20CONFIGURATION_GUIDE.md) - feature flag,
  default bean, validation, exception handler, logging, async executor,
  resilience, contoh YAML, dan troubleshooting.

## Versioning Dokumentasi

Dokumentasi mengikuti versi library. Gunakan panduan yang sesuai dengan versi
artifact yang digunakan project, karena kontrak API, konfigurasi, dan perilaku
fitur dapat berbeda antarversi.

- Folder dokumentasi menggunakan pola `docs/VER<versi>/`.
- Nama file menggunakan pola `(<versi>) NAMA_GUIDE.md`.
- Dokumentasi yang tersedia saat ini adalah [versi 1.0.0](docs/VER1.0.0/).
- Untuk rilis berikutnya, tambahkan dokumentasi pada folder versi rilis tersebut
  dan perbarui indeks serta tautan di README. Pertahankan dokumentasi versi
  sebelumnya sebagai referensi pengguna versi lama.
- Setiap perubahan class, interface, enum, record, konfigurasi, atau perilaku
  publik harus disertai pembaruan dokumentasi yang terdampak pada versi terkait.

Nomor versi dokumentasi diselaraskan dengan versi rilis library yang dikontrol
melalui `projectVersion` di `gradle.properties`.

## Dependency Antar Module

- `common`: base module
- `common-database` -> `common`
- `core` -> `common`
- `logging` -> `common`

## Build & Publish

Semua module sudah dikonfigurasi sebagai Java library + Maven publish:
- `common`
- `common-database`
- `common-io`
- `core`
- `logging`

Module source `foundation-starter-internal` menyediakan implementasi default dan
auto-configuration. Hasil build-nya dipublish sebagai artifact `foundation-starter`,
bukan sebagai artifact `foundation-starter-internal`. JAR ini membawa bytecode
implementasi dan resource auto-configuration; dependency API dan pihak ketiga
tetap didistribusikan melalui metadata dependency, bukan digabungkan ke dalam JAR.
Source dan Javadoc JAR juga diterbitkan sesuai konfigurasi module.

Koordinat artifact:
- `group`: `com.reyga-dev.starter`
- `version`: dikontrol terpusat via `gradle.properties` (`projectVersion`)

### Menggunakan implementasi default

Pertahankan dependency module API yang digunakan aplikasi pada scope compile.
Tambahkan distribusi implementasi sebagai dependency runtime:

```xml
<dependency>
    <groupId>com.reyga-dev.starter</groupId>
    <artifactId>foundation-starter</artifactId>
    <version>1.0.0</version>
    <scope>runtime</scope>
</dependency>
```

Dengan demikian, aplikasi menggunakan kontrak seperti `QueryProcessor` saat
compile, sementara Spring memuat `DefaultQueryProcessor`, `CommonDatabaseConfig`,
dan `CoreConfig` saat runtime. Scope ini bukan mekanisme untuk menyembunyikan
bytecode. Selaraskan versi starter dengan seluruh module API.

Starter existing memuat seluruh auto-configuration: datasource/JdbcTemplate tetap
diperlukan untuk database, dan fitur opt-in mengikuti flag konfigurasi masing-masing.
Untuk output logging bawaan, tetap set
`logging.config=classpath:META-INF/logback-spring.xml`.

Untuk publikasi lokal semua artifact (termasuk dependency API):

```bash
./gradlew publishToMavenLocal
```

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
