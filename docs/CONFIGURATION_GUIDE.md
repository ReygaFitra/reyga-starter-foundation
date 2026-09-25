# Panduan Konfigurasi

Gunakan panduan ini untuk menyiapkan dependency aplikasi, memilih fitur yang
dibutuhkan, dan mengatur perilakunya melalui `application.yml` atau
`application.properties`.

Mulai dari langkah 1–3. Bagian berikutnya menjelaskan pilihan fitur, nilai default,
contoh profile, dan penyelesaian masalah penggunaan.

## A. Langkah 1: Siapkan Dependency Aplikasi

Tambahkan module API yang dipakai (`core`, `common`, `common-database`,
`common-io`, `common-http`, atau `logging`) sebagai dependency compile.
Selaraskan semuanya dengan `projectVersion` library (`1.1.0` saat ini).
Tambahkan starter berikut untuk menggunakan fitur bawaan:

```xml
<dependency>
    <groupId>com.reyga-dev.starter</groupId>
    <artifactId>foundation-starter</artifactId>
<version>1.1.0</version>
    <scope>runtime</scope>
</dependency>
```

Untuk aplikasi JDBC, sertakan `spring-boot-starter-jdbc` dan driver database
yang digunakan. Konfigurasikan datasource agar `JdbcTemplate` tersedia;
`QueryProcessor` tidak memerlukan flag aktivasi tambahan.

Contoh PostgreSQL (dependency Spring Boot mengikuti dependency management aplikasi):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Isi environment variable sesuai lingkungan aplikasi; jangan simpan password
production di repository. Starter versi ini memerlukan datasource/JdbcTemplate.
Jika memakai `utilities: true`, siapkan pula transaction manager aplikasi.

Untuk contoh implementasi request, kontrak interface, concrete service, servlet
context, dan pemanggilan `execute()` dari controller, lihat
[Service Implementation Guide](SERVICE_IMPLEMENTATION_GUIDE.md).

> Semua feature flag `reyga.config.*` menggunakan nilai default `false`, kecuali
> dinyatakan lain. Tidak ada master switch tunggal untuk seluruh library.

## B. Langkah 2: Aktifkan Fitur yang Digunakan

Gabungkan contoh berikut dengan konfigurasi datasource Anda. Contoh mengaktifkan
validasi, penanganan exception, dan logging HTTP. Ubah flag sesuai kebutuhan;
jangan membuat key YAML yang sama dua kali.

```yaml
#
# Gunakan konfigurasi Logback bawaan library
#
logging:
  config: classpath:META-INF/logback-spring.xml

reyga:
  config:
    default-bean:
      exception-handler: true
      validation-handler: true
      logging-handler: true
      http-client: false
      utilities: false
    aspect:
      around: false
      behavior: ""
      before: false
      before-behavior: ""
      after: false
      after-behavior: ""
      after-returning: false
      after-returning-behavior: ""
      after-throwing: false
      after-throwing-behavior: ""
    rate-limiter:
      required: false
    circuit-breaker:
      required: false
    retry:
      required: false
    logging:
      file:
        enable: false
        summary:
          enable: true
        async-logs:
          core-pool-size: 5
          max-pool-size: 10
          queue-capacity: 100
          thread-name: async-logging-
```

Jika tidak menggunakan fitur opt-in, bagian `reyga.config` boleh dihilangkan.
Prasyarat dependency dan datasource tetap berlaku.

## C. Langkah 3: Pilih Panduan Implementasi

- [Service](SERVICE_IMPLEMENTATION_GUIDE.md): DTO, validasi, proses bisnis, dan servlet context.
- [Controller](CONTROLLER_IMPLEMENTATION_GUIDE.md): pemanggilan `execute()`, response, dan rate limiter.
- [Repository](REPOSITORY_IMPLEMENTATION_GUIDE.md): interface repository, query, dan pagination.

## D. Ringkasan Aktivasi Fitur

| Fitur | Property | Default | Dampak ketika aktif                                                                                                        |
|---|---|---:|----------------------------------------------------------------------------------------------------------------------------|
| Global exception handler | `reyga.config.default-bean.exception-handler` | `false` | Menangani exception umum dan database dengan format error standar.                                                  |
| Programmatic validation | `reyga.config.default-bean.validation-handler` | `false` | Mengaktifkan validasi programmatic melalui injection `ValidationUtility`. |
| HTTP request/response logging | `reyga.config.default-bean.logging-handler` | `false` | Mencatat request dan response HTTP.                                                          |
| Default HTTP client | `reyga.config.default-bean.http-client` | `false` | Mendaftarkan reusable `OkHttpClient` dan `StarterHttpClient`. |
| Default utilities | `reyga.config.default-bean.utilities` | `false` | Mendaftarkan `ResilienceService` dan `TransactionalExecutor`.                                                              |
| Around aspect | `reyga.config.aspect.around` | `false` | Mendaftarkan aspect untuk method beranotasi `@AroundExecution`.                                                            |
| Around behavior | `reyga.config.aspect.behavior` | string kosong | Nama bean turunan `BaseAspectAround` yang digunakan ketika around aspect aktif.                                            |
| Before aspect | `reyga.config.aspect.before` | `false` | Mendaftarkan aspect untuk method beranotasi `@BeforeExecution`.                                                            |
| Before behavior | `reyga.config.aspect.before-behavior` | string kosong | Nama bean turunan `BaseAspectBefore` yang digunakan ketika before aspect aktif.                                            |
| After aspect | `reyga.config.aspect.after` | `false` | Mendaftarkan aspect untuk method beranotasi `@AfterExecution`.                                                             |
| After behavior | `reyga.config.aspect.after-behavior` | string kosong | Nama bean turunan `BaseAspectAfter` yang digunakan ketika after aspect aktif.                                              |
| After Returning aspect | `reyga.config.aspect.after-returning` | `false` | Mendaftarkan aspect untuk method beranotasi `@AfterReturningExecution`.                                                    |
| After Returning behavior | `reyga.config.aspect.after-returning-behavior` | string kosong | Nama bean turunan `BaseAspectAfterReturning` yang digunakan ketika advice aktif.                                           |
| After Throwing aspect | `reyga.config.aspect.after-throwing` | `false` | Mendaftarkan aspect untuk method beranotasi `@AfterThrowingExecution`.                                                     |
| After Throwing behavior | `reyga.config.aspect.after-throwing-behavior` | string kosong | Nama bean turunan `BaseAspectAfterThrowing` yang digunakan ketika advice aktif.                                            |
| Rate limiter | `reyga.config.rate-limiter.required` | `false` | Mendaftarkan default `RateLimiterConfig` dan `RateLimiterRegistry`.                                                        |
| Circuit breaker | `reyga.config.circuit-breaker.required` | `false` | Mendaftarkan default `CircuitBreakerConfig` dan `CircuitBreakerRegistry`.                                                  |
| Retry | `reyga.config.retry.required` | `false` | Mendaftarkan default `RetryConfig` dan `RetryRegistry`.                                                                    |
| Summary log file | `reyga.config.logging.file.summary.enable` | `true` | Mengaktifkan appender file khusus summary pada Logback.                                                                    |
| Rolling application log | `reyga.config.logging.file.enable` | `false` | Mengaktifkan rolling file appender untuk log aplikasi.                                                                     |
| Async logging executor | Tidak memiliki flag | selalu dibuat | Mendaftarkan bean bernama `asyncExecutor` yang meneruskan MDC.                                                             |

## E. Default Exception Handler

### Global handler

```yaml
reyga:
  config:
    default-bean:
      exception-handler: true
```

Aktifkan flag ini untuk mengembalikan response error standar:

- `IllegalArgumentException` sebagai HTTP `400`, kode `91`, dan pesan
  `GENERAL ERROR`;
- `JpaSystemException` dan `DataAccessException` sebagai HTTP `500`, kode `99`,
  dan pesan `DATABASE ERROR`;
- exception lain sebagai HTTP `500`, kode `99`, dan pesan
  `INTERNAL SERVER ERROR`.

Nonaktifkan property ini jika aplikasi memiliki global exception handler sendiri.
Hindari dua handler untuk exception yang sama agar pemilihan handler dan format
response konsisten.

### Handler validasi dan application fault

Penanganan application fault tetap tersedia tanpa
mengaktifkan kedua flag tersebut. Perilakunya:

- `AppFaultException` sesuai status, kode, dan pesan dari `AppFaultContent`;
- `IOFaultException` sebagai HTTP `400` dengan metadata file yang aman untuk
  response.

Dengan demikian, `exception-handler: false` hanya menonaktifkan handler global dan
database; handler application fault tetap tersedia. Handler error validasi HTTP
mengikuti flag `validation-handler`.

## F. Mengaktifkan Validasi

Aktifkan `reyga.config.default-bean.validation-handler=true` untuk menyediakan
bean validasi bawaan. Pilihan `reyga.custom.validation.map-details` mengatur
detail error (default `true`). Jika aplikasi sudah menyediakan bean
`ValidationUtility`, bean manual diprioritaskan.

Tutorial bean manual/YAML, validation groups, dan anotasi field tersedia di
[Panduan Utilities](UTILITIES_GUIDE.md).

## G. HTTP Request/Response Logging

### Konfigurasi wajib untuk custom logging library

Project yang menggunakan custom logging bawaan library **wajib** memilih
konfigurasi Logback library melalui `logging.config` di `application.yml` atau
`application.properties`. Ketentuan ini berlaku saat memakai module `logging`
secara mandiri ataupun tidak.

Contoh `application.yml`:

```yaml
#
# Gunakan konfigurasi Logback bawaan library
#
logging:
  config: classpath:META-INF/logback-spring.xml
```

Atau, jika project menggunakan `application.properties`:

```properties
# Gunakan konfigurasi Logback bawaan library
logging.config=classpath:META-INF/logback-spring.xml
```

Pilih salah satu format sesuai konfigurasi project. Jika sudah ada blok `logging`
di YAML, tambahkan `config` ke blok tersebut tanpa membuat key `logging` duplikat.
Pastikan artifact `logging` tersedia pada runtime classpath.

Mengaktifkan `reyga.config.default-bean.logging-handler` atau mengisi
`reyga.config.logging.*` saja tidak memilih file XML ini. `logging.config`
menentukan konfigurasi appender, pattern, dan filter bawaan library yang dimuat;
`logging-handler` mengaktifkan pengumpulan log HTTP. Jika aplikasi sengaja memakai
XML Logback sendiri, arahkan `logging.config` ke XML tersebut dan konfigurasi
appender/filter foundation yang diperlukan seperti dijelaskan di bawah.

### Penggunaan module logging secara mandiri

Gunakan module `logging` dan konfigurasi berikut jika hanya membutuhkan output
console tanpa file rolling atau summary.

Untuk memakai konfigurasi bawaan pada aplikasi Spring Boot:

```yaml
logging:
  config: classpath:META-INF/logback-spring.xml
reyga:
  config:
    logging:
      file:
        enable: false
        summary:
          enable: false
```

Konfigurasi tersebut mengaktifkan output console. Pengumpulan log HTTP melalui
request/response advice tetap membutuhkan artifact runtime `foundation-starter` dan
flag `logging-handler` di bawah.

### Menambahkan filter console sendiri

Buat class turunan `BaseConsoleLogFilter` dan implementasikan `filter(event)`.
Contoh ini hanya meneruskan log dari package aplikasi:

```java
package com.example.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import reyga.starter.foundation.logging.filter.BaseConsoleLogFilter;

public class ApplicationConsoleLogFilter extends BaseConsoleLogFilter {
    @Override
    protected FilterReply filter(ILoggingEvent event) {
        return event.getLoggerName().startsWith("com.example")
                ? FilterReply.NEUTRAL : FilterReply.DENY;
    }
}
```

Pada appender console dalam konfigurasi Logback milik aplikasi:

```xml
<filter class="com.example.logging.ApplicationConsoleLogFilter"/>
```

Gunakan `logging.config` untuk memilih XML aplikasi tersebut. Class custom harus
tersedia pada runtime classpath dan memiliki constructor tanpa argument yang
dapat diakses Logback. Tidak perlu memberi anotasi `@Component` pada filter.
Untuk file rolling atau summary, gunakan `BaseRollingLogFilter` atau
`BaseSummaryLogFilter` dengan pola yang sama.

### Mengaktifkan pencatatan HTTP

```yaml
reyga:
  config:
    default-bean:
      logging-handler: true
```

Logging berlaku untuk request/response body yang didukung HTTP message converter.
Log mencakup request ID, method, endpoint,
alamat remote, username, user agent, waktu response, request body, response body,
exception, serta beberapa header lain. Jika request ID tidak tersedia, UUID baru
dibuat.

Gunakan fitur ini dengan memperhatikan keamanan data. Implementasi saat ini dapat
mencatat access token, request body, dan response body. Aplikasi harus memastikan
masking/redaction, kebijakan retensi, dan akses file log sesuai klasifikasi data.

### Standard logging context dengan `StarterHeaderEnum`

`StarterHeaderEnum` menyediakan nama key yang konsisten untuk pertukaran HTTP
header, MDC, dan servlet request attribute pada proses logging. Gunakan
`getValue()` agar aplikasi tidak menulis nama key secara manual.

```java
import org.springframework.http.HttpHeaders;
import reyga.starter.foundation.common.enumeration.StarterHeaderEnum;

HttpHeaders headers = new HttpHeaders();
headers.set(StarterHeaderEnum.REQUEST_ID.getValue(), requestId);
headers.set(StarterHeaderEnum.USERNAME.getValue(), username);
```

Walaupun bernama `StarterHeaderEnum`, tidak semua constant harus dikirim sebagai
HTTP header. Constant yang bersumber dari aplikasi atau library digunakan sebagai
key context selama satu request.

| Constant | Nilai | Kategori dan kegunaan |
|---|---|---|
| `ACCESS_TOKEN` | `x-starter-access-token` | HTTP header opsional untuk membawa access token ke context log. Nilainya bersifat sensitif dan harus dimasking atau tidak diteruskan jika kebijakan keamanan melarang pencatatan token. |
| `USERNAME` | `x-starter-user-name` | HTTP header opsional untuk menghubungkan log dengan username pemanggil. |
| `REQUEST_ID` | `x-starter-request-id` | Correlation ID lintas service. Logging handler memakai nilai yang diterima dan membuat UUID ketika nilainya tidak tersedia. Teruskan key ini pada outbound request agar trace tetap berkesinambungan. |
| `METHOD` | `x-starter-request-method` | Context yang diisi dari HTTP method request, misalnya `GET` atau `POST`. |
| `STATUS_CODE` | `x-starter-status-code` | Key standard untuk menyimpan atau meneruskan HTTP response status pada custom logging context. |
| `REQUEST_ENDPOINT` | `x-starter-request-uri` | Context yang diisi dengan URI endpoint yang sedang diproses. |
| `FORWARDED_FOR` | `x-starter-forwarded-for` | Context alamat asal request. Default logging handler mengisinya dari remote address request. |
| `PACKAGE_INFO` | `x-starter-package-name` | Context opsional untuk nama package, komponen, atau execution scope yang ingin ditampilkan pada summary log. Aplikasi dapat mengisinya melalui MDC bila diperlukan. |
| `EXCEPTION` | `x-starter-request-exception` | Servlet request attribute untuk exception yang ditangkap exception handler dan kemudian dimasukkan ke summary log. Bukan header yang perlu dikirim client. |
| `REQUEST` | `x-starter-request` | Servlet request attribute untuk request body yang ditangkap request body advice. Bukan header yang perlu dikirim client. |
| `RESPONSE` | `x-starter-response` | Servlet request attribute untuk response body yang ditangkap response body advice. Bukan header yang perlu dikirim client. |
| `USER_AGENT` | `x-starter-user-agent` | HTTP header opsional untuk identitas client yang akan dicatat pada context log. |
| `RESPONSE_TIME` | `x-starter-response-time` | Context durasi pemrosesan request yang dihitung oleh logging handler dalam milidetik. |
| `SUMMARY_LOG` | `x-starter-summary-log` | Marker MDC internal untuk mengarahkan event penyelesaian request ke summary appender. Jangan dikirim atau diubah oleh client. |

Untuk custom filter atau custom logger, gunakan constant yang sama saat membaca
MDC. Selalu hapus context yang dibuat manual setelah proses selesai agar data
request tidak terbawa ketika thread digunakan kembali.

```java
import org.slf4j.MDC;

try {
    MDC.put(StarterHeaderEnum.PACKAGE_INFO.getValue(), "customer-service");
    // Jalankan proses yang membutuhkan logging context.
} finally {
    MDC.remove(StarterHeaderEnum.PACKAGE_INFO.getValue());
}
```

### Perbedaan feature flag dan output Logback

`reyga.config.default-bean.logging-handler` mengontrol pengumpulan log HTTP.
Sementara itu, `reyga.config.logging.*` mengontrol format dan appender Logback.
Keduanya independen:

- console appender selalu dikonfigurasi;
- rolling appender hanya aktif jika `rolling.enable: true`;
- summary appender aktif secara default karena `summary.enable` bernilai `true`;
- tanpa `logging-handler`, summary HTTP normalnya tidak dihasilkan karena service
  tidak memberi marker MDC `AFTER COMPLETION`.

### Property Logback

```yaml
reyga:
  config:
    logging:
      console:
        pattern: "[%d{yyyy-MM-dd HH:mm:ss.SSS}] %X{x-starter-request-id} %-5level %logger{150} - %msg%n"
      file:
        enable: true
        pattern: "[%d{yyyy-MM-dd HH:mm:ss.SSS}] | %X{x-starter-request-id} | %-5level | %logger{150} | - %msg%n"
        file-path: logs/
        active-file-name: application.log
        file-name: "%d{dd-MM-yyyy}.app.log.%i.gz"
        clean-history-on-start: false
        summary:
          enable: true
          pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} | %X{x-starter-request-id} | %-5level | %logger{150} | - %msg%n"
          summary-active-file-name: monitoring/summary.log
          summary-file-name: "monitoring/%d{dd-MM-yyyy}.summary.log.%i.gz"
          summary-clean-history-on-start: false
        max-history: 30
        max-file-size: 20MB
        async-logs:
          core-pool-size: 5
          max-pool-size: 10
          queue-capacity: 100
          thread-name: async-logging-
```

| Property | Default Logback | Kegunaan |
|---|---|---|
| `reyga.config.logging.console.pattern` | Lihat contoh di atas | Pattern log console. |
| `reyga.config.logging.file.enable` | `false` | Mengaktifkan file log aplikasi reguler. |
| `reyga.config.logging.file.pattern` | Lihat contoh di atas | Pattern setiap baris file log reguler. |
| `reyga.config.logging.file.file-path` | `logs/` | Direktori dasar file log, relatif terhadap working directory jika bukan absolute path. |
| `reyga.config.logging.file.active-file-name` | `application.log` | Nama file aplikasi aktif yang langsung menerima event log. |
| `reyga.config.logging.file.file-name` | `%d{dd-MM-yyyy}.app.log.%i.gz` | Pola nama archive log aplikasi. |
| `reyga.config.logging.file.clean-history-on-start` | `false` | Menjalankan pembersihan archive aplikasi berdasarkan retention saat startup. |
| `reyga.config.logging.file.summary.enable` | `true` | Mengaktifkan file summary yang hanya menerima event ber-marker summary. |
| `reyga.config.logging.file.summary.pattern` | Lihat contoh di atas | Pattern setiap baris summary log. |
| `reyga.config.logging.file.summary.summary-active-file-name` | `monitoring/summary.log` | Nama file summary aktif di bawah `file-path`. |
| `reyga.config.logging.file.summary.summary-file-name` | `monitoring/%d{dd-MM-yyyy}.summary.log.%i.gz` | Pola archive summary di bawah `file-path`. |
| `reyga.config.logging.file.summary.summary-clean-history-on-start` | `false` | Menjalankan pembersihan archive summary berdasarkan retention saat startup. |
| `reyga.config.logging.file.max-history` | `30` | Jumlah periode archive yang dipertahankan Logback. |
| `reyga.config.logging.file.max-file-size` | `20MB` | Ukuran maksimum per archive sebelum index berikutnya dibuat. |

Nilai default tabel berlaku ketika menggunakan
`logging.config=classpath:META-INF/logback-spring.xml`.

File aktif ditulis segera setelah appender menerima event. File dengan ekstensi
`.gz` adalah archive yang baru terbentuk ketika terjadi rollover berdasarkan
tanggal atau `max-file-size`. Konfigurasi dari JAR tidak dipantau untuk hot reload;
restart aplikasi setelah mengubah property logging.

Jika aplikasi menyediakan `logback-spring.xml` sendiri, pastikan appender/filter
foundation yang dibutuhkan ikut dikonfigurasi atau pindahkan seluruh pengaturan ke
file milik aplikasi.

## H. Async Logging dan Propagasi MDC

Atur kapasitas executor logging sesuai beban aplikasi. Konteks log (MDC) diteruskan
ke pekerjaan asynchronous sehingga identitas request tetap dapat ditelusuri.

```yaml
reyga:
  config:
    logging:
      file:
        async-logs:
          core-pool-size: 5
          max-pool-size: 10
          queue-capacity: 100
          thread-name: async-logging-
```

| Property | Default | Kegunaan |
|---|---:|---|
| `reyga.config.logging.file.async-logs.core-pool-size` | `5` | Jumlah minimum worker thread. |
| `reyga.config.logging.file.async-logs.max-pool-size` | `10` | Jumlah maksimum worker thread. |
| `reyga.config.logging.file.async-logs.queue-capacity` | `100` | Kapasitas antrean sebelum pool berkembang menuju maksimum. |
| `reyga.config.logging.file.async-logs.thread-name` | `async-logging` | Prefix nama worker thread. Disarankan diakhiri `-` agar mudah dibaca. |

Nama bean `asyncExecutor` digunakan oleh library. Gunakan nama lain untuk executor
tambahan milik aplikasi agar tidak terjadi konflik.

## I. Default Utilities

```yaml
reyga:
  config:
    default-bean:
      utilities: true
```

Aktifkan flag ini untuk menggunakan dua API berikut:

- `ResilienceService`, facade untuk menjalankan supplier/runnable dengan rate
  limiter, circuit breaker, retry, dan fallback;
- `TransactionalExecutor`, untuk menjalankan proses dengan
  fallback dan rollback saat terjadi exception.

`TransactionalExecutor` membutuhkan sebuah `PlatformTransactionManager`. Jika
flag aktif tetapi aplikasi tidak menyediakan transaction manager, startup context
akan gagal. Flag ini juga tidak otomatis membuat registry Resilience4j; aktifkan
masing-masing konfigurasi resilience yang memang akan dipakai.

### Default HTTP client

Tambahkan artifact `common-http`, lalu aktifkan bean bawaan:

```yaml
reyga:
  config:
    default-bean:
      http-client: true
```

Konfigurasi ini membuat satu `OkHttpClient` reusable dan satu
`StarterHttpClient`. Untuk mengatur timeout, interceptor, proxy, authenticator,
atau TLS, deklarasikan bean `OkHttpClient` sendiri; auto-configuration akan
menggunakannya tanpa membuat client kedua. Bean `StarterHttpClient` milik aplikasi
juga mengambil prioritas atas implementasi bawaan.

Tutup setiap `Response` dari call sinkron dengan try-with-resources. Callback
asinkron juga bertanggung jawab menutup response yang diterimanya. Method caching
tetap mengikuti header dan aturan cache HTTP, serta tidak mengambil alih lifecycle
`Cache` yang diberikan consumer.

## J. Rate Limiter

```yaml
reyga:
  config:
    rate-limiter:
      required: true
      timeout-milis: 60000
      max-request: 3
      refresh-period-seconds: 6000
```

| Property | Default | Kegunaan |
|---|---:|---|
| `required` | `false` | Membuat `RateLimiterConfig` dan `RateLimiterRegistry`. |
| `timeout-milis` | `60000` | Waktu maksimum menunggu permission, dalam milidetik. Nama property mengikuti field library: `milis`, bukan `millis`. |
| `max-request` | `3` | Jumlah permission dalam setiap refresh period. |
| `refresh-period-seconds` | `6000` | Interval pengisian ulang permission, dalam detik. |

`utilities: true` diperlukan jika registry tersebut akan digunakan melalui default
`ResilienceService`. Tidak diperlukan jika aplikasi menginjeksi registry dan
menggunakannya secara langsung.

Inject `RateLimiterConfig` ke controller atau service untuk menggunakan nilai YAML.
Tidak perlu membangun ulang config dengan
`RateLimiterConfig.custom()` apabila seluruh endpoint memang menggunakan nilai
global dari YAML.

Untuk `ResilienceBaseController`, aktifkan juga
`reyga.config.circuit-breaker.required=true` karena constructor class tersebut
selalu membutuhkan `CircuitBreakerRegistry`, meskipun endpoint hanya memanggil
helper rate limiter. Contoh constructor injection dan pemanggilan helper tersedia
pada [Panduan Implementasi Controller](CONTROLLER_IMPLEMENTATION_GUIDE.md#contoh-rate-limiter-menggunakan-config-yaml).

## K. Circuit Breaker

Tidak ada default untuk parameter circuit breaker selain `required`. Ketika fitur
diaktifkan, isi seluruh nilai secara eksplisit:

```yaml
reyga:
  config:
    circuit-breaker:
      required: true
      failure-rate-threshold: 50
      slow-call-rate-threshold: 50
      slow-call-duration-threshold-seconds: 2
      minimum-number-of-calls: 10
      sliding-window-size: 20
      permitted-number-of-calls-in-half-open-state: 5
      wait-duration-in-open-state-seconds: 30
      automatic-transition-from-open-to-half-open-enabled: true
```

| Property | Default | Kegunaan |
|---|---:|---|
| `required` | `false` | Membuat `CircuitBreakerConfig` dan `CircuitBreakerRegistry`. |
| `failure-rate-threshold` | `0.0` | Persentase failure untuk membuka circuit. |
| `slow-call-rate-threshold` | `0.0` | Persentase slow call untuk membuka circuit. |
| `slow-call-duration-threshold-seconds` | `0` | Durasi minimum agar call dikategorikan lambat. |
| `minimum-number-of-calls` | `0` | Minimum call sebelum failure/slow-call rate dihitung. |
| `sliding-window-size` | `0` | Ukuran sliding window. |
| `permitted-number-of-calls-in-half-open-state` | `0` | Call yang diizinkan saat half-open. |
| `wait-duration-in-open-state-seconds` | `0` | Belum mengubah durasi open; lihat catatan perilaku di bawah. |
| `automatic-transition-from-open-to-half-open-enabled` | `false` | Mengaktifkan transisi otomatis ke half-open. |

Nilai binding `0` bukan berarti valid untuk Resilience4j. Mengaktifkan fitur tanpa
nilai yang sesuai batas Resilience4j dapat menggagalkan startup.

> Perilaku saat ini: durasi menunggu dalam state open mengikuti nilai
> `slow-call-duration-threshold-seconds`. Mengubah
> `wait-duration-in-open-state-seconds` saja belum mengubah durasi tersebut.
> Pertimbangkan keterkaitan ini saat menentukan konfigurasi circuit breaker.

## L. Retry

```yaml
reyga:
  config:
    retry:
      required: true
      max-attempts: 3
      wait-duration-millis: 10000
      fail-after-max-attempts: true
```

| Property | Default | Kegunaan |
|---|---:|---|
| `required` | `false` | Membuat `RetryConfig` dan `RetryRegistry`. |
| `max-attempts` | `3` | Total attempt, termasuk pemanggilan awal. |
| `wait-duration-millis` | `10000` | Jeda antar-attempt dalam milidetik. |
| `fail-after-max-attempts` | `true` | Mengikuti opsi `failAfterMaxAttempts` pada Resilience4j. |

Setiap retry dicatat sebagai log warning. Gunakan retry hanya untuk operasi yang
aman diulang, misalnya pembacaan atau operasi dengan idempotency key.

## M. Aspect Advice

Setiap Aspect Advice mempunyai flag aktivasi dan nama bean behavior sendiri:

```yaml
reyga:
  config:
    aspect:
      around: true
      behavior: auditAspectBehavior
      before: true
      before-behavior: authorizationBeforeBehavior
      after: true
      after-behavior: cleanupAfterBehavior
      after-returning: true
      after-returning-behavior: resultAuditBehavior
      after-throwing: true
      after-throwing-behavior: failureAuditBehavior
```

Setiap flag yang bernilai `true` mewajibkan property behavior pasangannya berisi
nama bean Spring dari class yang extends base behavior advice tersebut. Tutorial
pembuatan behavior, anotasi method, lifecycle, penanganan exception, dan
troubleshooting tersedia pada bagian
[Aspect Advice di Panduan Utilities](UTILITIES_GUIDE.md#k-aspect-advice).

## N. Pilihan Fitur Saat Ini

Gunakan `logging-handler` untuk mengaktifkan pencatatan HTTP, bukan
`request-response-advice`. Advice `around`, `before`, `after`, `after-returning`,
dan `after-throwing` tersedia secara independen. Aktifkan flag yang diperlukan
dan isi property behavior pasangannya dengan nama bean yang sesuai.

## O. Contoh Profile Development

```yaml
logging:
  config: classpath:META-INF/logback-spring.xml

reyga:
  config:
    default-bean:
      exception-handler: true
      validation-handler: true
      logging-handler: true
      utilities: true
    logging:
      file:
        enable: false
        summary:
          enable: false
```

Konfigurasi ini mempertahankan log HTTP di console tanpa membuat file rolling atau
summary.

## P. Contoh Profile Production

```yaml
logging:
  config: classpath:META-INF/logback-spring.xml

reyga:
  config:
    default-bean:
      exception-handler: true
      validation-handler: true
      logging-handler: true
      utilities: true
    rate-limiter:
      required: true
      timeout-milis: 500
      max-request: 100
      refresh-period-seconds: 1
    retry:
      required: true
      max-attempts: 3
      wait-duration-millis: 500
      fail-after-max-attempts: true
    logging:
      file:
        enable: true
        file-path: /var/log/my-service/
        active-file-name: application.log
        file-name: "%d{dd-MM-yyyy}.app.log.%i.gz"
        clean-history-on-start: true
        summary:
          enable: true
          summary-active-file-name: monitoring/summary.log
          summary-file-name: "monitoring/%d{dd-MM-yyyy}.summary.log.%i.gz"
          summary-clean-history-on-start: true
        max-history: 30
        max-file-size: 20MB
        async-logs:
          core-pool-size: 5
          max-pool-size: 20
          queue-capacity: 1000
          thread-name: async-logging-
```

Nilai kapasitas, timeout, rate limit, serta retensi di atas adalah contoh, bukan
baseline universal. Sesuaikan melalui load test, SLA, jumlah instance, dan batas
resource aplikasi. Pastikan direktori log tersedia dan writable oleh process.

## Q. Environment Variable

Spring Boot relaxed binding memungkinkan property dikirim melalui environment
variable. Contoh:

```text
REYGA_CONFIG_DEFAULT_BEAN_LOGGING_HANDLER=true
REYGA_CONFIG_LOGGING_FILE_ENABLE=true
REYGA_CONFIG_LOGGING_FILE_FILE_PATH=/var/log/my-service/
REYGA_CONFIG_LOGGING_FILE_ASYNC_LOGS_MAX_POOL_SIZE=20
```

## R. Checklist Troubleshooting

| Gejala | Pemeriksaan |
|---|---|
| Bean `ValidationUtility` tidak ditemukan | Buat bean dengan `ValidationConfig.builder()` atau aktifkan `reyga.config.default-bean.validation-handler=true`. |
| Bean `StarterHttpClient` tidak ditemukan | Tambahkan module `common-http` dan aktifkan `reyga.config.default-bean.http-client=true`, atau deklarasikan bean sendiri. |
| Context gagal karena `PlatformTransactionManager` | Nonaktifkan `utilities` atau sediakan transaction manager. |
| Context gagal karena `JdbcTemplate` | Pastikan dependency JDBC, driver, dan konfigurasi datasource aplikasi tersedia. |
| Context gagal saat circuit breaker aktif | Isi semua parameter dengan nilai valid untuk Resilience4j. |
| Request/response tidak tercatat | Aktifkan `logging-handler` dan pastikan request melewati HTTP message converter. |
| Pattern atau appender bawaan library tidak digunakan | Pastikan `logging.config=classpath:META-INF/logback-spring.xml` pada konfigurasi aktif dan artifact `logging` tersedia di runtime classpath. |
| Summary file kosong | Aktifkan `logging-handler`; summary hanya menerima event dengan marker MDC summary. |
| File log tidak dibuat | Periksa `rolling.enable`/`summary.enable`, resolved `file-path`, permission, serta cari `application.log` atau `monitoring/summary.log`; file `.gz` baru dibuat ketika rollover. |
| Bean duplicate/ambigu | Hindari nama bean yang sama dan jangan aktifkan handler library bersamaan dengan handler aplikasi untuk exception yang sama. |
