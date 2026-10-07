# Panduan Implementasi Repository

Panduan ini menjelaskan pola implementasi repository JDBC menggunakan module `common-database`.
Tujuannya adalah menjaga kontrak domain tetap sederhana, memusatkan eksekusi JDBC pada
`QueryProcessor`, dan menyusun SQL melalui `QueryBuilder` serta `QueryFunction`.

Untuk constructor injection `QueryProcessor`, aplikasi memerlukan dependency API
`common-database` pada scope compile dan artifact `foundation-starter:1.1.0` pada
scope runtime. Siapkan datasource aplikasi agar `JdbcTemplate` tersedia.
Tidak ada flag YAML khusus untuk menggunakan `QueryProcessor`.
Contoh dependency tersedia pada
[Panduan Konfigurasi](CONFIGURATION_GUIDE.md#a-langkah-1-siapkan-dependency-aplikasi).

## A. Sebelum Mulai

Contoh berikut memakai tabel `app_user` dengan kolom `id`, `username`, `status`,
dan `created_at`. Siapkan tabel tersebut melalui migrasi database aplikasi.
Contoh mengasumsikan `id` diisi aplikasi serta `id`, `username`, `status`, dan
`created_at` tidak null. Contoh mapping tambahan menyebutkan tabel dan kolom
tambahan yang perlu disiapkan apabila dipakai di aplikasi.
Tambahkan Spring JDBC dan driver database yang sesuai; dependency aplikasi lengkap
dijelaskan dalam panduan konfigurasi.

Ikuti bagian A sampai E untuk membuat repository pertama, lalu gunakan bagian query,
pagination, dan batch sesuai kebutuhan. Buat instance `QueryBuilder` baru untuk
setiap operasi; jangan simpan builder mutable pada field repository.

## B. Arsitektur yang Direkomendasikan

Alur dependency yang direkomendasikan:

```text
Service
  -> Domain Repository Interface
       extends BaseJdbcRepository<Entity, Identifier>
          -> Repository Implementation
               -> QueryProcessor
                    -> JdbcTemplate
```

Tanggung jawab setiap komponen:

| Komponen | Tanggung jawab |
| --- | --- |
| `BaseJdbcRepository<E, I>` | Kontrak persistence minimum: `save`, `findAll`, dan `findById`. |
| Domain repository interface | Menambahkan query yang bermakna bagi domain. |
| Repository implementation | Menyusun query, memilih `RowMapper`, dan menerjemahkan hasil ke domain model. |
| `JdbcResultSetBuilder` | Membentuk `RowMapper` dengan callback yang hanya menerima `JdbcResultSetMapper`. |
| `JdbcResultSetMapper` | Membaca kolom bertipe dan JSON dengan aturan nullable atau mandatory yang eksplisit. |
| `JdbcJsonColumnReader`, `JdbcJsonColumnReaders` | Mengambil JSON sebagai teks melalui getter JDBC atau pembaca pilihan consumer. |
| `JdbcJsonDecoder<T>` | Mengubah JSON utuh, termasuk nested object dan array, menjadi tipe domain. |
| `QueryBuilder` | Menyusun SQL parameterized untuk SELECT, INSERT, UPDATE, dan DELETE. |
| `QueryFunction` | Menyusun expression SQL seperti `COUNT`, `UPPER`, `COALESCE`, atau `TO_DATE`. |
| `QueryProcessor` | Menjalankan query builder atau raw SQL melalui implementasi JDBC. |

Service sebaiknya hanya bergantung pada domain repository interface. Jangan inject
`JdbcTemplate`, `QueryProcessor`, atau `QueryBuilder` langsung ke service karena hal tersebut
akan membocorkan detail persistence ke business layer.

## C. Membuat Model Domain

Contoh domain model yang digunakan di seluruh panduan:

```java
package com.example.user.domain;

import java.time.LocalDateTime;

public record User(
        Long id,
        String username,
        String status,
        LocalDateTime createdAt
) {
}
```

## D. Mendefinisikan Repository Interface

Repository domain harus berupa interface yang meng-extend `BaseJdbcRepository`.
Method tambahan sebaiknya menggunakan istilah domain dan tidak mengekspos tipe JDBC.

```java
package com.example.user.repository;

import com.example.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reyga.starter.foundation.common_database.repository.BaseJdbcRepository;

import java.util.Optional;

public interface UserRepository extends BaseJdbcRepository<User, Long> {

    Page<User> findAll(Pageable pageable);

    Optional<User> findByUsernameIgnoreCase(String username);

    int updateStatus(Long id, String status);

    int deleteById(Long id);
}
```

Tambahkan overload `findAll(Pageable)` pada interface repository jika aplikasi
membutuhkan hasil berupa halaman. Gunakan `findAll()` untuk mengambil seluruh data.

## E. Mengimplementasikan Repository

Implementation class menerima `QueryProcessor` melalui constructor injection. Buat
`RowMapper` melalui `JdbcResultSetBuilder` agar setiap pembacaan kolom menggunakan
kontrak nullable atau mandatory pada `JdbcResultSetMapper`. Mapper yang callback-nya
stateless dapat digunakan ulang untuk menjaga mapping kolom tetap konsisten.

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
INSERT INTO app_user (id, username, status, created_at) VALUES (?, ?, ?, ?);
SELECT id, username, status, created_at FROM app_user ORDER BY created_at DESC;
SELECT id, username, status, created_at FROM app_user WHERE id = ?;
SELECT id, username, status, created_at FROM app_user ORDER BY id ASC LIMIT ? OFFSET ?;
SELECT id, username, status, created_at FROM app_user WHERE UPPER(username) = ?;
UPDATE app_user SET status = ? WHERE id = ?;
DELETE FROM app_user WHERE id = ?;
```

Penggunaan QueryBuilder:

```java
package com.example.user.repository.jdbc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.example.user.domain.User;
import com.example.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import reyga.starter.foundation.common_database.processor.QueryProcessor;
import reyga.starter.foundation.common_database.util.JdbcResultSetBuilder;
import reyga.starter.foundation.common_database.util.QueryBuilder;
import reyga.starter.foundation.common_database.util.QueryFunction;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final String TABLE = "app_user";
    private static final String[] COLUMNS = {
            "id", "username", "status", "created_at"
    };

    private static final RowMapper<User> USER_ROW_MAPPER = JdbcResultSetBuilder.<User>builder()
            .map((row, rowNumber) -> new User(
                    row.getMandatory("id", Long.class),
                    row.getMandatory("username", String.class),
                    row.getMandatory("status", String.class),
                    row.getMandatory("created_at", LocalDateTime.class)
            ))
            .build();

    private final QueryProcessor queryProcessor;

    public JdbcUserRepository(QueryProcessor queryProcessor) {
        this.queryProcessor = queryProcessor;
    }

    @Override
    public User save(User user) {
        QueryBuilder query = QueryBuilder.builder()
                .insertInto(TABLE, COLUMNS)
                .values(user.id(), user.username(), user.status(), user.createdAt());

        int affectedRows = queryProcessor.execute(query);
        if (affectedRows != 1) {
            throw new IllegalStateException("Penyimpanan user harus memengaruhi tepat satu baris");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        QueryBuilder query = QueryBuilder.builder()
                .select(COLUMNS)
                .from(TABLE)
                .orderBy("created_at DESC");

        return queryProcessor.fetch(query, USER_ROW_MAPPER);
    }

    @Override
    public Optional<User> findById(Long id) {
        QueryBuilder query = QueryBuilder.builder()
                .select(COLUMNS)
                .from(TABLE)
                .where()
                    .equals("id", id)
                .done();

        return queryProcessor.optionalFetchOneBy(query, USER_ROW_MAPPER);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        QueryBuilder query = QueryBuilder.builder()
                .select(COLUMNS)
                .from(TABLE).orderBy("id ASC");

        return queryProcessor.fetchPage(query, USER_ROW_MAPPER, pageable);
    }

    @Override
    public Optional<User> findByUsernameIgnoreCase(String username) {
        QueryBuilder query = QueryBuilder.builder()
                .select(COLUMNS)
                .from(TABLE)
                .where()
                    .equals(
                            QueryFunction.upper("username"),
                            username.toUpperCase(Locale.ROOT)
                    )
                .done();

        return queryProcessor.optionalFetchOneBy(query, USER_ROW_MAPPER);
    }

    @Override
    public int updateStatus(Long id, String status) {
        QueryBuilder query = QueryBuilder.builder()
                .update(TABLE)
                .set("status", status)
                .where()
                    .equals("id", id)
                .done();

        return queryProcessor.execute(query);
    }

    @Override
    public int deleteById(Long id) {
        QueryBuilder query = QueryBuilder.builder()
                .deleteFrom(TABLE)
                .where()
                    .equals("id", id)
                .done();

        return queryProcessor.execute(query);
    }
}
```

Pada contoh ini, `save` melakukan INSERT, bukan upsert. Pastikan ID baru dan
`createdAt` sudah diisi. Untuk perubahan data, gunakan method UPDATE terpisah.

`QueryProcessor` dapat menerima builder langsung. Panggil `query.build()` hanya jika
Anda perlu memeriksa SQL sebelum eksekusi.

### Mapping kolom nullable dan mandatory

Import API builder dan mapper dari package berikut:

```java
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common_database.util.JdbcResultSetBuilder;
import reyga.starter.foundation.common_database.util.JdbcResultSetMapper;
```

`JdbcResultSetMapper` hanya perlu di-import jika callback dideklarasikan sebagai
method dengan parameter bertipe eksplisit. Pada lambda, tipe `row` diinferensikan
oleh builder. API ini tersedia dari artifact `common-database`, tidak membutuhkan
bean Spring atau property aktivasi. Eksekusi melalui implementasi default
`QueryProcessor` tetap membutuhkan datasource dan runtime starter sebagaimana
dijelaskan di awal panduan.

`JdbcResultSetBuilder` membentuk Spring `RowMapper`, bukan JDBC `ResultSet`. Callback
`map((row, rowNumber) -> ...)` menerima `JdbcResultSetMapper` yang hanya membaca
baris saat ini. Callback tidak menerima cursor JDBC dan tidak boleh menyimpan `row`
untuk dipakai setelah callback selesai. Builder tidak menjalankan query, tidak
memanggil `ResultSet.next()`, dan tidak menutup resource JDBC.

#### Alur builder

1. Tentukan tipe hasil melalui `JdbcResultSetBuilder.<T>builder()`.
2. Pasang callback dengan `.map((row, rowNumber) -> ...)` untuk membuat satu object
   dari satu baris hasil query.
3. Baca setiap kolom memakai `row.getMandatory(label, type)` atau
   `row.getNullable(label, type)` sesuai kontrak hasil query.
4. Panggil `.build()` untuk memperoleh `RowMapper<T>`.
5. Teruskan mapper ke method SELECT pada `QueryProcessor`.

Builder tidak mencocokkan nama property domain dengan nama kolom secara otomatis.
Callback menentukan kolom, konversi tambahan, dan constructor atau setter yang
dipakai. `rowNumber` adalah nomor baris yang diberikan Spring JDBC, umumnya
zero-based; nilainya bukan ID database atau nomor urut global pagination.

#### Kontrak nullable dan mandatory

Setiap pembacaan memakai salah satu kontrak berikut:

| Pembacaan | SQL NULL | Nilai non-null |
|---|---|---|
| `row.getNullable(column, type)` | Mengembalikan Java `null` | Mengembalikan nilai bertipe yang diminta |
| `row.getMandatory(column, type)` | Melempar `SQLDataException` dengan SQL state `22004` dan nama kolom | Mengembalikan nilai, termasuk `0`, `false`, atau string kosong |

Aturan ini berlaku pada nilai hasil query, termasuk hasil outer join; bukan pada
metadata constraint tabel. Kolom yang tidak ada atau konversi yang tidak didukung
tetap gagal dengan `SQLException` asli dari driver. Mandatory hanya melarang NULL,
bukan menggantikan validasi panjang string, rentang angka, atau aturan domain.

#### Mapping record

Contoh berikut memakai proyeksi `user_statistics` dengan kolom `user_id`,
`login_count`, dan `verified`. `user_id` wajib ada; dua kolom lainnya boleh SQL
NULL. Definisikan record pada file `UserStatistics.java`:

```java
public record UserStatistics(Long id, Integer loginCount, Boolean verified) {
}
```

Di repository, buat mapper sekali dan gunakan ulang:

```java
RowMapper<UserStatistics> mapper = JdbcResultSetBuilder.<UserStatistics>builder()
        .map((row, rowNumber) -> new UserStatistics(
                row.getMandatory("user_id", Long.class),
                row.getNullable("login_count", Integer.class),
                row.getNullable("verified", Boolean.class)
        ))
        .build();
```

Berikut contoh hasil mapping; tipe `verified` mengasumsikan driver mendukung
konversi kolom database tersebut ke `Boolean`:

| `user_id` | `login_count` | `verified` | Hasil |
|---|---|---|---|
| `10` | SQL NULL | SQL NULL | `UserStatistics(10L, null, null)` |
| `10` | `0` | `false` | `UserStatistics(10L, 0, false)` |
| SQL NULL | `3` | `true` | Mapping gagal dengan `SQLDataException` |

#### Integrasi dengan QueryProcessor

Gunakan `mapper` dari contoh record di atas untuk query builder maupun raw SQL:

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
SELECT user_id, login_count, verified FROM user_statistics WHERE user_id = ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("user_id", "login_count", "verified")
        .from("user_statistics")
        .where()
            .equals("user_id", userId)
        .done();

List<UserStatistics> rows = queryProcessor.fetch(query, mapper);
Optional<UserStatistics> statistics = queryProcessor.optionalFetchOneBy(query, mapper);

Optional<UserStatistics> rawStatistics = queryProcessor.fetchOneRaw(
        "SELECT user_id, login_count, verified FROM user_statistics WHERE user_id = ?",
        mapper,
        userId
);
```

Untuk pagination, mapper yang sama diteruskan ke
`queryProcessor.fetchPage(query, mapper, pageable)`. Buat `QueryBuilder` baru untuk
setiap eksekusi, terutama karena pagination mengubah state query builder.
Contoh pemanggilan di atas merupakan alternatif operasi repository, bukan kewajiban
menjalankan semua query untuk membaca data yang sama.

Nullability kolom tidak menentukan apakah sebuah baris ditemukan. Pada
`optionalFetchOneBy` dan `fetchOneRaw`, tidak adanya baris menghasilkan
`Optional.empty()`. Jika baris ditemukan tetapi kolom mandatory berisi SQL NULL,
mapping tetap gagal; exception tersebut tidak diubah menjadi optional kosong.
Gunakan method single-row sesuai jaminan jumlah baris query. Semantik `fetchOne`
yang mengambil baris pertama berbeda dari `fetchOneBy` yang memakai operasi JDBC
single-row; builder tidak mengubah semantik tersebut.

#### Mapping object mutable

Untuk class dengan constructor kosong dan setter, buat object baru di dalam
callback untuk setiap baris. Contoh berikut mengasumsikan `UserStatisticsBean`
memiliki setter `setId(Long)`, `setLoginCount(Integer)`, dan `setVerified(Boolean)`:

```java
RowMapper<UserStatisticsBean> beanMapper = JdbcResultSetBuilder.<UserStatisticsBean>builder()
        .map((row, rowNumber) -> {
            UserStatisticsBean bean = new UserStatisticsBean();
            bean.setId(row.getMandatory("user_id", Long.class));
            bean.setLoginCount(row.getNullable("login_count", Integer.class));
            bean.setVerified(row.getNullable("verified", Boolean.class));
            return bean;
        })
        .build();
```

Jangan menggunakan satu instance bean bersama untuk semua baris; setiap elemen
hasil harus mewakili object yang dibuat untuk barisnya sendiri. Kolom nullable
memerlukan property wrapper agar nilai null bisa disimpan.

#### Alias kolom dan outer join

Gunakan alias SELECT sebagai label pembacaan, terutama saat join menghasilkan
nama kolom yang sama. Contoh ini menambahkan tabel `user_profile(user_id, nickname)`
dan record `UserProfile(Long id, String username, String nickname)`:

```java
RowMapper<UserProfile> profileMapper = JdbcResultSetBuilder.<UserProfile>builder()
        .map((row, rowNumber) -> new UserProfile(
                row.getMandatory("user_id", Long.class),
                row.getMandatory("username", String.class),
                row.getNullable("profile_nickname", String.class)
        ))
        .build();

List<UserProfile> profiles = queryProcessor.fetchRaw(
        """
        SELECT u.id AS user_id, u.username AS username, p.nickname AS profile_nickname
        FROM app_user u
        LEFT JOIN user_profile p ON p.user_id = u.id
        WHERE u.status = ?
        """,
        profileMapper,
        "ACTIVE"
);
```

Jika user tidak memiliki profile, `profile_nickname` bernilai SQL NULL meskipun
kolom tabel `user_profile.nickname` didefinisikan NOT NULL. Karena itu, pilih
`getNullable` berdasarkan hasil SELECT. Gunakan label `profile_nickname`, bukan
ekspresi `p.nickname`. Nama label dibaca sebagaimana diberikan tanpa trimming;
sesuaikan casing dan aturan alias dengan driver database.

#### Tipe nilai dan konversi

Gunakan label kolom atau alias SELECT, serta wrapper `Integer.class`, `Long.class`,
dan `Boolean.class` agar SQL NULL tidak berubah menjadi `0` atau `false`.
`int.class`, tipe primitive lainnya, `Void.class`, nama kolom null/kosong, dan tipe
null ditolak. Jangan langsung melakukan unboxing hasil nullable tanpa penanganan
null yang eksplisit.

| Tipe Java | Token untuk pembacaan | Perilaku konversi |
|---|---|---|
| String | `String.class` | JDBC typed conversion |
| Angka wrapper | `Integer.class`, `Long.class`, `Short.class`, `Byte.class`, `Double.class`, `Float.class` | JDBC typed conversion; nullable mempertahankan null |
| Boolean wrapper | `Boolean.class` | JDBC typed conversion; representasi database mengikuti dukungan driver |
| Nilai decimal | `BigDecimal.class` | JDBC typed conversion; cocok untuk nilai decimal yang memerlukan presisi |
| Nilai binary | `byte[].class` | JDBC typed conversion |
| JDBC temporal | `java.sql.Date.class`, `java.sql.Time.class`, `java.sql.Timestamp.class` | JDBC typed conversion |
| Local date | `LocalDate.class` | `getDate(column)` lalu `toLocalDate()` |
| Local date/time | `LocalDateTime.class` | `getTimestamp(column)` lalu `toLocalDateTime()` |
| Local time dan reference type lain | Misalnya `LocalTime.class` atau `UUID.class` | JDBC typed conversion jika driver mendukung tipe tersebut |

`LocalDate` dibaca melalui JDBC `Date`, dan `LocalDateTime` melalui JDBC `Timestamp`
agar nanosecond timestamp tetap terjaga. Tidak ada konversi zona waktu eksplisit;
interpretasi awal nilai tetap mengikuti driver dan konfigurasi database. Tipe lain,
termasuk `BigDecimal`, `LocalTime`, dan tipe khusus driver, menggunakan
`ResultSet.getObject(column, type)`; dukungan konversinya mengikuti driver JDBC.

Builder tidak melakukan konversi custom seperti string `"ACTIVE"` menjadi enum
atau karakter `"Y"` menjadi boolean. Baca tipe yang didukung driver terlebih
dahulu, lalu lakukan konversi domain di callback. Jika database menyimpan waktu
dengan offset atau zona waktu, pilih tipe reference dan driver yang mendukung
semantik tersebut; jangan menganggap `LocalDateTime` mempertahankan offset.

#### Default domain untuk kolom nullable

Jika model domain memakai primitive, tentukan default secara eksplisit setelah
membaca wrapper. Contoh berikut mengasumsikan record
`UserMetrics(long id, int loginCount, boolean verified)` dan kebijakan aplikasi
bahwa SQL NULL berarti nol login serta belum terverifikasi:

```java
RowMapper<UserMetrics> metricsMapper = JdbcResultSetBuilder.<UserMetrics>builder()
        .map((row, rowNumber) -> {
            Integer loginCount = row.getNullable("login_count", Integer.class);
            Boolean verified = row.getNullable("verified", Boolean.class);
            return new UserMetrics(
                    row.getMandatory("user_id", Long.class),
                    loginCount != null ? loginCount : 0,
                    Boolean.TRUE.equals(verified)
            );
        })
        .build();
```

Default pada contoh ini adalah kebijakan domain, bukan default builder. Gunakan
record dengan wrapper jika perbedaan antara null dan `0`/`false` perlu dipertahankan.

#### Lifecycle dan penggunaan ulang

`build()` memerlukan callback non-null. Mapper yang sudah dibangun menyimpan
callback saat itu sehingga perubahan builder berikutnya tidak memengaruhinya.
Builder mutable tidak thread-safe; mapper dapat dipakai lintas thread jika
callback-nya thread-safe. `SQLException` dan runtime exception callback diteruskan
tanpa membungkus ulang. Callback boleh mengembalikan null sesuai kontrak Spring
`RowMapper`; ini terpisah dari nullability tiap kolom.

Bangun mapper stateless sekali pada field `private static final RowMapper<T>`
seperti contoh repository utama. Simpan hanya mapper yang sudah dibangun; hindari
builder bersama atau callback yang menangkap state mutable antar-request. Untuk
object mutable, pembuatan instance di dalam callback juga berlaku saat mapper
digunakan paralel.

#### Kegagalan dan troubleshooting

| Kondisi | Hasil | Perbaikan |
|---|---|---|
| `.build()` sebelum `.map(...)` | `IllegalStateException` | Pasang callback sebelum build |
| Callback pada `.map(null)` | `NullPointerException` | Berikan callback non-null |
| Label atau tipe null | `NullPointerException` | Berikan label dan class token yang valid |
| Label kosong/blank, class token primitive, atau `Void.class` | `IllegalArgumentException` | Gunakan alias yang valid dan tipe reference |
| Kolom mandatory bernilai SQL NULL | `SQLDataException`, SQL state `22004` | Periksa proyeksi, data, atau nullability hasil join |
| Kolom tidak ada atau tipe tidak dapat dikonversi | `SQLException` dari driver | Periksa SELECT/alias dan dukungan konversi driver |
| Unboxing nullable tanpa default | `NullPointerException` dari callback | Pertahankan wrapper atau tetapkan default domain |
| JSON kosong, invalid, atau bentuknya tidak cocok dengan tipe tujuan | `SQLDataException`, SQL state `22000` | Periksa data dan decoder; cause parsing tetap tersedia |
| JSON mandatory berupa literal `null` atau hasil decoding null | `SQLDataException`, SQL state `22004` | Perbaiki data atau pilih kontrak nullable |
| JSON melebihi batas pembaca | `SQLDataException`, SQL state `22001` | Pilih batas yang sesuai kebutuhan aplikasi atau kurangi ukuran dokumen |
| Constructor, setter, atau konversi domain gagal | Runtime exception dari callback | Perbaiki validasi atau konversi domain |

Saat memanggil mapper secara langsung, misalnya dalam unit test,
`mapper.mapRow(resultSet, rowNumber)` meneruskan `SQLException` asli. Saat dipakai
melalui `JdbcTemplate` pada implementasi default `QueryProcessor`, exception JDBC
melewati mekanisme penerjemahan exception Spring menjadi `DataAccessException`.
Jenis exception Spring mengikuti translator dan driver; jangan mengandalkan bahwa
`SQLDataException` selalu muncul sebagai exception terluar. Pertahankan exception
dan cause saat menerjemahkan kegagalan ke kontrak aplikasi. Jangan mencatat nilai
kolom sensitif atau seluruh hasil query untuk diagnosis.

#### Mapping kolom JSON dan nested JSON

Gunakan `getNullableJson(column, decoder)` atau
`getMandatoryJson(column, decoder)` untuk mapping JSON. JSON tidak dideteksi
otomatis oleh `getNullable(column, type)` maupun `getMandatory(column, type)`;
method JSON membuat decoding eksplisit pada setiap kolom.

Pembacaan JDBC dan decoding merupakan dua kontrak terpisah:

| Komponen | Kontrak |
|---|---|
| `JdbcJsonColumnReader` | Membaca satu kolom menjadi JSON text; hanya SQL NULL yang menghasilkan null |
| `JdbcJsonDecoder<T>` | Mengubah dokumen JSON utuh menjadi `T`, termasuk struktur bersarang dan generic collection |

Kontrak tersebut tersedia di package
`reyga.starter.foundation.common_database.util`. `common-database` tidak memakai
tipe JSON vendor dalam API publik dan tidak menentukan library parser. Decoder
bisa berupa lambda atau method reference ke parser yang dikonfigurasi consumer.
Tidak ada bean atau property aktivasi baru. `JdbcResultSetBuilder` juga tidak
membuat parser baru pada setiap baris.

Contoh berikut memakai tabel `customer_document` dengan kolom mandatory `id` dan
kolom JSON `profile`. Dokumen `profile` berisi nested object, array object, dan map
dengan nilai array:

```json
{
  "displayName": "Customer",
  "address": {"city": "Jakarta", "postalCode": "10110"},
  "contacts": [{"type": "email", "value": "example@test.invalid"}],
  "preferences": {"notifications": ["EMAIL", "SMS"]}
}
```

Siapkan model domain berikut; letakkan setiap public record pada file Java
tersendiri dan import tipe domainnya di repository:

```java
public record CustomerDocument(Long id, CustomerProfile profile) {
}
```

```java
import java.util.List;
import java.util.Map;

public record CustomerProfile(
        String displayName,
        CustomerAddress address,
        List<CustomerContact> contacts,
        Map<String, List<String>> preferences
) {
}
```

```java
public record CustomerAddress(String city, String postalCode) {
}
```

```java
public record CustomerContact(String type, String value) {
}
```

Contoh adapter decoder memakai Jackson 3, sesuai lini Spring Boot aplikasi ini.
Tipe Jackson hanya dipakai oleh kode consumer; signature API foundation memakai
`JdbcJsonDecoder<T>`. Gunakan instance parser aplikasi yang sudah dikonfigurasi dan
bangun reader sebelum callback:

```java
import java.util.List;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common_database.util.JdbcJsonDecoder;
import reyga.starter.foundation.common_database.util.JdbcResultSetBuilder;

// Gunakan JsonMapper aplikasi yang sudah dikonfigurasi; lakukan ini sekali saat
// menyiapkan mapper, bukan untuk setiap baris hasil query.
ObjectReader profileReader = jsonMapper.readerFor(CustomerProfile.class)
        .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
JdbcJsonDecoder<CustomerProfile> profileDecoder = profileReader::readValue;

RowMapper<CustomerDocument> documentMapper = JdbcResultSetBuilder.<CustomerDocument>builder()
        .map((row, rowNumber) -> new CustomerDocument(
                row.getMandatory("id", Long.class),
                row.getMandatoryJson("profile", profileDecoder)
        ))
        .build();

List<CustomerDocument> documents = queryProcessor.fetchRaw(
        "SELECT id, profile FROM customer_document WHERE id = ?",
        documentMapper,
        customerId
);
```

Field `address` menjadi `CustomerAddress`, setiap elemen `contacts` menjadi
`CustomerContact`, dan `preferences` mempertahankan bentuk `Map<String, List<String>>`.
Parser tidak melakukan flattening otomatis. Aturan field JSON yang hilang,
unknown property, format tanggal, enum, dan konversi angka mengikuti konfigurasi
decoder. Mandatory pada kolom JSON hanya memvalidasi dokumen dan hasil decoding
non-null, bukan mewajibkan setiap property nested; jalankan validasi domain bila
property tertentu wajib ada.

Untuk JSON array sebagai root atau tipe generic lain, tangkap tipe lengkap dalam
decoder, misalnya memakai `TypeReference` Jackson 3:

```java
import java.util.List;
import tools.jackson.core.type.TypeReference;

ObjectReader profilesReader = jsonMapper.readerFor(new TypeReference<List<CustomerProfile>>() {})
        .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
JdbcJsonDecoder<List<CustomerProfile>> profilesDecoder = profilesReader::readValue;

RowMapper<List<CustomerProfile>> profilesMapper = JdbcResultSetBuilder.<List<CustomerProfile>>builder()
        .map((row, rowNumber) -> row.getMandatoryJson("profiles", profilesDecoder))
        .build();
```

`profilesMapper` memetakan satu kolom array menjadi satu `List<CustomerProfile>`
per baris JDBC. `queryProcessor.fetchRaw(sql, profilesMapper)` mengembalikan
`List<List<CustomerProfile>>`; builder tidak menggabungkan array dari beberapa
baris. Untuk `Map<String, List<CustomerProfile>>`, gunakan `TypeReference` dengan
tipe tersebut. Hindari `List.class` jika tipe elemen harus dipertahankan.

#### Memilih pembaca JSON berdasarkan penyimpanan

Default builder adalah `JdbcJsonColumnReaders.text()`, menggunakan
`ResultSet.getString(column)`. Pembaca standar menggunakan JDBC tanpa menebak nama
vendor atau memanggil `toString()` pada object vendor:

| Pembaca | Getter JDBC | Penggunaan |
|---|---|---|
| `text()` | `getString(column)` | Kolom text dan JSON native jika driver mendukung konversi ke teks |
| `characterStream()` | `getCharacterStream(column)` | JSON dalam text/CLOB melalui character stream; NCLOB bergantung pada dukungan getter driver |
| `utf8BinaryStream()` | `getBinaryStream(column)` | JSON text yang disimpan sebagai byte UTF-8 dalam binary/BLOB |
| Implementasi consumer | Dipilih consumer | Konversi native atau cara baca khusus driver yang tidak tersedia melalui pembaca standar |

Pembaca UTF-8 tidak dapat membaca format binary JSON vendor seperti OSON atau
format compressed secara langsung. Gunakan konversi JSON-to-text yang didukung
driver/database atau reader khusus. Dukungan MySQL, Oracle, PostgreSQL, dan
database lain tetap bergantung pada versi driver, tipe penyimpanan, dan getter
yang dipilih; penggunaan JDBC bukan jaminan semua kombinasi tersebut identik.

Semua pembaca standar memakai batas default **1.048.576 UTF-16 code unit Java**,
bukan byte. Batas ini dapat disesuaikan lewat overload dengan `maxCharacters` yang
positif. Pembaca stream membatasi buffer dokumen selama pembacaan dan menutup
stream pada keberhasilan maupun kegagalan. `text()` memeriksa panjang setelah
driver membentuk String, sehingga gunakan stream untuk membatasi buffering LOB
besar. Tidak ada fallback otomatis antar-getter yang dapat menyembunyikan error
driver.

Pilih satu pembaca default untuk mapper bila seluruh kolom JSON memakai cara
penyimpanan yang sama:

```java
import reyga.starter.foundation.common_database.util.JdbcJsonColumnReaders;

RowMapper<CustomerDocument> clobMapper = JdbcResultSetBuilder.<CustomerDocument>builder()
        .jsonColumnReader(JdbcJsonColumnReaders.characterStream(2_097_152))
        .map((row, rowNumber) -> new CustomerDocument(
                row.getMandatory("id", Long.class),
                row.getMandatoryJson("profile", profileDecoder)
        ))
        .build();
```

Untuk kolom dengan penyimpanan berbeda pada mapper yang sama, gunakan overload
`getNullableJson(column, reader, decoder)` atau
`getMandatoryJson(column, reader, decoder)`. Siapkan reader sekali sebelum callback:

```java
import reyga.starter.foundation.common_database.util.JdbcJsonColumnReader;

JdbcJsonColumnReader blobReader = JdbcJsonColumnReaders.utf8BinaryStream();
RowMapper<CustomerProfile> blobMapper = JdbcResultSetBuilder.<CustomerProfile>builder()
        .map((row, rowNumber) -> row.getNullableJson("profile_blob", blobReader, profileDecoder))
        .build();
```

Reader custom wajib mempertahankan SQL NULL, mengonsumsi dan menutup stream yang
dibukanya, serta tidak menggeser/menutup ResultSet. Reader dan decoder yang dipakai
ulang harus thread-safe. `build()` menangkap reader dan callback saat itu sehingga
perubahan builder berikutnya tidak memengaruhi mapper lama.

#### Null dan kegagalan decoding JSON

| Nilai kolom | `getNullableJson` | `getMandatoryJson` |
|---|---|---|
| SQL NULL | Java null; decoder tidak dipanggil | Gagal dengan SQL state `22004`; decoder tidak dipanggil |
| JSON literal `null` | Diteruskan ke decoder; hasil bisa Java null atau node JSON null | Gagal dengan SQL state `22004`; decoder tidak dipanggil |
| String kosong/blank | Gagal dengan SQL state `22000` | Gagal dengan SQL state `22000` |
| JSON valid dengan bentuk cocok | Nilai hasil decoder | Nilai hasil decoder yang non-null |
| `{}` atau `[]` | Diterima hanya jika decoder mendukung bentuk tersebut | Sama, serta hasil decoding harus non-null |
| JSON invalid, bentuk salah, atau kegagalan decoder | `SQLDataException`, SQL state `22000`, cause asli dipertahankan | Sama |

Nullable tidak berarti mengabaikan JSON yang invalid. Untuk membedakan SQL NULL
dan JSON literal `null`, consumer dapat memasang decoder tree seperti
`jsonMapper::readTree`; SQL NULL tetap Java null, sedangkan JSON `null` dapat menjadi
node null. Decoder yang menghasilkan null dari dokumen non-null akan ditolak oleh
method mandatory.

Exception JDBC dari reader diteruskan tanpa membungkus ulang. `IOException` dan
runtime exception dari decoder dibungkus menjadi `SQLDataException`; melalui
`JdbcTemplate`, exception tersebut mengikuti penerjemahan Spring sebagaimana
dijelaskan pada troubleshooting. Message pembungkus hanya memuat nama kolom dan
konteks kegagalan, tetapi cause parser bisa memuat potongan payload; hindari
pencatatan payload sensitif. Atur batas nesting/token dan kebijakan polymorphism
pada parser consumer. Decoder juga harus menolak input trailing atau bentuk yang
tidak sesuai kontrak aplikasi; contoh Jackson di atas mengaktifkan pemeriksaan
trailing tokens secara eksplisit.

#### Migrasi mapper lama

API `QueryProcessor` tetap menerima `RowMapper` untuk kompatibilitas. Semua mapper
yang dibuat melalui builder baru wajib menggunakan pembacaan helper, sedangkan
mapper lama dan `MapperColumnProcessor` tetap dapat digunakan. Getter static lama
`JdbcResultSetMapper.Nullable` dan `Mandatory` mempertahankan perilakunya; khususnya
getter mandatory lama belum memvalidasi SQL NULL. Untuk pembacaan strict di kode
lama, gunakan `Nullable.getObject(resultSet, column, type)` atau
`Mandatory.getObject(resultSet, column, type)`.

Untuk memigrasikan mapper manual, pindahkan konstruksi object ke callback builder,
ganti pembacaan langsung `resultSet.getXxx(...)` dengan pembacaan typed helper,
dan tentukan nullability setiap label secara eksplisit:

| Pembacaan sebelumnya | Pengganti saat memakai builder |
|---|---|
| `resultSet.getLong("id")` untuk ID wajib | `row.getMandatory("id", Long.class)` |
| `Nullable.getInt(resultSet, "login_count")` | `row.getNullable("login_count", Integer.class)` dan tangani null sesuai domain |
| `Mandatory.getString(resultSet, "username")` | `row.getMandatory("username", String.class)` |
| `resultSet.getTimestamp("created_at").toLocalDateTime()` | `row.getMandatory("created_at", LocalDateTime.class)` atau versi nullable sesuai hasil query |

Pemindahan getter mandatory lama ke pembacaan strict dapat mengungkap data SQL NULL
yang sebelumnya lolos sebagai null atau default JDBC. Pemindahan getter nullable
primitive mempertahankan null, sehingga kode consumer harus menangani nilainya
sebelum unboxing. Migrasi ini adalah pilihan consumer; API lama belum dihapus.

### Logging metadata eksekusi

Implementasi default mencatat satu event `DATABASE_QUERY` pada level `DEBUG` setelah
setiap operasi JDBC selesai. Event hanya memuat metadata operasional, misalnya:

```text
| {DATABASE_QUERY=operation=SELECT | execution=FETCH_MANY | outcome=SUCCESS | durationMs=4 | rows=3} |
```

Untuk batch, metadata juga merangkum jumlah hasil batch, baris yang terpengaruh,
hasil tanpa jumlah pasti, dan hasil yang gagal. Pada kegagalan, event memuat tipe
exception tanpa message. SQL, nama tabel atau kolom, bind parameter, exception
message, dan data hasil mapping tidak pernah dicatat oleh `DefaultQueryProcessor`.
Aktifkan level `DEBUG` untuk class tersebut hanya ketika metadata diagnosis database
dibutuhkan; event tidak tampil pada konfigurasi `INFO` production:

```yaml
logging:
  level:
    reyga.starter.foundation.common_database.processor.DefaultQueryProcessor: DEBUG
```

## F. QueryBuilder

### Alias dengan as

Alias bersifat **opsional**. Penambahan `.as(...)` tidak mengubah SQL atau urutan
parameter dari builder existing yang tidak memanggilnya. `select`, `from`, JOIN,
INSERT, UPDATE, DELETE, pagination, dan count query tetap dapat digunakan seperti
sebelumnya. Alias inline seperti `.select("u.id AS user_id")`, `.from("app_user u")`,
`.join("roles r")`, maupun `.join("roles", "r")` tetap didukung; tidak perlu migrasi.
Pola lama dan `.as(...)` dapat digunakan pada query yang sama untuk expression
berbeda. Jangan menambahkan `.as(...)` ke expression yang sudah memiliki alias.
Validasi alias baru hanya berlaku ketika `.as(...)` dipanggil.

Contoh tanpa alias, SQL yang akan dibangun:

```sql
SELECT id, username FROM app_user WHERE status = ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("id", "username")
        .from("app_user")
        .where().equals("status", "ACTIVE").done();
```

Contoh dengan alias inline existing, SQL yang akan dibangun:

```sql
SELECT u.id AS user_id FROM app_user u WHERE u.status = ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("u.id AS user_id")
        .from("app_user u")
        .where().equals("u.status", "ACTIVE").done();
```

Gunakan `.as("alias")` segera setelah `select(...)` atau `from(...)`, serta pada
builder JOIN. Jika `select` menerima beberapa kolom, alias berlaku hanya pada
kolom terakhir; panggil `select` lagi untuk projection berikutnya. Alias harus
identifier sederhana tanpa spasi, qualification, atau quoting. Alias kosong,
null, dan fragment SQL ditolak. Pemanggilan tanpa target atau berulang ditolak.
Gunakan expression sumber tanpa alias sebelumnya; builder tidak mem-parsing SQL
mentah. Alias kolom memakai `AS`; alias tabel memakai spasi tanpa `AS` untuk Oracle.
Alias merupakan konstanta aplikasi, bukan input pengguna. Binding tetap sama.

SQL yang akan dibangun:

```sql
SELECT u.id AS user_id, u.username AS login_name
FROM app_user u
LEFT JOIN user_role r ON u.id = r.user_id
WHERE u.status = ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("u.id").as("user_id")
        .select("u.username").as("login_name")
        .from("app_user").as("u")
        .join(QueryJoinType.LEFT, "user_role").as("r")
            .on("u.id", QueryOperator.EQUALS, "r.user_id").done()
        .where().equals("u.status", "ACTIVE").done();
```

### Operator query bertipe aman

Import `reyga.starter.foundation.common_database.enumeration.QueryOperator`. Enum ini
membatasi operator SQL yang didukung builder. `QueryJoinType` tetap menentukan
jenis JOIN, sedangkan `QueryOperator` menentukan predikatnya.

| Operator | SQL | Jumlah nilai pada `condition(...)` |
|---|---|---|
| `EQUALS`, `NOT_EQUALS` | `=`, `<>` | 1 |
| `GREATER_THAN`, `GREATER_THAN_OR_EQUALS` | `>`, `>=` | 1 |
| `LESS_THAN`, `LESS_THAN_OR_EQUALS` | `<`, `<=` | 1 |
| `LIKE`, `NOT_LIKE` | `LIKE`, `NOT LIKE` | 1 pattern |
| `IN`, `NOT_IN` | `IN`, `NOT IN` | Minimal 1 |
| `BETWEEN`, `NOT_BETWEEN` | `BETWEEN`, `NOT BETWEEN` | 2 |
| `IS_NULL`, `IS_NOT_NULL` | `IS NULL`, `IS NOT NULL` | 0 |
| `IS_TRUE`, `IS_NOT_TRUE` | `IS TRUE`, `IS NOT TRUE` | 0 |
| `IS_FALSE`, `IS_NOT_FALSE` | `IS FALSE`, `IS NOT FALSE` | 0 |
| `IS_UNKNOWN`, `IS_NOT_UNKNOWN` | `IS UNKNOWN`, `IS NOT UNKNOWN` | 0 |
| `IS_DISTINCT_FROM`, `IS_NOT_DISTINCT_FROM` | `IS DISTINCT FROM`, `IS NOT DISTINCT FROM` | 1 |

Daftar ini mencakup seluruh operator predikat yang dimodelkan builder, bukan seluruh
operator setiap dialect SQL. Operator aritmetika, JSON, regex, serta subquery
`ANY`/`ALL` tidak dimodelkan. `EXISTS` tersedia melalui `WhereBuilder.exists`. Kombinasi logika menggunakan `.and()`/`.or()`;
negasi menggunakan operator `NOT_*` yang sesuai. Dukungan predikat boolean dan
DISTINCT bergantung pada database target.

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
SELECT u.id, r.name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.age >= ? AND u.status IN (?, ?) AND u.deleted_at IS NULL
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("u.id", "r.name")
        .from("users").as("u")
        .join(QueryJoinType.LEFT, "roles", "r")
            .on("u.role_id", QueryOperator.EQUALS, "r.id")
        .done()
        .where()
            .condition("u.age", QueryOperator.GREATER_THAN_OR_EQUALS, 18)
            .and().condition("u.status", QueryOperator.IN, "ACTIVE", "PENDING")
            .and().condition("u.deleted_at", QueryOperator.IS_NULL)
        .done();
```

Query ini dapat dieksekusi melalui `queryProcessor.fetch(query, ROW_MAPPER)`.
Tambahkan `.paginate(pageable)` untuk SQL paginated. `buildCount()` tidak
menyertakan LIMIT/OFFSET dan `getCountParameters()` hanya berisi parameter filter.
Untuk hasil `Page<T>`, gunakan kontrak pagination QueryProcessor pada bagian
pagination panduan ini.

Semua nilai `condition(...)` dibind sebagai parameter JDBC. Sebaliknya, kedua
operand `on(left, operator, right)` adalah ekspresi SQL/kolom terpercaya, bukan nilai
request. ON bertipe menerima operator dengan satu operand kanan saja; IN, BETWEEN,
dan predikat unary ditolak. Operator null, jumlah nilai salah, serta ekspresi kosong
ditolak sebelum kondisi dan parameter ditambahkan.

`condition("name", QueryOperator.LIKE, "Rey%")` mempertahankan pattern input.
Helper `.like("name", "Rey")` dan `.notLike(...)` menambahkan `%` di kedua sisi.
Helper tambahan: `greaterThanOrEquals`, `lessThanOrEquals`, `notBetween`, `notIn`.
Nilai SQL null harus dikirim sebagai `(Object) null`, bukan array varargs null;
gunakan `IS_NULL` untuk pengujian null, bukan `EQUALS`.


### SELECT dengan filter, join, grouping, dan ordering

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
SELECT u.id, u.username, COUNT(r.id) AS role_count
FROM app_user u
INNER JOIN user_role r ON u.id = r.user_id
WHERE u.status = ? AND u.username IS NOT NULL
GROUP BY u.id, u.username
ORDER BY u.username ASC
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("u.id", "u.username", QueryFunction.count("r.id")).as("role_count")
        .from("app_user").as("u")
        .join("user_role").as("r")
            .on("u.id", QueryOperator.EQUALS, "r.user_id")
        .done()
        .where()
            .equals("u.status", "ACTIVE")
            .and()
            .isNotNull("u.username")
        .done()
        .groupBy("u.id", "u.username")
        .orderBy("u.username ASC");

List<UserSummary> users = queryProcessor.fetch(query, userSummaryRowMapper);
```

Nilai `ACTIVE` disimpan sebagai JDBC parameter. Nama tabel, kolom, join condition, function, dan
expression pada `orderBy` adalah SQL fragment dan harus berasal dari constant atau source yang
dipercaya.

### INSERT

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
INSERT INTO app_user (id, username, status) VALUES (?, ?, ?)
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .insertInto("app_user", "id", "username", "status")
        .values(user.id(), user.username(), user.status());

int affectedRows = queryProcessor.execute(query);
```

Periksa `affectedRows` sesuai kebutuhan operasi, misalnya tepat satu baris untuk perubahan berdasarkan ID.

### UPDATE

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
UPDATE app_user SET username = ?, status = ? WHERE id = ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .update("app_user")
        .set("username", newUsername)
        .set("status", newStatus)
        .where()
            .equals("id", userId)
        .done();

int affectedRows = queryProcessor.execute(query);
```

### DELETE

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
DELETE FROM app_user WHERE id = ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .deleteFrom("app_user")
        .where()
            .equals("id", userId)
        .done();

int affectedRows = queryProcessor.execute(query);
```

## G. Pagination dengan Pageable dan Page

Spring Data `Pageable` menggunakan page number zero-based. Page pertama adalah `0`.

Hasil `Page<User>` menyediakan konten halaman dan jumlah total baris. Untuk request
paged, data dan total dihitung melalui query berikut:

```sql
SELECT id, username, status, created_at
FROM app_user
WHERE status = ?
ORDER BY created_at DESC, id ASC
LIMIT ? OFFSET ?
```

```sql
SELECT COUNT(*) FROM (
    SELECT id, username, status, created_at
    FROM app_user
    WHERE status = ?
) query_count
```

Penggunaan QueryBuilder:

```java
Pageable pageable = PageRequest.of(
        0,
        20,
        Sort.by(
                Sort.Order.desc("created_at"),
                Sort.Order.asc("id")
        )
);

QueryBuilder query = QueryBuilder.builder()
        .select("id", "username", "status", "created_at")
        .from("app_user")
        .where()
            .equals("status", "ACTIVE")
        .done();

Page<User> page = queryProcessor.fetchPage(query, USER_ROW_MAPPER, pageable);
```

Count query membungkus SELECT utama agar semantik `DISTINCT`, join, dan `GROUP BY` tetap terjaga.
Parameter `LIMIT` dan `OFFSET` tidak ikut diteruskan ke count query.

### Pageable.unpaged()

```java
Pageable pageable = Pageable.unpaged(Sort.by("username"));
Page<User> page = queryProcessor.fetchPage(query, USER_ROW_MAPPER, pageable);
```

`Pageable.unpaged()` mengambil seluruh hasil tanpa batas halaman. Gunakan hanya
jika jumlah data terkendali.

### Pagination dengan nomor halaman

Jika tidak memakai `Pageable`, gunakan nomor halaman yang dimulai dari 1:

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
SELECT * FROM app_user LIMIT ? OFFSET ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("*")
        .from("app_user")
        .paginate(1, 20); // one-based: page pertama adalah 1
```

Gunakan `paginate(Pageable)` atau `QueryProcessor.fetchPage(...)` untuk implementasi baru.

### SQL dialect

Pagination builder saat ini menghasilkan `LIMIT ? OFFSET ?`. Pastikan dialect database yang
digunakan mendukung syntax tersebut. Untuk database yang menggunakan syntax lain, gunakan raw SQL
native seperti contoh raw pagination pada bagian berikut dan bentuk `PageImpl` secara eksplisit.

## H. Raw Query

Raw query digunakan ketika query bersifat database-specific atau belum dapat direpresentasikan
oleh `QueryBuilder`. Parameter value tetap wajib dikirim melalui placeholder.

### Raw SELECT

```java
String sql = """
        SELECT id, username, status, created_at
        FROM app_user
        WHERE status = ?
        ORDER BY created_at DESC
        """;

List<User> users = queryProcessor.fetchRaw(
        sql,
        USER_ROW_MAPPER,
        "ACTIVE"
);
```

### Raw single-row SELECT

```java
Optional<User> user = queryProcessor.fetchOneRaw(
        "SELECT id, username, status, created_at FROM app_user WHERE id = ?",
        USER_ROW_MAPPER,
        userId
);
```

### Raw UPDATE atau DELETE

```java
int affectedRows = queryProcessor.executeRaw(
        "UPDATE app_user SET status = ? WHERE id = ?",
        "INACTIVE",
        userId
);
```

### Raw pagination untuk dialect khusus

Contoh ini memerlukan `pageable.isPaged() == true`. Gunakan raw SELECT biasa untuk
permintaan tanpa pagination. Contoh berikut menggunakan syntax `OFFSET ... ROWS FETCH NEXT ... ROWS ONLY`.

```java
String contentSql = """
        SELECT id, username, status, created_at
        FROM app_user
        WHERE status = ?
        ORDER BY created_at DESC
        OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        """;

String countSql = """
        SELECT COUNT(*) AS total_count
        FROM app_user
        WHERE status = ?
        """;

List<User> content = queryProcessor.fetchRaw(
        contentSql,
        USER_ROW_MAPPER,
        "ACTIVE",
        pageable.getOffset(),
        pageable.getPageSize()
);

long total = queryProcessor.fetchOneRaw(
        countSql,
        JdbcResultSetBuilder.<Long>builder()
                .map((row, rowNumber) -> row.getMandatory("total_count", Long.class))
                .build(),
        "ACTIVE"
).orElse(0L);

Page<User> result = new PageImpl<>(content, pageable, total);
```

Jangan menambahkan nilai filter atau sort dari request langsung ke string SQL. Untuk sort raw,
petakan property API ke daftar kolom yang diizinkan terlebih dahulu.

```java
Map<String, String> allowedSortColumns = Map.of(
        "username", "username",
        "createdAt", "created_at"
);

String requestedProperty = pageable.getSort().iterator().next().getProperty();
String databaseColumn = Optional.ofNullable(allowedSortColumns.get(requestedProperty))
        .orElseThrow(() -> new IllegalArgumentException("Unsupported sort property"));
```

## I. QueryFunction

`QueryFunction` menghasilkan SQL expression dan dapat digunakan pada `select`, condition,
grouping, atau ordering.

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
SELECT id, UPPER(username) AS normalized_username, COALESCE(status, 'UNKNOWN') AS resolved_status, CURRENT_TIMESTAMP AS queried_at, TO_DATE('2026-01-01', 'YYYY-MM-DD') AS cutoff_date
FROM app_user
WHERE UPPER(username) = ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .select(
                "id",
                QueryFunction.upper("username") + " AS normalized_username",
                QueryFunction.coalesce("status", "UNKNOWN") + " AS resolved_status",
                QueryFunction.currentTimestamp() + " AS queried_at",
                QueryFunction.toDate("2026-01-01", "YYYY-MM-DD") + " AS cutoff_date"
        )
        .from("app_user")
        .where()
            .equals(QueryFunction.upper("username"), normalizedUsername)
        .done();
```

Perhatikan bahwa condition builder memperlakukan value sebagai bind parameter. Jika database
harus mengevaluasi expression seperti `TO_TIMESTAMP(...)` sebagai SQL, expression tersebut perlu
ditulis sebagai bagian dari trusted raw SQL. Jangan menganggap string hasil `QueryFunction`
otomatis dieksekusi ketika ditempatkan sebagai value parameter.

### Fungsi umum dan fungsi database

Helper umum tambahan: `sum`, `average`, `minimum`, `maximum`, `absolute`,
`round`, `trim`, `replace`, dan `nullIf`.

Pilih kelompok fungsi sesuai database yang digunakan; library tidak mendeteksi dialect
melalui koneksi dan tidak menerjemahkan ekspresi secara otomatis.

| Kelompok | Helper |
|---|---|
| `QueryFunction.PostgreSql` | `dateTrunc`, `toChar`, `stringAgg`, `jsonExtractPathText`, `jsonbExtractPathText`, `jsonAgg` |
| `QueryFunction.MySql` | `ifNull`, `dateFormat`, `strToDate`, `groupConcat`, `jsonExtract`, `jsonUnquote`, `jsonArrayAgg`, `concatWs` |
| `QueryFunction.Oracle` | `toChar`, `trunc`, `addMonths`, `monthsBetween`, `listAgg`, `jsonValue`, `jsonQuery` |
| `QueryFunction.SqlServer` | `isNull`, `len`, `dateAdd`, `dateDiff`, `stringAgg`, `jsonValue`, `jsonQuery` |
| `QueryFunction.Sqlite` | `strftime`, `ifNull`, `groupConcat`, `jsonExtract`, `jsonGroupArray` |

Semua argumen helper baru merupakan **SQL expression**, termasuk angka, format, delimiter,
dan JSON path. Literal SQL harus sudah diberi tanda kutip; jangan memasukkan input pengguna.
Berbeda dengan API lama `coalesce(column, defaultValue)` dan `toDate(value, format)`,
helper baru tidak mengubah Java value menjadi literal. Null Java ditolak dengan
`NullPointerException`; argument kosong ditolak dengan `IllegalArgumentException`.
Gunakan string `"NULL"` apabila memang membutuhkan SQL NULL.

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
SELECT id, JSON_UNQUOTE(JSON_EXTRACT(payload, '$.profile.address.city')) AS city, DATE_FORMAT(created_at, '%Y-%m-%d') AS created_date
FROM customer
WHERE JSON_UNQUOTE(JSON_EXTRACT(payload, '$.profile.address.city')) = ?
```

Penggunaan QueryBuilder:

```java
// MySQL: mengambil scalar dari nested JSON.
String city = QueryFunction.MySql.jsonUnquote(
        QueryFunction.MySql.jsonExtract("payload", "'$.profile.address.city'"));
QueryBuilder query = QueryBuilder.builder()
        .select("id", city + " AS city",
                QueryFunction.MySql.dateFormat("created_at", "'%Y-%m-%d'") + " AS created_date")
        .from("customer")
        .where()
            .equals(city, requestedCity)
        .done();

// Format dan argumen mengikuti sintaks masing-masing database.
String month = QueryFunction.PostgreSql.dateTrunc("'month'", "created_at");
String names = QueryFunction.Oracle.listAgg("name", "','", "id ASC");
String tomorrow = QueryFunction.SqlServer.dateAdd("day", "1", "created_at");
String sqliteDate = QueryFunction.Sqlite.strftime("'%Y-%m-%d'", "created_at");
```

`jsonExtractPathText` dan `jsonbExtractPathText` menerima satu key path. Untuk PostgreSQL
dengan beberapa key bertingkat, atau database/fungsi lain yang belum memiliki helper,
gunakan extension point `call`:

```java
String city = QueryFunction.call("JSONB_EXTRACT_PATH_TEXT",
        "payload", "'profile'", "'address'", "'city'");
String custom = QueryFunction.call("app.normalize_name", "name");
```

`call` menerima nama fungsi berupa identifier tanpa kutip, opsional dengan schema,
dan nol atau lebih argumen. Validasi nama tidak menjamin keamanan SQL argument.
Placeholder `?` dipertahankan tanpa mengisi parameter; untuk expression dengan placeholder,
bind parameter melalui API eksekusi raw SQL sesuai urutan. `QueryBuilder` tidak otomatis
menambahkan parameter dari expression pada `select`.

Hasil tetap mengikuti aturan database: null, timezone, trailing spaces pada `LEN`,
tipe JSON versus teks, urutan agregasi, dan batas panjang agregasi dapat berbeda.
`stringAgg` tanpa ORDER BY tidak menjamin urutan; gunakan trusted raw SQL untuk klausa
agregasi lanjutan. `groupConcat` MySQL dan `listAgg` Oracle menyediakan argumen ordering.
Nama helper tidak menjamin fungsi tersedia pada semua versi database. SQL Server
`STRING_AGG` membutuhkan versi 2017 atau lebih baru; fungsi JSON membutuhkan dukungan JSON
pada versi/build database yang dipakai. SQLite dapat dibangun tanpa dukungan JSON.

API lama tetap menghasilkan SQL yang sama: `length` memakai `LENGTH`,
`concat` memakai `CONCAT`, dan `nvl` memakai `NVL`. Pemilihan helper fungsi
tidak mengubah pagination `LIMIT/OFFSET` atau bagian lain dari `QueryBuilder`.
Utility ini tidak menyimpan state dan aman digunakan bersama antar thread.

Referensi sintaks: [PostgreSQL](https://www.postgresql.org/docs/18/functions.html),
[MySQL](https://dev.mysql.com/doc/refman/8.4/en/functions.html),
[Oracle](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Functions.html),
[SQL Server](https://learn.microsoft.com/en-us/sql/t-sql/functions/functions),
dan [SQLite](https://www.sqlite.org/lang_corefunc.html).


### Helper lanjutan setiap database

Helper tambahan di bawah memakai kontrak SQL expression yang sama. Nama parameter
mengikuti urutan argument database, bukan Java value conversion.

| Database | Helper tambahan |
|---|---|
| `PostgreSql` | `splitPart`, `substring`, `left`, `right`, `lpad`, `rpad`, `regexpReplace`, `datePart`, `age`, `makeDate`, `toDate`, `toTimestamp`, `arrayAgg`, `jsonbAgg`, `jsonbArrayLength`, `jsonbTypeof`, `jsonbSet`, `jsonbPathExists`, `jsonbObjectAgg`, `now`, `floor`, `ceiling`, `power` |
| `MySql` | `substring`, `substringIndex`, `locate`, `left`, `right`, `lpad`, `rpad`, `charLength`, `regexpReplace`, `timestampAdd`, `timestampDiff`, `dateDiff`, `lastDay`, `year`, `month`, `day`, `jsonSet`, `jsonRemove`, `jsonContains`, `jsonLength`, `jsonValid`, `jsonObjectAgg`, `now`, `floor`, `ceiling`, `power` |
| `Oracle` | `substr`, `instr`, `lpad`, `rpad`, `regexpReplace`, `regexpSubstr`, `nvl2`, `toNumber`, `toDate`, `toTimestamp`, `lastDay`, `nextDay`, `roundDate`, `jsonExists`, `jsonArrayAgg`, `sysGuid`, `floor`, `ceiling`, `power` |
| `SqlServer` | `substring`, `charIndex`, `patIndex`, `left`, `right`, `replicate`, `stuff`, `datePart`, `dateName`, `eomonth`, `dateFromParts`, `format`, `convert`, `tryConvert`, `isJson`, `jsonModify`, `getDate`, `sysDateTime`, `newId`, `floor`, `ceiling`, `power` |
| `Sqlite` | `substr`, `instr`, `length`, `hex`, `printf`, `date`, `time`, `dateTime`, `julianday`, `unixepoch`, `total`, `json`, `jsonArrayLength`, `jsonType`, `jsonValid`, `jsonSet`, `jsonRemove`, `jsonGroupObject`, `random`, `floor`, `ceiling`, `power` |

Contoh komposisi:

```java
String nestedUpdate = QueryFunction.PostgreSql.jsonbSet(
        "payload", "'{profile,address,city}'", "'\"Jakarta\"'::jsonb", "true");
String elapsedMonths = QueryFunction.MySql.timestampDiff(
        "MONTH", "started_at", "finished_at");
String nextMonthEnd = QueryFunction.Oracle.lastDay(
        QueryFunction.Oracle.addMonths("created_at", "1"));
String monthEnd = QueryFunction.SqlServer.eomonth("created_at", "1");
String tomorrow = QueryFunction.Sqlite.date("created_at", "'+1 day'");
```

- PostgreSQL `jsonbSet` membutuhkan path berupa `text[]` dan value berupa `jsonb`.
  `jsonbPathExists` memakai JSONPath, berbeda dari key path `jsonbSet`.
- MySQL `timestampDiff(unit, start, end)` menghitung end minus start dalam unit
  yang dipilih; `dateDiff(end, start)` hanya menghitung selisih tanggal dalam hari.
- Oracle `nextDay` memakai nama hari yang mengikuti bahasa sesi database.
  `toDate` dan `toTimestamp` dalam kelompok Oracle/PostgreSql menerima SQL expression;
  metode lama pada kelas utama menerima Java literal.
- SQL Server `datePart`, `dateName`, `dateAdd`, dan `dateDiff` membutuhkan keyword
  date-part tanpa kutip, misalnya `"day"`. `convert` dan `tryConvert` membutuhkan
  SQL data type tepercaya seperti `"decimal(18, 2)"`. `format` mengikuti aturan
  format dan culture SQL Server.
- SQLite `date`, `time`, `dateTime`, `julianday`, dan `unixepoch` menyediakan
  satu modifier. Untuk tanpa modifier atau beberapa modifier gunakan `call`.
  Helper `floor`, `ceiling`, dan `power` memerlukan SQLite yang dikompilasi
  dengan `SQLITE_ENABLE_MATH_FUNCTIONS`.
- Helper regex tidak menyamakan engine, flag, atau aturan replacement antar database.
  MySQL `regexpReplace` membutuhkan MySQL 8.0 atau versi yang menyediakan fungsi tersebut.
- Fungsi pembuat nilai seperti `now`, `random`, `sysGuid`, dan `newId` mengikuti
  lifecycle evaluasi database, bukan waktu atau UUID dari JVM.

Klausa khusus seperti window `OVER (...)`, `FILTER`, `RETURNING`, JSON error clauses,
dan opsi agregasi tambahan tetap dapat dikomposisikan sebagai trusted SQL.
Helper bukan katalog seluruh fungsi vendor; `call` menyediakan akses fungsi lain.

Detail SQLite: [fungsi tanggal](https://www.sqlite.org/lang_datefunc.html) dan
[fungsi matematika](https://www.sqlite.org/lang_mathfunc.html).


## J. Batch Execution

Satu SQL dengan beberapa parameter group:

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
UPDATE app_user SET status = ? WHERE id = ?
```

Penggunaan QueryBuilder:

```java
QueryBuilder query = QueryBuilder.builder()
        .update("app_user")
        .set("status", null)
        .where()
            .equals("id", null)
        .done();

List<Object[]> parameters = List.of(
        new Object[]{"ACTIVE", 1L},
        new Object[]{"INACTIVE", 2L}
);

int[] results = queryProcessor.batchExecute(query, parameters);
```

Raw batch SQL:

```java
int[] results = queryProcessor.batchExecute(
        "INSERT INTO app_user (id, username, status) VALUES (?, ?, ?)",
        List.of(
                new Object[]{1L, "alice", "ACTIVE"},
                new Object[]{2L, "bob", "ACTIVE"}
        )
);
```

## K. Mengatur Transaksi

`QueryProcessor` adalah abstraction untuk eksekusi query, bukan transaction boundary. Letakkan
`@Transactional` pada service operation atau repository operation yang menggabungkan beberapa
statement atomik.

```java
@Service
public class UserRegistrationService {

    private final UserRepository userRepository;

    public UserRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(User user) {
        return userRepository.save(user);
    }
}
```

## L. Menguji Repository Aplikasi

Repository unit test sebaiknya memverifikasi hasil dan interaksi dengan `QueryProcessor`.

```java
@ExtendWith(MockitoExtension.class)
class JdbcUserRepositoryTest {

    @Mock
    private QueryProcessor queryProcessor;

    @Test
    void should_ReturnUserPage_When_PageIsRequested() {
        // Given
        JdbcUserRepository repository = new JdbcUserRepository(queryProcessor);
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> expected = new PageImpl<>(List.of(), pageable, 0);
        when(queryProcessor.fetchPage(any(QueryBuilder.class), any(RowMapper.class), eq(pageable)))
                .thenReturn(expected);

        // When
        Page<User> result = repository.findAll(pageable);

        // Then
        assertSame(expected, result);
        verify(queryProcessor).fetchPage(any(QueryBuilder.class), any(RowMapper.class), eq(pageable));
        verifyNoMoreInteractions(queryProcessor);
    }
}
```

Untuk test SQL builder, validasi SQL final, urutan parameters, count parameters, serta negative case
seperti page invalid dan sort expression yang tidak aman.

## Subquery pada QueryBuilder

`WhereBuilder.conditionSubquery(column, operator, subquery)` mendukung `IN`,
`NOT_IN`, dan operator binary (termasuk perbandingan scalar). `exists(subquery)`
dan `notExists(subquery)` menerima SELECT dengan projection bebas.

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
SELECT u.id, u.name FROM users u WHERE u.active = ? AND u.id IN (SELECT o.user_id FROM orders o WHERE o.status = ?)
```

Penggunaan QueryBuilder:

```java
QueryBuilder paidOrders = QueryBuilder.builder()
        .select("o.user_id")
        .from("orders").as("o")
        .where().equals("o.status", "PAID").done();

QueryBuilder users = QueryBuilder.builder()
        .select("u.id", "u.name")
        .from("users").as("u")
        .where().equals("u.active", true).and()
        .conditionSubquery("u.id", QueryOperator.IN, paidOrders)
        .done();
// users.getParameters(): [true, "PAID"]
```

Untuk pemeriksaan keberadaan dengan korelasi:

SQL yang akan dibangun (nilai menggunakan placeholder JDBC):

```sql
SELECT u.id FROM users u WHERE EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = ?)
```

Penggunaan QueryBuilder:

```java
QueryBuilder paidOrderExists = QueryBuilder.builder()
        .select("1")
        .from("orders").as("o")
        .where().compareColumns("o.user_id", QueryOperator.EQUALS, "u.id").and()
        .equals("o.status", "PAID").done();

QueryBuilder users = QueryBuilder.builder()
        .select("u.id")
        .from("users").as("u")
        .where().exists(paidOrderExists).done();
```

Gunakan import `reyga.starter.foundation.common_database.util.QueryBuilder` dan
`reyga.starter.foundation.common_database.enumeration.QueryOperator`.
Buat builder baru per operasi pada repository; builder mutable tidak thread-safe.
Gunakan parameter binding untuk input pengguna dan alias/kolom dari konstanta
aplikasi. Jangan menyambung `subquery.build()` ke string SQL: parameter tidak
akan ikut tersalin melalui API string.

SQL dan parameter disalin ketika predicate dipasang; perubahan berikutnya pada
builder anak tidak mengubah induk. Snapshot parameter bukan deep copy nilai.
Subquery wajib SELECT dan bukan builder induk sendiri. Subquery bertingkat
serta predicate pada UPDATE/DELETE didukung. Tambahkan assignment UPDATE sebelum
WHERE seperti pada API lama. `IN`/`NOT IN` harus mengembalikan satu kolom;
perbandingan scalar harus mengembalikan satu kolom dan paling banyak satu baris.
Validasi bentuk hasil tersebut dilakukan database. Perhatikan semantik NULL
pada `NOT IN`; gunakan correlated `NOT EXISTS` saat sesuai kebutuhan domain.

`buildCount()` menghapus pagination induk tetapi tetap mempertahankan pagination
dan binding subquery karena memengaruhi hasil filter. Sintaks pagination tetap
`LIMIT/OFFSET` dan harus sesuai dialect database. Dukungan ini terbatas pada
predicate WHERE; derived table pada FROM/JOIN dan projection subquery belum
memiliki API binding khusus. Evaluasi execution plan dan index pada database
consumer untuk query yang sering digunakan.
