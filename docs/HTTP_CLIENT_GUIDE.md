# Panduan Starter HTTP Client

`common-http` menyediakan `StarterHttpClient`, `HttpOperations<T, R>`, dan
`HttpBodyCodec`. Implementasi, registrasi YAML, dan auto-configuration berada
di artifact `foundation-starter`. Gunakan dependency dari
[panduan utilities](UTILITIES_GUIDE.md#l-common-http-client).

Panduan ini mencakup semua entry point dan setter pada API saat ini. Contoh
potongan method mengasumsikan `httpClient` sudah di-inject. Variabel seperti
`customerId`, `requestId`, `token`, `uploadPath`, dan `destination` berasal dari
aplikasi consumer. Nama class pada YAML harus disesuaikan dengan package DTO
consumer. Setiap public record/class pada contoh disimpan dalam file terpisah.

| Kebutuhan | Bagian |
|---|---|
| Instalasi dan constructor injection | [0](#0-instalasi-dan-constructor-injection) |
| Schema YAML dan defaults | [1](#1-registrasi-api-melalui-yaml) |
| Tanpa body: id; dengan body: id + payload | [1.1](#11-pemakaian-paling-sederhana-metadata-dari-yaml-payload-per-request) |
| Enum method, media type, dan header | [1.2](#12-enum-dan-string-pada-builder-dan-yaml) |
| Sumber body dan codec | [2](#2-kontrak-body) |
| Builder dan sync | [3](#3-builder-dan-eksekusi-sinkron) |
| Async dan cancellation | [4](#4-asinkron-timeout-dan-customization), [10](#10-async-dengan-completablefuture-dan-cancellation) |
| Seluruh method dan prioritas override | [5](#5-referensi-api-dan-prioritas-override) |
| GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS | [6](#6-case-method-http-dan-body-kosong) |
| DTO, collection, XML, dan raw payload | [7](#7-case-json-xml-dan-generic-response) |
| Form, multipart, upload, download | [8](#8-form-multipart-upload-dan-download) |
| Error HTTP, transport, dan parsing | [9](#9-status-http-response-kosong-dan-error-handling) |
| Timeout, auth, redirect, dan TLS | [11](#11-timeout-authentication-interceptor-dan-tls) |
| Cache dan API Request lama | [12](#12-response-cache-dan-api-request-lama) |
| Payload per request dan custom codec | [13](#13-payload-per-request-dan-custom-codec) |
| Profiles, lifecycle, testing, troubleshooting | [14](#14-profiles-environment-dan-lifecycle), [15](#15-testing-consumer), [16](#16-troubleshooting-dan-batasan-api) |

## 0. Instalasi dan constructor injection

Versi contoh mengikuti `projectVersion=1.1.0`. BOM menyelaraskan artifact
foundation; BOM tidak menambahkan API atau implementasi secara otomatis.

Gradle Kotlin DSL:

```kotlin
val foundationVersion = "1.1.0"
dependencies {
    implementation(platform("com.reyga-dev.starter:foundation-bom:$foundationVersion"))
    implementation("com.reyga-dev.starter:common-http")
    runtimeOnly("com.reyga-dev.starter:foundation-starter")
}
```

Gradle Groovy DSL:

```groovy
def foundationVersion = "1.1.0"
dependencies {
    implementation platform("com.reyga-dev.starter:foundation-bom:${foundationVersion}")
    implementation "com.reyga-dev.starter:common-http"
    runtimeOnly "com.reyga-dev.starter:foundation-starter"
}
```

Maven:

```xml
<properties>
    <reyga-foundation.version>1.1.0</reyga-foundation.version>
</properties>
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.reyga-dev.starter</groupId>
            <artifactId>foundation-bom</artifactId>
            <version>${reyga-foundation.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
<dependencies>
    <dependency>
        <groupId>com.reyga-dev.starter</groupId>
        <artifactId>common-http</artifactId>
    </dependency>
    <dependency>
        <groupId>com.reyga-dev.starter</groupId>
        <artifactId>foundation-starter</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

Gunakan repository publikasi foundation milik organisasi sesuai
[Readme](../Readme.md); contoh ini tidak menjalankan publikasi.
Jika source consumer memakai class implementasi/configuration dari artifact
`foundation-starter`, artifact tersebut harus berada pada compile classpath
(`implementation` pada Gradle atau scope default pada Maven).

Model yang digunakan pada contoh YAML:

```java
package com.example.customer;

public record CustomerRequest(String name) {
}
```

```java
package com.example.customer;

public record CustomerResponse(String id, String name) {
}
```

Gateway minimal yang dapat dipanggil dari service:

```java
package com.example.customer;

import java.io.IOException;

import okhttp3.Response;
import org.springframework.stereotype.Component;
import reyga.starter.foundation.common_http.client.HttpOperations;
import reyga.starter.foundation.common_http.client.StarterHttpClient;

@Component
public class CustomerGateway {
    private final StarterHttpClient httpClient;

    public CustomerGateway(StarterHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CustomerResponse find(String customerId) throws IOException {
        HttpOperations<Void, CustomerResponse> operation = httpClient
                .<Void, CustomerResponse>operationBuilder()
                .id("customer-api")
                .pathVariable("customerId", customerId)
                .build();
        try (Response response = httpClient.execute(operation)) {
            if (!response.isSuccessful()) {
                throw new IOException("Customer API returned HTTP " + response.code());
            }
            return operation.readResponse(response);
        }
    }
}
```

Tipe import utama adalah `okhttp3.Call`, `okhttp3.Callback`, `okhttp3.Response`,
`okhttp3.Request`, `okhttp3.RequestBody`, `okhttp3.MediaType`, serta
`reyga.starter.foundation.common_http.client.HttpOperations` dan
`reyga.starter.foundation.common_http.client.StarterHttpClient`.

## 1. Registrasi API melalui YAML

```yaml
reyga:
  config:
    http-client:
      enabled: true
      connect-timeout: 5s
      read-timeout: 20s
      write-timeout: 20s
      call-timeout: 30s
      follow-redirects: false
      retry-on-connection-failure: false
      register-api:
        - id: customer-api
          base-url: https://customer.example.com/v1
          path: customers/{customerId}
          method: GET
          content-type: application/json
          accept: application/json
          headers:
            - name: Authorization
              value: "Bearer ${CUSTOMER_API_TOKEN}"
            - name: X-Channel
              value: mobile
            - name: X-Channel
              value: web
          query-parameters:
            - name: include
              value: address
            - name: include
              value: contact
          response-type: com.example.customer.CustomerResponse
          call-timeout: 10s
        - id: create-customer-xml
          base-url: https://customer.example.com/v1
          path: customers
          method: POST
          content-type: application/xml
          accept: application/xml
          request-body-required: true
          response-type: com.example.customer.CustomerResponse
```

`register-api`, `headers`, dan `query-parameters` berupa list record; tidak ada
batas jumlah entry dari starter. Header/query dengan nama yang sama dapat berulang.
`path-variables` berupa map opsional untuk nilai default placeholder:

```yaml
path-variables:
  customerId: CUS-01
```

Setiap registrasi wajib memiliki `id` unik dan `base-url` absolut HTTP(S).
`path` selalu ditambahkan pada path `base-url`, termasuk ketika diawali `/`.
Gunakan `query-parameters` untuk query; `path` tidak boleh berisi URL absolut,
query, fragment, atau segment `.`/`..`. Base URL tidak boleh mengandung userinfo
atau fragment. Nilai path variable di-encode sebagai satu path segment; slash
dalam nilai tidak membuat segment baru.

Flag lama `reyga.config.default-bean.http-client=true` tetap didukung. Fitur
aktif jika salah satu flag bernilai `true`; jika keduanya tidak aktif, starter
tidak membuat atau memvalidasi infrastructure HTTP.

| Property | Default | Perilaku |
|---|---|---|
| `enabled` | `false` | Membuat transport, codec, registry, dan starter client. |
| `connect-timeout` | `10s` | Timeout koneksi pada transport default. |
| `read-timeout` | `10s` | Timeout pembacaan pada transport default. |
| `write-timeout` | `10s` | Timeout penulisan pada transport default. |
| `call-timeout` | `0ms` | Timeout keseluruhan; dapat di-override per registrasi dan builder. |
| `follow-redirects` | `true` | Kebijakan redirect transport default. |
| `retry-on-connection-failure` | `true` | Recovery koneksi OkHttp; bukan retry bisnis berdasarkan status HTTP. |
| `register-api` | list kosong | Registrasi endpoint; builder langsung tetap dapat digunakan. |
| `register-api[].path` | string kosong | Path tambahan. |
| `register-api[].method` | `GET` | Method dinormalisasi ke uppercase. |
| `register-api[].headers` | list kosong | Header default. |
| `register-api[].query-parameters` | list kosong | Query default; value harus string non-null. |
| `register-api[].path-variables` | map kosong | Nilai default placeholder, dapat di-override. |
| `register-api[].content-type` | `application/json` | Representasi request. |
| `register-api[].accept` | sama dengan `content-type` | Representasi response yang diharapkan. |
| `register-api[].request-body-required` | mengikuti method final | Wajib payload builder; default true untuk POST/PUT/PATCH/PROPPATCH/REPORT, false untuk method lainnya. |
| `register-api[].response-type` | tidak ada | Nama class lengkap untuk decoding opsional. |
| `register-api[].call-timeout` | tidak ada | Mewarisi timeout transport. |

Timeout nol menonaktifkan batas terkait. Nilai lain harus antara `1ms` dan
`2147483647ms`. Registrasi invalid, id duplikat, nilai wajib kosong/unresolved,
kontrak body GET/HEAD invalid, atau response class tidak dapat dimuat menggagalkan
startup. Placeholder path tanpa default diperbolehkan saat startup tetapi wajib
diisi ketika `build()`.

### 1.1. Pemakaian paling sederhana: metadata dari YAML, payload per request

Untuk endpoint tanpa request body, builder cukup `id`. Untuk request yang memiliki
body, builder wajib memiliki `id` dan `requestBody(payload)`; metadata lainnya
berasal dari YAML. Payload tidak disimpan di YAML maupun diambil dari bean.

```yaml
reyga:
  config:
    http-client:
      enabled: true
      connect-timeout: 5s
      read-timeout: 20s
      write-timeout: 20s
      call-timeout: 30s
      register-api:
        - id: customer-config-only
          base-url: https://customer.example.com/v1
          path: customers/{customerId}
          path-variables:
            customerId: CUS-01
          method: GET
          content-type: APPLICATION_JSON
          accept: APPLICATION_JSON
          headers:
            - name: X-Channel
              value: web
          query-parameters:
            - name: include
              value: address
          response-type: com.example.customer.CustomerResponse
          call-timeout: 10s
        - id: create-customer-config-only
          base-url: https://customer.example.com/v1
          path: customers
          method: POST
          content-type: APPLICATION_JSON
          accept: APPLICATION_JSON
          request-body-required: true
          response-type: com.example.customer.CustomerResponse
```

GET cukup memilih id; POST menerima payload baru untuk setiap call:

```java
var get = httpClient.<Void, CustomerResponse>operationBuilder()
        .id("customer-config-only").build();
try (Response response = httpClient.execute(get)) {
    if (!response.isSuccessful()) {
        throw new IOException("Customer GET returned HTTP " + response.code());
    }
    CustomerResponse customer = get.readResponse(response);
}

var post = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-config-only")
        .requestBody(new CustomerRequest("Ada"))
        .build();
try (Response response = httpClient.execute(post)) {
    if (!response.isSuccessful()) {
        throw new IOException("Customer POST returned HTTP " + response.code());
    }
    CustomerResponse created = post.readResponse(response);
}
```

Untuk raw response, generic type tidak wajib ditulis. Async memakai pola builder
yang sama: `executeAsync(operation, callback)` tetap mengembalikan Call dan callback
menutup response. `build()` tidak mengirim request.

Semua placeholder path harus memiliki default YAML agar kode minimal cukup id
atau id+body; jika nilainya dinamis, tambahkan `pathVariable(...)`. Runtime response
class berasal dari `response-type`; generic Java membantu tipe hasil di source.
Credential harus resolved. DTO tetap dibuat consumer, tetapi tidak perlu menjadi
Spring bean. String/byte array/RequestBody raw juga selalu diberikan per request.

`request-body-required` hanya menandai kontrak endpoint, bukan menyediakan payload:

| Method final | Default wajib body | Pemakaian minimal |
|---|---|---|
| GET / HEAD | false; body dilarang | `id(...).build()` |
| POST / PUT / PATCH / PROPPATCH / REPORT | true | `id(...).requestBody(payload).build()` |
| DELETE / OPTIONS / method lain | false | Id saja jika tanpa body; id+body jika mengirim payload. |

Gunakan `request-body-required: true` untuk DELETE/custom endpoint yang membutuhkan
body. Endpoint POST tanpa payload dapat memakai `request-body-required: false`;
id saja kemudian menghasilkan empty RequestBody yang diperlukan transport OkHttp.
Nilai false membuat payload opsional, bukan melarang payload. GET/HEAD tetap
menolak body dan `request-body-required: true` menggagalkan startup.

Jika body wajib, payload hilang/null gagal saat `build()` dengan message yang
meminta `requestBody(...)`. Empty String/byte array boleh diberikan secara eksplisit
jika kontrak upstream memang membutuhkan body kosong. Semua request dengan body
memerlukan registered id, termasuk form/multipart/binary. Builder langsung tanpa
id tetap tersedia untuk request tanpa body; raw Request API OkHttp tetap tersedia
untuk kebutuhan low-level.

### 1.2. Enum dan String pada builder dan YAML

String tetap didukung, dengan overload enum untuk field berikut:

| Setter | Enum | String |
|---|---|---|
| `method` | `HttpMethod` | Method standar/custom. |
| `contentType` | `HttpContentType` | MIME literal, symbolic enum name, charset/boundary/custom MIME. |
| `accept` | `HttpContentType` | MIME literal atau symbolic enum name. |
| `header` / `addHeader` | `HttpHeader` untuk nama | Header standar/custom; value tetap String dinamis. |

Import ketiganya dari `reyga.starter.foundation.common_http.enums`:

```java
var operation = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-config-only")
        .method(HttpMethod.POST)
        .contentType(HttpContentType.APPLICATION_XML)
        .accept(HttpContentType.APPLICATION_XML)
        .header(HttpHeader.AUTHORIZATION, "Bearer " + token)
        .addHeader(HttpHeader.X_REQUEST_ID, requestId)
        .requestBody(new CustomerRequest("Ada"))
        .build();
```

Enum memakai wire value dengan behavior/override yang sama seperti String.
`HttpMethod` menyediakan GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE,
CONNECT, PROPPATCH, REPORT. Header enum menyediakan ACCEPT, CONTENT_TYPE,
AUTHORIZATION, PROXY_AUTHORIZATION, CACHE_CONTROL, IF_NONE_MATCH,
IF_MODIFIED_SINCE, USER_AGENT, COOKIE, X_REQUEST_ID.

| HttpContentType | Wire value |
|---|---|
| `APPLICATION_JSON` | `application/json` |
| `APPLICATION_XML` | `application/xml` |
| `TEXT_XML` | `text/xml` |
| `TEXT_PLAIN` | `text/plain` |
| `TEXT_HTML` | `text/html` |
| `APPLICATION_FORM_URLENCODED` | `application/x-www-form-urlencoded` |
| `MULTIPART_FORM_DATA` | `multipart/form-data` |
| `APPLICATION_OCTET_STREAM` | `application/octet-stream` |
| `APPLICATION_PDF` | `application/pdf` |
| `APPLICATION_PROBLEM_JSON` | `application/problem+json` |
| `APPLICATION_JSON_PATCH` | `application/json-patch+json` |
| `APPLICATION_MERGE_PATCH_JSON` | `application/merge-patch+json` |

`HttpContentType.value()` dan `HttpHeader.value()` mengembalikan wire String;
`HttpMethod.name()` mengembalikan method token. Charset dan boundary memakai
String/custom RequestBody; enum media tidak menyimpan parameter tersebut.
Structured format selain JSON/XML tetap memerlukan codec consumer atau raw body.

YAML berupa teks. `method: GET` memilih method GET; `content-type` dan `accept`
menerima MIME literal serta nama konstanta media enum tanpa membedakan case:

```yaml
method: POST
content-type: APPLICATION_JSON
accept: application_xml
```

Contoh tersebut mengirim JSON dan mengharapkan XML. Builder String media setters
juga menerima nama enum. MIME custom seperti `application/vnd.customer+json`
tetap valid; nama tidak dikenal seperti `APPLICATION_UNKNOWN` gagal validasi.
Header YAML memakai nama wire, misalnya `name: Content-Type`; `CONTENT_TYPE`
bukan alias header YAML. Overload enum menolak null; untuk menguji argumen null,
cast ke tipe yang dimaksud agar compiler dapat memilih overload. Tidak ada
perubahan default method, media type, maupun prioritas override.

## 2. Kontrak body

Payload selalu berasal dari pemanggilan `requestBody(...)`, bukan YAML atau nama
bean. Buat DTO immutable baru sesuai data setiap request. Contoh minimal:

```java
var operation = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-xml")
        .requestBody(new CustomerRequest("Ada"))
        .build();
```

DTO response tetap memakai `response-type` sebagai nama class/record di YAML.
Tidak ada lookup Spring bean untuk DTO request maupun response. Isi body di-encode
saat build dan tidak ditambahkan ke registry global.
Codec default menggunakan Jackson 2 JSON/XML; versinya mengikuti
`jacksonDatabindVersion`. Structured DTO menggunakan UTF-8. `String` dan `byte[]`
diteruskan sebagai raw body, sehingga String JSON/XML harus sudah berisi dokumen
lengkap. `RequestBody` OkHttp juga diterima untuk streaming, form, multipart, atau
representasi khusus; caller bertanggung jawab atas content type serta replayability.
Byte array disalin dan structured payload diserialisasi menjadi snapshot.
GET/HEAD menolak body; POST/PUT/PATCH mewajibkan payload per request secara default.

Untuk mengganti mapping, modules, naming strategy, atau format, deklarasikan bean
`HttpBodyCodec` milik aplikasi. Default codec tidak mengubah mapper Spring Boot.
XML decoding menonaktifkan DTD dan external entities. JSON/XML mendukung juga
media type dengan suffix `+json`/`+xml`; custom codec diperlukan untuk structured
format lain. Format namespace, root, dan collection XML tetap mengikuti model
dan kemampuan Jackson XML; raw XML tersedia bila schema memerlukan bentuk khusus.

## 3. Builder dan eksekusi sinkron

Inject `StarterHttpClient` melalui constructor, kemudian buat builder baru untuk
setiap operasi. Builder mutable tidak thread-safe; starter client dan registry
dapat dibagikan antar-thread.

```java
HttpOperations<Void, CustomerResponse> operation = httpClient
        .<Void, CustomerResponse>operationBuilder()
        .id("customer-api")
        .pathVariable("customerId", customerId)
        .header("X-Request-Id", requestId)
        .queryParameter("include", "address")
        .addQueryParameter("include", "contact")
        .callTimeout(Duration.ofSeconds(8))
        .build();

try (Response response = httpClient.execute(operation)) {
    if (!response.isSuccessful()) {
        throw new IOException("Customer API returned HTTP " + response.code());
    }
    CustomerResponse customer = operation.readResponse(response);
}
```

Untuk endpoint tanpa body dengan semua default placeholder tersedia, pemakaian paling sederhana adalah
`httpClient.operationBuilder().id("customer-config-only").build()`; endpoint dengan body membutuhkan `requestBody(...)`.
`execute()` selalu mengembalikan `okhttp3.Response`. Response non-2xx tetap
dikembalikan; starter tidak mengubahnya menjadi exception bisnis.
`readResponse()` opsional, membaca body sekali, dan mengembalikan `null` untuk
body kosong, termasuk response `204`. Method tersebut tidak menafsirkan status
HTTP dan caller tetap harus menutup response. Jangan membaca raw body lalu
memanggil `readResponse()` pada body yang sama.

Semua nilai endpoint dapat di-override melalui builder:

```java
HttpOperations<CustomerRequest, CustomerResponse> operation = httpClient
        .<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-xml")
        .baseUrl("https://customer.example.com/v2")
        .path("customers")
        .method("POST")
        .contentType("application/json")
        .accept("application/json")
        .requestBody(new CustomerRequest("Ada"))
        .responseType(CustomerResponse.class)
        .build();
```

Override diterapkan setelah defaults YAML tanpa bergantung urutan pemanggilan
setter. `header()` mengganti seluruh nilai header dengan nama sama tanpa
membedakan case; `addHeader()` menambahkan nilai. `queryParameter()` mengganti
seluruh nilai query dengan nama sama; `addQueryParameter()` menambahkan nilai.
Override `contentType` juga mengubah default accept jika registrasi memakai
representasi request/response yang sama; gunakan `accept()` untuk memilihnya
secara eksplisit. Header `Content-Type`/`Accept` yang diisi melalui setter header
memiliki prioritas terakhir sesuai urutan setter header.

`requestBody(null)` menghapus payload builder saat ini; body wajib tetap gagal validasi jika payload menjadi null. `responseType(null)` menonaktifkan decoding default.
Untuk generic collection, `responseType(...)` menerima `java.lang.reflect.Type`,
misalnya `TypeReference<List<CustomerResponse>>.getType()` dari dependency Jackson
yang dideklarasikan sendiri oleh consumer.

Builder langsung tanpa registrasi membutuhkan `baseUrl`:

```java
var operation = httpClient.operationBuilder()
        .baseUrl("https://customer.example.com")
        .path("health")
        .build(); // GET, application/json
```

`build()` tidak melakukan request jaringan. Unknown id, URL/path/header invalid,
placeholder hilang, body GET/HEAD, atau serialization gagal menghasilkan
`IllegalArgumentException`; cause serialization dipertahankan. Prepared operation
dengan body buffered dapat dipakai ulang, sedangkan streaming/one-shot body
tetap mengikuti lifecycle OkHttp.

## 4. Asinkron, timeout, dan customization

```java
Call call = httpClient.executeAsync(operation, new Callback() {
    @Override
    public void onFailure(Call call, IOException failure) {
        handleFailure(failure);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try (response) {
            if (!response.isSuccessful()) {
                handleFailure(new IOException("Upstream HTTP " + response.code()));
                return;
            }
            handleCustomer(operation.readResponse(response));
        }
    }
});
// Simpan call untuk cancellation jika operasi tidak lagi dibutuhkan.
call.cancel();
```

`executeAsync()` mengembalikan `okhttp3.Call` yang sudah di-enqueue. Callback
berjalan pada dispatcher OkHttp dan wajib menutup response. Timeout per operasi
diterapkan pada Call baru; transport, connection pool, dan dispatcher tetap
dibagikan. `IOException` transport diteruskan; cancellation mengikuti OkHttp.

Bean `OkHttpClient`, `HttpBodyCodec`, `HttpOperationRegistry`, dan
`StarterHttpClient` consumer mengambil prioritas. Properties transport tidak
memodifikasi bean OkHttp consumer. Config registry tetap divalidasi ketika fitur
aktif. Custom `StarterHttpClient` lama tetap kompatibel; implementasikan
`operationBuilder()` untuk mendukung registrasi dan override `execute`/`executeAsync`
untuk menerapkan timeout per operasi pada transport custom.

API lama `call(Request)`, `asynchronousCall(Request, Callback)`, dan
`callWithResponseCaching(Request, Cache)` tetap tersedia. Gunakan
`operation.request()` jika perlu meneruskan hasil builder ke API caching tersebut;
timeout per operasi berlaku pada `execute`/`executeAsync` implementasi default.
Untuk caching, interceptor, proxy, authenticator, certificate pinning, dan TLS,
gunakan konfigurasi bean OkHttp sendiri sesuai
[panduan utilities](UTILITIES_GUIDE.md#l4-response-caching).

Simpan token di environment/secret store. Hindari retry bisnis otomatis untuk
request non-idempotent dan jangan mencatat credential atau payload sensitif.

## 5. Referensi API dan prioritas override

| Entry point | Hasil | Catatan |
|---|---|---|
| `operationBuilder()` | `HttpOperations.Builder<T, R>` | Builder baru; belum mengirim request. |
| `execute(operation)` | `Response` | Sinkron; caller menutup response. |
| `executeAsync(operation, callback)` | `Call` | Sudah dijadwalkan; jangan memanggil `enqueue`/`execute` lagi pada Call yang sama. |
| `call(request)` | `Response` | Sinkron dengan raw OkHttp Request. |
| `asynchronousCall(request, callback)` | `Call` | Async dengan raw Request. |
| `callWithResponseCaching(request, cache)` | `Response` | Sinkron dengan cache pilihan caller. |
| `operation.request()` | `Request` | Snapshot request untuk inspeksi atau API raw. |
| `operation.callTimeout()` | `Duration` atau `null` | Override timeout; `null` mewarisi transport. |
| `operation.readResponse(response)` | `R` atau `null` | Decoding opsional; membaca body sekali. |

`HttpOperations` dibuat melalui `httpClient.operationBuilder()`, bukan static
`HttpOperations.builder()`. Constructor kosong hanya untuk compatibility dan
tidak menghasilkan request yang dapat dieksekusi.

| Setter | Mandatory/null | Hasil pada default YAML |
|---|---|---|
| `id(String)` | Opsional; jika dipakai harus nonblank dan terdaftar | Memilih registrasi; tidak menghapus override lain. |
| `baseUrl(String)` | Wajib tanpa id; nonblank | Mengganti seluruh base URL. |
| `path(String)` | Opsional; `""` boleh, `null` tidak | Mengganti path, bukan menambahkan ke path registrasi. |
| `method(String/HttpMethod)` | Opsional; nonblank/huruf saja atau enum non-null | Mengganti method; default GET. |
| `header(String/HttpHeader, value)` | Nama nonblank/non-null, value non-null | Mengganti semua value dengan nama sama. |
| `addHeader(String/HttpHeader, value)` | Sama seperti header | Menambahkan value. |
| `queryParameter(name, value)` | Nama nonblank, value non-null | Mengganti value dari base URL, YAML, dan setter sebelumnya. |
| `addQueryParameter(name, value)` | Sama seperti queryParameter | Menambahkan value, termasuk nama berulang. |
| `pathVariable(name, value)` | Nama nonblank, value non-null | Mengganti nilai placeholder dari map YAML. |
| `contentType(String/HttpContentType)` | Opsional; MIME/enum name valid atau enum non-null | Mengganti representasi request. |
| `accept(String/HttpContentType)` | Opsional; media type tunggal/enum name valid atau enum non-null | Mengganti representasi response yang diharapkan. |
| `requestBody(T)` | Wajib jika kontrak body-required; semua payload memerlukan id | Mengisi payload per request; null menghapus payload builder dan gagal jika body wajib. |
| `responseType(Type)` | Opsional; `null` boleh | Mengganti atau menonaktifkan target decoding. |
| `callTimeout(Duration)` | Opsional; non-null, rentang valid | Mengganti timeout registrasi; nol menonaktifkan batas. |
| `build()` | URL final wajib valid; placeholder wajib lengkap | Menghasilkan operasi; belum mengirim jaringan. |

Scalar yang diset berulang memakai nilai setter terakhir, termasuk `requestBody()`; payload tidak diwarisi dari YAML.
Urutan pemanggilan setter `id()` tidak mempengaruhi prioritas override. Urutan
setter header/query tetap penting karena operasi replace dan append diterapkan
berurutan setelah defaults.

Untuk media headers, builder membuat Content-Type/Accept dari field final,
kemudian menerapkan list header YAML, setter eksplisit `contentType`/`accept`,
dan terakhir operasi `header`/`addHeader` berurutan. Karena itu, header Accept
yang ditulis eksplisit pada list YAML tetap dapat mengalahkan default Accept
yang mengikuti perubahan content type; gunakan `accept(...)` untuk menggantinya.

```java
var operation = httpClient.operationBuilder()
        .header("X-Channel", "first")
        .id("customer-api")
        .header("x-channel", "second")
        .addHeader("X-Channel", "third")
        .pathVariable("customerId", "CUS-01")
        .build();
// X-Channel berisi second dan third; value YAML serta first telah diganti.
```

Contoh komposisi URL:

| Base URL | Path | Hasil |
|---|---|---|
| `https://example.com/v1` | `customers` | `https://example.com/v1/customers` |
| `https://example.com/v1/` | `/customers` | `https://example.com/v1/customers` |
| `https://example.com/v1?page=1` | `customers` | `https://example.com/v1/customers?page=1` |
| `https://example.com/v1` | `""` | `https://example.com/v1` |

Query values di-encode oleh OkHttp. Jangan melakukan URL encoding manual sebelum
`queryParameter()` atau `pathVariable()` karena karakter `%` dapat di-encode lagi.
Query names case-sensitive; header names tidak. Value kosong `""` boleh untuk
header/query, tetapi placeholder path yang kosong ditolak saat build.
Tidak ada setter penghapusan header/query; value kosong tidak menghapus entry.
Gunakan registrasi lain atau builder tanpa id ketika ingin menghilangkan default.
`path("")` menghapus path registrasi, tetapi mempertahankan path pada base URL.

## 6. Case method HTTP dan body kosong

| Method | Body tidak diisi | Body diisi | Response |
|---|---|---|---|
| GET / HEAD | Tidak mengirim body | Ditolak | GET dapat mempunyai body; HEAD biasanya kosong. |
| POST / PUT / PATCH | Ditolak secara default; body-required=false mengizinkan empty body | Mengirim payload | Raw Response; decode bila dibutuhkan. |
| DELETE / OPTIONS | Tidak mengirim body | Diperbolehkan builder; upstream menentukan kontrak | Raw Response. |
| PROPPATCH / REPORT | Ditolak secara default; body-required=false mengizinkan empty body | Mengirim payload | Mengikuti upstream. |
| Method lain berupa huruf | Mengikuti validasi OkHttp | Mengikuti validasi OkHttp/upstream | Tidak ada registry whitelist method bisnis. |

Contoh create JSON dengan payload per request (registrasi `create-customer-config-only` dari bagian 1.1):

```java
var create = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-config-only")
        .requestBody(new CustomerRequest("Ada"))
        .build();
try (Response response = httpClient.execute(create)) {
    if (response.code() != 201) {
        throw new IOException("Expected HTTP 201, received " + response.code());
    }
    CustomerResponse created = create.readResponse(response);
    String location = response.header("Location");
}
```

PUT mengganti representasi resource; PATCH mengikuti format patch upstream.
Starter tidak membuat dokumen patch secara otomatis:

```java
var update = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("customer-api")
        .pathVariable("customerId", "CUS-01")
        .method("PUT")
        .requestBody(new CustomerRequest("Updated Name"))
        .build();

var patch = httpClient.<String, CustomerResponse>operationBuilder()
        .id("customer-api")
        .pathVariable("customerId", "CUS-01")
        .method("PATCH")
        .contentType("application/merge-patch+json")
        .accept("application/json")
        .requestBody("{\"name\":\"Updated Name\"}")
        .build();
```

DELETE tanpa decoding serta HEAD untuk metadata:

```java
var delete = httpClient.operationBuilder().id("customer-api")
        .pathVariable("customerId", "CUS-01")
        .method("DELETE").responseType(null).build();
try (Response response = httpClient.execute(delete)) {
    if (!response.isSuccessful()) {
        throw new IOException("Delete failed with HTTP " + response.code());
    }
}

var head = httpClient.operationBuilder().baseUrl("https://customer.example.com")
        .path("exports/latest").method("HEAD").build();
try (Response response = httpClient.execute(head)) {
    String length = response.header("Content-Length");
    String etag = response.header("ETag");
}
```

OPTIONS menggunakan `.method("OPTIONS")`; baca `response.header("Allow")` jika
upstream mengirimnya. Untuk POST tanpa payload, set `request-body-required: false` pada registrasi. Jika body wajib, `.requestBody(null)` tetap ditolak; empty body harus diberikan eksplisit bila diperlukan.

## 7. Case JSON, XML, dan generic response

### 7.1. Nested object, array, Map, dan tipe generic

Structured payload boleh berupa record, POJO, nested object, `Map`, atau list
yang dapat diserialisasi oleh codec. Tipe generic `T`/`R` membantu source code;
decoding runtime tetap memerlukan `response-type` atau `responseType(...)`.
Memakai `<Void, CustomerResponse>` saja tidak menentukan runtime response class.

```java
// Import java.util.Map.
var operation = httpClient.<Map<String, Object>, CustomerResponse>operationBuilder()
        .id("create-customer-config-only")
        .requestBody(Map.of("name", "Ada", "tags", List.of("web", "new")))
        .build();
```

```java
// Membutuhkan com.fasterxml.jackson.core.type.TypeReference dan java.util.List.
var responseType = new TypeReference<List<CustomerResponse>>() {}.getType();
var operation = httpClient.<Void, List<CustomerResponse>>operationBuilder()
        .baseUrl("https://customer.example.com/v1")
        .path("customers")
        .responseType(responseType)
        .build();
try (Response response = httpClient.execute(operation)) {
    if (!response.isSuccessful()) {
        throw new IOException("List failed with HTTP " + response.code());
    }
    List<CustomerResponse> customers = operation.readResponse(response);
}
```

`TypeReference` pada contoh berasal dari Jackson 2 (`com.fasterxml.jackson.*`).
Declare Jackson databind sebagai dependency compile jika consumer memakai tipe
tersebut. Spring Boot 4 mapper menggunakan package Jackson 3 yang berbeda;
jangan mencampur API kedua versi. Alternatif tanpa tipe Jackson adalah
`responseType(CustomerResponse[].class)` dengan `R=CustomerResponse[]`.
YAML `response-type` hanya memuat class; tipe seperti
`java.util.List<com.example.customer.CustomerResponse>` tidak dapat dimuat.
Untuk generic wrapper/Map/list, gunakan `java.lang.reflect.Type` lewat builder.

### 7.2. Raw JSON/XML dan charset

```java
var rawJson = httpClient.<String, Void>operationBuilder()
        .id("create-customer-config-only")
        .baseUrl("https://customer.example.com/v1").path("customers")
        .method("POST").contentType("application/json")
        .requestBody("{\"name\":\"Ada\"}").build();

var rawXml = httpClient.<String, Void>operationBuilder()
        .id("create-customer-config-only")
        .baseUrl("https://customer.example.com/v1").path("customers")
        .method("POST").contentType("application/xml; charset=utf-8")
        .requestBody("<customer><name>Ada</name></customer>").build();
```

Raw String tidak divalidasi sebagai JSON/XML saat build. Mengirim Java String
`"Ada"` pada media type JSON mengirim karakter `Ada`, bukan JSON string
`"\"Ada\""`. Gunakan DTO atau dokumen JSON lengkap sesuai kontrak upstream.
Structured payload default harus UTF-8; raw String mengikuti charset media type
OkHttp, sementara raw bytes harus sudah memakai encoding yang diinginkan.

### 7.3. DTO XML dengan root dan namespace

Jika consumer mengimpor annotation XML, declare `jackson-dataformat-xml` pada
compile classpath. Contoh versi yang selaras dengan source project:

```kotlin
val jacksonVersion = "2.21.2"
dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
}
```

```groovy
def jacksonVersion = "2.21.2"
dependencies {
    implementation "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:${jacksonVersion}"
}
```

```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
    <version>2.21.2</version>
</dependency>
```

```java
package com.example.customer;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "customer", namespace = "urn:example:customer")
public record XmlCustomerRequest(
        @JacksonXmlProperty(localName = "name", namespace = "urn:example:customer")
        String name
) {
}
```

```java
var operation = httpClient.<XmlCustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-xml")
        .requestBody(new XmlCustomerRequest("Ada"))
        .build();
```

Response class harus cocok dengan struktur response sebenarnya, termasuk nested
element/collection. Root/namespace pada DTO request tidak otomatis diterapkan ke
DTO response. Codec XML default tidak memanggil `findAndRegisterModules()` seperti
mapper JSON-nya; Java time/custom modules XML membutuhkan codec consumer.
SOAP envelope, XML signature, schema validation, dan WS-Security bukan fitur
otomatis starter; gunakan raw XML/custom codec dan implementasi protokol consumer.

### 7.4. Content-Type response dan Accept

`readResponse()` memilih JSON/XML berdasarkan header **Content-Type response**.
Jika header tersebut tidak ada, fallback memakai Accept final pada operation.
`accept("application/xml")` tidak memaksa body JSON menjadi XML. Nonempty response
dengan `text/html`/`text/plain` gagal pada codec default; baca raw body jika format
tersebut memang diharapkan. Header kosong/invalid juga bukan fallback ke JSON.
`Accept` registrasi/setter media harus satu media type yang valid; daftar
negosiasi seperti `application/json, application/xml` tidak didukung oleh parser
konfigurasi. `responseType(String.class)` berarti deserialize JSON/XML ke String,
bukan mengembalikan raw text; untuk raw text gunakan `response.body().string()`.

## 8. Form, multipart, upload, dan download

Registrasi berikut digunakan oleh contoh body khusus. Payload/form/file tetap
diberikan setiap request melalui builder:

```yaml
reyga:
  config:
    http-client:
      enabled: true
      register-api:
        - id: submit-customer-form
          base-url: https://customer.example.com
          path: forms/customer
          method: POST
          content-type: APPLICATION_FORM_URLENCODED
          accept: APPLICATION_JSON
        - id: upload-customer-document
          base-url: https://customer.example.com
          path: documents
          method: POST
          content-type: MULTIPART_FORM_DATA
          accept: APPLICATION_JSON
        - id: upload-customer-binary
          base-url: https://customer.example.com
          path: binary
          method: PUT
          content-type: APPLICATION_OCTET_STREAM
          accept: APPLICATION_JSON
```

Jika contoh ini dipasang bersama registrasi lain, gabungkan entry ke satu list
`register-api`; jangan membuat key YAML duplikat. Multipart boundary tetap
diperoleh dari RequestBody pada setiap upload.

### 8.1. Form URL encoded

```java
// Import okhttp3.FormBody dan okhttp3.RequestBody.
RequestBody form = new FormBody.Builder()
        .add("name", "Ada")
        .add("channel", "web")
        .build();
var operation = httpClient.<RequestBody, Void>operationBuilder()
        .id("submit-customer-form")
        .requestBody(form).build();
try (Response response = httpClient.execute(operation)) {
    if (!response.isSuccessful()) {
        throw new IOException("Form submission failed with HTTP " + response.code());
    }
}
```

### 8.2. Multipart file dan metadata

```java
// Import okhttp3.MediaType, okhttp3.MultipartBody, dan okhttp3.RequestBody.
RequestBody file = RequestBody.create(uploadPath.toFile(), MediaType.get("application/pdf"));
MultipartBody multipart = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("customerId", "CUS-01")
        .addFormDataPart("document", "document.pdf", file)
        .build();
var operation = httpClient.<RequestBody, Void>operationBuilder()
        .id("upload-customer-document")
        .contentType(multipart.contentType().toString())
        .requestBody(multipart).build();
try (Response response = httpClient.execute(operation)) {
    if (!response.isSuccessful()) {
        throw new IOException("Upload failed with HTTP " + response.code());
    }
}
```

Boundary dibuat oleh `MultipartBody`; jangan hardcode boundary sendiri. Raw
`RequestBody` melewati codec. Content type body dapat menentukan header yang
dikirim transport OkHttp; selaraskan dengan builder. Source file harus masih
tersedia dan tidak berubah sampai request selesai, termasuk pada async.

### 8.3. Binary upload dan streaming download

```java
var upload = httpClient.<byte[], Void>operationBuilder()
        .id("upload-customer-binary")
        .requestBody(new byte[]{1, 2, 3}).build();
```

Untuk download besar, stream langsung; `readResponse()` membaca seluruh body ke
memory dan tidak menyediakan batas ukuran payload:

```java
// Import java.nio.file.Files dan java.io.InputStream; destination bertipe Path.
var download = httpClient.operationBuilder()
        .baseUrl("https://customer.example.com")
        .path("exports/latest").accept("application/octet-stream").build();
try (Response response = httpClient.execute(download)) {
    if (!response.isSuccessful()) {
        throw new IOException("Download failed with HTTP " + response.code());
    }
    try (InputStream input = response.body().byteStream()) {
        Files.copy(input, destination); // Gagal jika target sudah ada.
    }
}
```

Validasi ukuran serta lokasi output sesuai aplikasi. Download yang terputus dapat
meninggalkan file parsial; consumer dapat menulis ke file temporary lalu memindah
hasil setelah sukses. Streaming raw tidak menggunakan response DTO.

## 9. Status HTTP, response kosong, dan error handling

Ada tiga fase kegagalan yang berbeda:

| Fase | Contoh | Yang diterima caller |
|---|---|---|
| Startup | Id duplikat, base URL invalid, bean/class hilang | Context gagal; penyebab menjelaskan registrasi invalid. |
| Build | Unknown id, placeholder hilang, GET body, payload tidak bisa diserialisasi | `IllegalArgumentException`; payload hilang/null atau id tidak ada juga ditolak. |
| Execute | DNS, koneksi, TLS, timeout, cancellation | `IOException` pada sync atau `onFailure` pada async. |
| HTTP selesai | 400, 401, 404, 429, 500 | Tetap `Response`; bukan `onFailure` hanya karena status. |
| Decode | JSON/XML malformed, DTO tidak cocok, media unsupported | `IOException`; tipe generic yang salah juga dapat menyebabkan cast failure pada caller. |

Parameter Java mandatory yang `null` dapat menghasilkan `NullPointerException`.
`readResponse()` tanpa response type menghasilkan `IllegalStateException` sebelum
body dibaca. Body kosong menghasilkan `null`, termasuk HEAD/204. Response dengan
JSON literal `null` juga dapat menghasilkan `null`. Payload nonempty seperti HTML
error page tetap gagal decoding meskipun status 200.

Decode error body dengan tipe terpisah, tanpa membaca body dua kali:

```java
// ErrorResponse: public record ErrorResponse(String code, String message) {}
// consumerCodec adalah bean HttpBodyCodec yang di-inject melalui constructor.
var operation = httpClient.<Void, CustomerResponse>operationBuilder()
        .id("customer-api").pathVariable("customerId", "CUS-01").build();
try (Response response = httpClient.execute(operation)) {
    if (response.isSuccessful()) {
        CustomerResponse customer = operation.readResponse(response);
    } else {
        byte[] bytes = response.body().bytes();
        String type = response.header("Content-Type", "application/json");
        ErrorResponse error = bytes.length == 0 ? null
                : (ErrorResponse) consumerCodec.decode(bytes, type, ErrorResponse.class);
        // Petakan response.code() dan error ke exception/domain outcome aplikasi.
    }
}
```

Jangan decode body 404/500 sebagai success DTO kecuali schema-nya memang sama.
Saat wrapping exception, pertahankan cause, misalnya
`throw new IOException("Customer lookup failed", exception)`.
Starter tidak otomatis menerapkan retry status, circuit breaker, pagination,
fallback, maupun pemetaan error bisnis. Kebijakan 404/409/429 dan pembacaan
`Retry-After` tetap milik consumer. Response harus ditutup sebelum retry berikutnya.

## 10. Async dengan CompletableFuture dan cancellation

Berikut adapter consumer; `CompletableFuture` bukan return type bawaan starter.
Potongan method ini ditempatkan di gateway yang mempunyai field `httpClient`:

```java
public CompletableFuture<CustomerResponse> findAsync(String customerId) {
    var operation = httpClient.<Void, CustomerResponse>operationBuilder()
            .id("customer-api").pathVariable("customerId", customerId).build();
    CompletableFuture<CustomerResponse> future = new CompletableFuture<>();
    Call call = httpClient.executeAsync(operation, new Callback() {
        @Override
        public void onFailure(Call call, IOException failure) {
            future.completeExceptionally(failure);
        }

        @Override
        public void onResponse(Call call, Response response) {
            try (response) {
                if (!response.isSuccessful()) {
                    throw new IOException("Customer API returned HTTP " + response.code());
                }
                future.complete(operation.readResponse(response));
            } catch (IOException | RuntimeException failure) {
                future.completeExceptionally(failure);
            }
        }
    });
    future.whenComplete((value, failure) -> {
        if (future.isCancelled()) {
            call.cancel();
        }
    });
    return future;
}
```

Import `java.util.concurrent.CompletableFuture` dan tipe OkHttp callback. Exception
saat `build()` atau penjadwalan sebelum callback terpasang tetap synchronous.
Parsing failure di dalam `onResponse()` tidak otomatis memicu `onFailure()`;
adapter menangkapnya agar future tidak menggantung.

`future.cancel(true)` pada adapter membatalkan Call. Cancellation dapat berlomba
dengan completion; jangan mengasumsikan callback tidak akan pernah dipanggil.
Timeout pada `future.get(...)` hanya membatasi penantian caller, tidak otomatis
membatalkan jaringan. Untuk raw `Call`, simpan handle lalu panggil `call.cancel()`.
Caller callback tetap menutup response ketika diterima setelah cancellation.

Callback memakai dispatcher OkHttp; context MDC, transaksi Spring, dan request
context thread caller tidak otomatis dibawa. Jalankan pekerjaan panjang melalui
executor aplikasi; baca/tutup response sebelum menyerahkan DTO ke executor.

## 11. Timeout, authentication, interceptor, dan TLS

### 11.1. Prioritas timeout

| Layer | Penerapan |
|---|---|
| Properties connect/read/write | Pada transport default saat bean dibuat. |
| Properties global call-timeout | Pada transport default. |
| Bean OkHttp consumer | Menggantikan semua setting transport default. |
| Registration call-timeout | Override timeout keseluruhan pada Call default starter. |
| Builder callTimeout | Mengalahkan registration call-timeout, termasuk nol. |

Timeout connect/read/write bukan timeout total request. Global call timeout nol
berarti tidak ada batas keseluruhan, walaupun read/write tetap mempunyai batas.
Builder tidak mempunyai setter connect/read/write timeout; gunakan client consumer
untuk setting tersebut. `execute`/`executeAsync` menerapkan override pada Call,
sementara `call(operation.request())` dan API caching tidak membawa metadata
timeout operation.

### 11.2. Bearer, Basic, dan token dinamis

```java
// Import okhttp3.Credentials untuk Basic authentication.
var bearer = httpClient.operationBuilder().id("customer-api")
        .pathVariable("customerId", "CUS-01")
        .header("Authorization", "Bearer " + token).build();
var basic = httpClient.operationBuilder().id("customer-api")
        .pathVariable("customerId", "CUS-01")
        .header("Authorization", Credentials.basic(username, password)).build();
```

Gunakan setter `header`, bukan `addHeader`, untuk mengganti authorization default.
Token pada YAML di-bind saat startup; token yang diperbarui tiap call lebih sesuai
dengan builder atau interceptor. Jangan menggunakan URL userinfo untuk credential.
Perhatikan redirect lintas host dan gunakan origin yang dipercaya ketika mengubah
base URL pada registrasi yang mempunyai credential default.

### 11.3. Custom transport dengan interceptor

```java
@Bean
OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(20))
            .writeTimeout(Duration.ofSeconds(20))
            .callTimeout(Duration.ofSeconds(30))
            .followRedirects(false)
            .retryOnConnectionFailure(false)
            .addInterceptor(chain -> {
                Request request = chain.request().newBuilder()
                        .header("X-Application", "customer-service")
                        .build();
                return chain.proceed(request);
            })
            .build();
}
```

Letakkan factory method pada class `@Configuration`; import
`org.springframework.context.annotation.Bean`, `okhttp3.OkHttpClient`,
`okhttp3.Request`, dan `java.time.Duration`. Request interceptor berjalan setelah
builder sehingga dapat mengganti header final. Interceptor harus thread-safe;
hindari membaca body one-shot hanya untuk log. Jika mengimplementasikan
`Authenticator`, batasi percobaan refresh agar 401 tidak membuat loop.

Proxy, authenticator, certificate pinning, trust store, dan TLS dikonfigurasi
melalui builder OkHttp consumer; tidak ada property starter untuk setiap setting
tersebut. Pertahankan validasi certificate/hostname. `follow-redirects` mengatur
followRedirects OkHttp, bukan seluruh policy redirect khusus seperti
`followSslRedirects`; policy lanjutan membutuhkan bean consumer.

## 12. Response cache dan API Request lama

### 12.1. Cache reusable pada transport

```java
@Bean(destroyMethod = "close")
Cache httpResponseCache() {
    return new Cache(Path.of("var", "cache", "customer-http").toFile(), 50L * 1024L * 1024L);
}

@Bean
OkHttpClient okHttpClient(Cache httpResponseCache) {
    return new OkHttpClient.Builder()
            .cache(httpResponseCache)
            .callTimeout(Duration.ofSeconds(30))
            .build();
}
```

Import `okhttp3.Cache`, `java.nio.file.Path`, dan konfigurasi bean seperti bagian
11. Spring menutup cache saat context ditutup; jangan tutup cache per response.
Gunakan directory yang writable dan satu instance Cache untuk directory tersebut.

```java
var operation = httpClient.operationBuilder()
        .baseUrl("https://customer.example.com").path("catalog")
        .header("Cache-Control", "max-age=60").build();
try (Response response = httpClient.execute(operation)) {
    boolean cacheUsed = response.cacheResponse() != null;
    boolean networkUsed = response.networkResponse() != null;
    String catalog = response.body().string();
}
```

Cache mengikuti method, status, serta header server. `Cache-Control` request
tidak membuat response non-cacheable menjadi cacheable. Revalidation dapat
memakai cache dan jaringan sekaligus. `only-if-cached` dapat menghasilkan HTTP
504 saat tidak ada cache yang memenuhi syarat; itu tetap raw Response.

### 12.2. Cache pilihan per request

```java
try (Response response = httpClient.callWithResponseCaching(operation.request(), cache)) {
    String payload = response.body().string();
}
```

Cache tetap milik caller; jika dibuat manual, tutup setelah seluruh call selesai.
Client turunan berbagi connection pool dan dispatcher. Setting `callTimeout()`
pada operation tidak diterapkan oleh API ini; gunakan cache pada transport bila
ingin cache dan timeout operation melalui `execute()` secara bersamaan.

### 12.3. Request OkHttp langsung

```java
Request request = new Request.Builder()
        .url("https://customer.example.com/health")
        .get().build();
try (Response response = httpClient.call(request)) {
    String health = response.body().string();
}
Call call = httpClient.asynchronousCall(request, callback);
```

`callback` di atas mengikuti pola penutupan response pada bagian 4/10. Raw Request
tidak memerlukan registrasi id dan tidak menggunakan defaults/body codec YAML.

## 13. Payload per request dan custom codec

### 13.1. Payload baru dan snapshot setiap operasi

```java
var first = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-xml").requestBody(new CustomerRequest("Ada")).build();
var second = httpClient.<CustomerRequest, CustomerResponse>operationBuilder()
        .id("create-customer-xml").requestBody(new CustomerRequest("Grace")).build();
```

Metadata endpoint dibagikan melalui registry, tetapi setiap build meng-encode
payload input menjadi snapshot tersendiri. Perubahan builder/DTO setelah build
tidak mengubah structured body operasi yang sudah dibangun. Raw streaming
RequestBody tetap mengikuti lifecycle sumbernya. Tidak ada lookup bean payload
atau ketergantungan scope request/prototype.

Penggunaan lama `request-body-bean` pada YAML dan `requestBodyBean(...)` pada
builder telah dihapus atas perubahan kontrak ini. Pindahkan pembuatan payload ke
kode request dan panggil `id(...).requestBody(payload).build()`. YAML lama yang
masih menggunakan key payload tersebut ditolak saat startup dengan arahan migrasi.
Body tidak boleh ditaruh di YAML; `request-body-required` hanya policy metadata.

### 13.2. Custom JSON settings dengan fallback XML default

Factory berikut memakai Jackson 2 JSON mapper consumer, dan mempertahankan codec
default untuk XML. Untuk class `DefaultHttpBodyCodec`, starter harus berada pada
compile classpath seperti dijelaskan pada bagian 0.

```java
@Bean
HttpBodyCodec httpBodyCodec() {
    ObjectMapper json = new ObjectMapper().findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    HttpBodyCodec fallback = new DefaultHttpBodyCodec();
    return new HttpBodyCodec() {
        private boolean isJson(String value) {
            MediaType type = MediaType.parse(value);
            return type != null && (type.subtype().equals("json") || type.subtype().endsWith("+json"));
        }

        @Override
        public byte[] encode(Object body, String mediaType) throws IOException {
            return isJson(mediaType) ? json.writeValueAsBytes(body) : fallback.encode(body, mediaType);
        }

        @Override
        public Object decode(byte[] body, String mediaType, Type type) throws IOException {
            return isJson(mediaType)
                    ? json.readValue(body, json.constructType(type))
                    : fallback.decode(body, mediaType, type);
        }
    };
}
```

Import `java.lang.reflect.Type`, `java.io.IOException`,
`com.fasterxml.jackson.databind.ObjectMapper`,
`com.fasterxml.jackson.databind.DeserializationFeature`, `okhttp3.MediaType`,
dan `reyga.starter.foundation.common_http.client.HttpBodyCodec` serta
`DefaultHttpBodyCodec`. Declare Jackson databind pada compile classpath jika
dipakai langsung. Jangan memutasi konfigurasi mapper setelah codec dipakai
bersama. Default JSON mapper menggunakan setting Jackson default; schema drift
seperti unknown fields dapat memerlukan customization seperti contoh ini.

### 13.3. Registry dan StarterHttpClient consumer

Bean `HttpOperationRegistry` consumer mengganti registry default; consumer
bertanggung jawab atas daftar registrasi/validasinya. Biasanya cukup mengganti
codec/transport dan tetap menggunakan registry bawaan.
Bean `StarterHttpClient` consumer mengganti implementasi default. Default method
`operationBuilder()` pada kontrak lama melempar `UnsupportedOperationException`;
custom implementation harus menyediakan builder. Default `execute()` pada
interface mendelegasikan ke `call(Request)` dan tidak menerapkan timeout operation;
custom implementation harus mengatasinya jika mendukung timeout per operasi.
Tidak ada keharusan menggunakan proxy/AOP untuk client; annotation transaksi pada
consumer tetap mengikuti batasan proxy Spring biasa dan tidak meliputi jaringan
async secara otomatis.

## 14. Profiles, environment, dan lifecycle

Gunakan Spring profile untuk mengganti registrasi environment. Contoh fragmen
`application-local.yml` berikut mengganti daftar dengan endpoint local:

```yaml
reyga:
  config:
    http-client:
      enabled: true
      register-api:
        - id: customer-api
          base-url: http://localhost:8089/v1
          path: customers/{customerId}
          response-type: com.example.customer.CustomerResponse
```

List binding profile Spring mengganti daftar pada sumber dengan prioritas lebih
rendah, bukan menambahkan satu entry secara otomatis. Tuliskan semua registrasi
yang diperlukan pada profile yang mengganti `register-api`. Placeholder secret
yang masih `${...}` ditolak untuk field yang divalidasi; jangan memakai default
secret dummy di environment production. Mapping environment umum:

```text
REYGA_CONFIG_HTTPCLIENT_ENABLED=true
REYGA_CONFIG_HTTPCLIENT_CONNECTTIMEOUT=5s
REYGA_CONFIG_HTTPCLIENT_CALLTIMEOUT=30s
CUSTOMER_API_TOKEN=<nilai dari secret store>
```

Gunakan YAML untuk list kompleks agar index/relaxed binding mudah ditinjau.
Registry adalah snapshot konfigurasi context: perubahan file YAML tidak
melakukan hot reload pada context yang sudah berjalan. Builder menghasilkan
snapshot body baru; request lama tidak berubah setelah builder/DTO diubah.
Jangan membagikan builder mutable antar-thread, atau menganggap satu Call dapat
dieksekusi ulang. Membuat Call baru dari buffered operation diperbolehkan.
Transport/codec/registry dapat dibagikan; streaming body memiliki aturan tersendiri.

Jika consumer memiliki transport/executor/cache sendiri, tetapkan lifecycle
shutdown pada bean consumer dan hentikan request outstanding sebelum menutup
resource bersama. Jangan mematikan dispatcher/connection pool per request.

## 15. Testing consumer

Untuk unit test service, mock `StarterHttpClient` atau gateway consumer dan assert
outcome aplikasi. Untuk integration test HTTP, gunakan server local atau
interceptor OkHttp yang mengembalikan response tanpa jaringan.
Contoh JUnit Jupiter berikut memakai `DefaultStarterHttpClient`, sehingga starter
harus tersedia pada test compile classpath:

```java
@Test
void should_DecodeCustomer_When_UpstreamReturnsJson() throws IOException {
    OkHttpClient transport = new OkHttpClient.Builder()
            .addInterceptor(chain -> new Response.Builder()
                    .request(chain.request()).protocol(Protocol.HTTP_1_1)
                    .code(200).message("OK")
                    .header("Content-Type", "application/json")
                    .body(ResponseBody.create("{\"id\":\"CUS-01\",\"name\":\"Ada\"}",
                            MediaType.get("application/json")))
                    .build())
            .build();
    StarterHttpClient client = new DefaultStarterHttpClient(transport);
    var operation = client.<Void, CustomerResponse>operationBuilder()
            .baseUrl("https://example.test")
            .responseType(CustomerResponse.class).build();
    try (Response response = client.execute(operation)) {
        assertEquals(new CustomerResponse("CUS-01", "Ada"), operation.readResponse(response));
    } finally {
        transport.dispatcher().executorService().shutdown();
        transport.connectionPool().evictAll();
    }
}
```

Import `org.junit.jupiter.api.Test`, static
`org.junit.jupiter.api.Assertions.assertEquals`, `okhttp3.Protocol`,
`okhttp3.ResponseBody`, dan
`reyga.starter.foundation.common_http.client.DefaultStarterHttpClient` bersama
tipe umum sebelumnya. Ini adalah cleanup transport test yang dimiliki test.

Uji kontrak aplikasi untuk status non-2xx, body kosong, malformed JSON/XML,
timeout/cancellation, header/query berulang, generic DTO, serta closure response.
Gunakan `ApplicationContextRunner` untuk flag aktif/nonaktif, missing required body,
duplicate id, dan consumer override; lihat contoh repository di
[HttpRegistrationConfigurationTest](../foundation-starter-internal/src/test/java/reyga/starter/foundation/common_http/config/HttpRegistrationConfigurationTest.java).

## 16. Troubleshooting dan batasan API

| Gejala | Penyebab dan tindakan |
|---|---|
| Bean StarterHttpClient tidak ditemukan | Pastikan artifact runtime tersedia dan salah satu flag aktif. |
| Flag enabled=false tetapi bean default masih dibuat | Periksa flag legacy; salah satu true tetap mengaktifkan fitur. |
| Unknown HTTP API id | Samakan string id dan profile aktif; builder langsung membutuhkan baseUrl. |
| Missing path variable | Lengkapi semua `{name}` lewat map YAML atau setter pathVariable. |
| Endpoint menjadi `/v1/v1/...` | Path selalu ditambahkan; jangan mengulang prefix base URL. |
| Body wajib hilang atau null | Isi `id(...).requestBody(payload)`; body-required=false hanya untuk kontrak upstream yang memang tanpa payload. |
| Body diberikan tanpa id | Daftarkan endpoint di YAML dan pilih id; builder langsung hanya untuk request tanpa body. |
| YAML masih memakai sumber payload lama | Hapus key payload dan berikan payload melalui requestBody pada setiap call. |
| Response class gagal dimuat | Gunakan fully qualified name; generic collection memakai Type di builder. |
| readResponse tanpa target | Tambahkan response-type atau responseType; generic R saja tidak cukup. |
| Parsing gagal pada 200 | Periksa Content-Type aktual, schema DTO, charset, serta kemungkinan HTML login/error page. |
| XML Java time/custom mapping gagal | XML default tidak auto-discover modules; gunakan custom codec. |
| Raw String dikirim sebagai JSON invalid | String tidak diberi JSON quoting otomatis; kirim dokumen lengkap atau DTO. |
| Timeout YAML tidak mempengaruhi transport | Bean OkHttp consumer mengambil prioritas; atur transport tersebut. |
| Timeout operation tidak berlaku pada caching/raw call | API tersebut hanya menerima Request; gunakan cache transport dengan execute. |
| Future async tidak selesai setelah parsing error | Tangkap error dalam onResponse seperti adapter bagian 10. |
| Cache selalu miss | Periksa aturan cache/header server, directory, dan method; cache tidak memaksa response cacheable. |
| Metadata/credential muncul dalam log | Jangan log Request/body/header penuh; gunakan masking dan interceptor terbatas. |
| UnsupportedOperationException pada operationBuilder | Custom StarterHttpClient belum mendukung builder; gunakan implementasi default atau lengkapi custom. |

Batasan saat ini: tidak ada dynamic hot reload registry, penghapusan header/query
default, per-operation connect/read/write timeout, batas ukuran response DTO,
typed async callback bawaan, retry status HTTP, SOAP client, WebSocket, atau SSE
adapter. Kebutuhan tersebut memerlukan konfigurasi/adaptor consumer atau API
OkHttp khusus; jangan menambahkan key YAML yang belum didukung lalu menganggap
key itu mengubah behavior. Registry/transport/codec default adalah bean global
context, bukan client terpisah otomatis per id.

Referensi behavior dependency: [konfigurasi eksternal Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html)
untuk binding environment dan penggantian list profile, serta
[Jackson XML](https://github.com/FasterXML/jackson-dataformat-xml) untuk mapping
root/namespace. Behavior khusus starter pada panduan ini mengikuti source dan
tes repository saat ini.
