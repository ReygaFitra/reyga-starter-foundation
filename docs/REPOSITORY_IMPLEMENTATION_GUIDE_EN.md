# Repository Implementation Guide

This guide describes JDBC repository patterns using `common-database`. It keeps
domain contracts simple, centralizes JDBC execution in `QueryProcessor`, and
builds SQL through `QueryBuilder` and `QueryFunction`.

For constructor injection of `QueryProcessor`, use the `common-database` API at
compile scope and `foundation-starter:1.1.0` at runtime scope. Configure a datasource
so `JdbcTemplate` is available. No dedicated YAML flag is required. See the
[Configuration Guide](CONFIGURATION_GUIDE_EN.md#a-step-1-prepare-application-dependencies).

## A. Before You Start

Examples use `app_user` with `id`, `username`, `status`, and `created_at`. Create
this table through application database migrations. Examples assume application-supplied
IDs and non-null values in all four columns. Additional mapping examples require
their additional tables and columns. Add Spring JDBC and the appropriate driver;
complete dependencies are described in the configuration guide.

Follow sections A�E to build your first repository, then use query, pagination,
and batch sections as needed. Create a new `QueryBuilder` per operation;
do not store mutable builders in repository fields.

## B. Recommended Architecture

Recommended dependency flow:

```text
Service
  -> Domain Repository Interface
       extends BaseJdbcRepository<Entity, Identifier>
          -> Repository Implementation
               -> QueryProcessor
                    -> JdbcTemplate
```

Responsibilities of each component:

| Component | Responsibility |
| --- | --- |
| `BaseJdbcRepository<E, I>` | Minimum persistence contract: `save`, `findAll`, and `findById`. |
| Domain repository interface | Adds queries meaningful to the domain. |
| Repository implementation | Builds queries, selects a `RowMapper`, and maps results to domain models. |
| `JdbcResultSetBuilder` | Builds a `RowMapper` with callbacks accepting only `JdbcResultSetMapper`. |
| `JdbcResultSetMapper` | Reads typed and JSON columns with explicit nullable or mandatory rules. |
| `JdbcJsonColumnReader`, `JdbcJsonColumnReaders` | Retrieves JSON text through JDBC getters or a consumer-selected reader. |
| `JdbcJsonDecoder<T>` | Converts complete JSON, including nested objects and arrays, into domain types. |
| `QueryBuilder` | Builds parameterized SELECT, INSERT, UPDATE, and DELETE SQL. |
| `QueryFunction` | Builds SQL expressions such as `COUNT`, `UPPER`, `COALESCE`, and `TO_DATE`. |
| `QueryProcessor` | Executes builders or raw SQL through JDBC. |

Services should depend only on domain repository interfaces. Do not inject
`JdbcTemplate`, `QueryProcessor`, or `QueryBuilder` directly into services;
this exposes persistence details to the business layer.

## C. Creating the Domain Model

Domain model used throughout this guide:

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

## D. Defining the Repository Interface

Domain repositories should be interfaces extending `BaseJdbcRepository`.
Additional methods should use domain terminology and avoid exposing JDBC types.

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

Add `findAll(Pageable)` for paged results. Use `findAll()` to retrieve all data.

## E. Implementing the Repository

Receive `QueryProcessor` through constructor injection. Build `RowMapper` with
`JdbcResultSetBuilder` so every read uses `JdbcResultSetMapper`'s nullable or
mandatory contract. Mappers with stateless callbacks can be reused for consistent mapping.

SQL to build (values use JDBC placeholders):

```sql
INSERT INTO app_user (id, username, status, created_at) VALUES (?, ?, ?, ?);
SELECT id, username, status, created_at FROM app_user ORDER BY created_at DESC;
SELECT id, username, status, created_at FROM app_user WHERE id = ?;
SELECT id, username, status, created_at FROM app_user ORDER BY id ASC LIMIT ? OFFSET ?;
SELECT id, username, status, created_at FROM app_user WHERE UPPER(username) = ?;
UPDATE app_user SET status = ? WHERE id = ?;
DELETE FROM app_user WHERE id = ?;
```

QueryBuilder usage:

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
            throw new IllegalStateException("Saving a user must affect exactly one row");
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

Here, `save` performs INSERT, not upsert. Populate the new ID and `createdAt`.
Use a separate UPDATE method to change data.

`QueryProcessor` accepts builders directly. Call `query.build()` only to inspect
SQL before execution.

### Mapping nullable and mandatory columns

Import builder and mapper APIs from these packages:

```java
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common_database.util.JdbcResultSetBuilder;
import reyga.starter.foundation.common_database.util.JdbcResultSetMapper;
```

Import `JdbcResultSetMapper` only for explicitly typed callback method parameters.
Lambdas infer the `row` type. This API is in `common-database` and needs no Spring
bean or activation property. Default `QueryProcessor` execution still requires
a datasource and runtime starter, as described at the start of this guide.

`JdbcResultSetBuilder` builds Spring `RowMapper`, not JDBC `ResultSet`.
`map((row, rowNumber) -> ...)` receives a `JdbcResultSetMapper` reading only the
current row. Callbacks have no JDBC cursor and must not retain `row` afterward.
The builder does not execute queries, call `ResultSet.next()`, or close JDBC resources.

#### Builder flow

1. Select the result type with `JdbcResultSetBuilder.<T>builder()`.
2. Attach `.map((row, rowNumber) -> ...)` to create one object per result row.
3. Read columns with `row.getMandatory(label, type)` or `row.getNullable(label, type)`.
4. Call `.build()` to obtain `RowMapper<T>`.
5. Pass the mapper to a SELECT method on `QueryProcessor`.

The builder does not match domain properties to columns automatically. Callbacks
choose columns, conversions, constructors, and setters. `rowNumber` comes from
Spring JDBC, typically zero-based; it is not a database ID or global page row number.

#### Nullable and mandatory contracts

Each read uses one of these contracts:

| Read | SQL NULL | Non-null value |
|---|---|---|
| `row.getNullable(column, type)` | Returns Java `null` | Returns the requested type |
| `row.getMandatory(column, type)` | Throws `SQLDataException` with SQL state `22004` and column name | Returns the value, including `0`, `false`, or empty string |

These rules apply to query values, including outer joins, not table constraint
metadata. Missing columns or unsupported conversions fail with the original driver
`SQLException`. Mandatory prohibits NULL; it does not replace length, range, or domain validation.

#### Mapping records

This uses `user_statistics` with `user_id`, `login_count`, and `verified`.
`user_id` is mandatory; the other two allow SQL NULL. Define `UserStatistics.java`:

```java
public record UserStatistics(Long id, Integer loginCount, Boolean verified) {
}
```

Create the mapper once in the repository and reuse it:

```java
RowMapper<UserStatistics> mapper = JdbcResultSetBuilder.<UserStatistics>builder()
        .map((row, rowNumber) -> new UserStatistics(
                row.getMandatory("user_id", Long.class),
                row.getNullable("login_count", Integer.class),
                row.getNullable("verified", Boolean.class)
        ))
        .build();
```

Example results; `verified` assumes driver support for conversion to `Boolean`:

| `user_id` | `login_count` | `verified` | Result |
|---|---|---|---|
| `10` | SQL NULL | SQL NULL | `UserStatistics(10L, null, null)` |
| `10` | `0` | `false` | `UserStatistics(10L, 0, false)` |
| SQL NULL | `3` | `true` | Mapping fails with `SQLDataException` |

#### Integration with QueryProcessor

Use the record example's `mapper` for builders and raw SQL:

SQL to build (values use JDBC placeholders):

```sql
SELECT user_id, login_count, verified FROM user_statistics WHERE user_id = ?
```

QueryBuilder usage:

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

For pagination, pass the same mapper to `queryProcessor.fetchPage(query, mapper, pageable)`.
Create a new builder per execution, especially since pagination changes builder
state. These calls are alternatives, not a requirement to run every query for the same data.

Column nullability does not determine row existence. `optionalFetchOneBy` and
`fetchOneRaw` yield `Optional.empty()` for no rows. An existing row with a mandatory
SQL NULL still fails; the exception is not converted to an empty optional. Choose
single-row methods according to row-count guarantees. First-row `fetchOne` differs
from `fetchOneBy`'s JDBC single-row operation; builders do not change these semantics.

#### Mapping mutable objects

For no-argument constructors and setters, create a new object per row inside the
callback. This assumes `UserStatisticsBean` has `setId(Long)`, `setLoginCount(Integer)`,
and `setVerified(Boolean)`:

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

Do not share one bean across rows; each element must represent its own row object.
Nullable columns require wrapper properties to retain nulls.

#### Column aliases and outer joins

Use SELECT aliases as read labels, especially for duplicate joined column names.
This adds `user_profile(user_id, nickname)` and
`UserProfile(Long id, String username, String nickname)`:

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

Without a profile, `profile_nickname` is SQL NULL even if `user_profile.nickname`
is NOT NULL. Choose nullable reads from SELECT results. Read `profile_nickname`,
not `p.nickname`. Labels are not trimmed; match casing and alias rules to the driver.

#### Value types and conversions

Use column labels/SELECT aliases with `Integer.class`, `Long.class`, and `Boolean.class`
wrappers so SQL NULL does not become `0` or `false`. Primitives including `int.class`,
`Void.class`, null/empty names, and null types are rejected. Handle null explicitly before unboxing.

| Java type | Read token | Conversion behavior |
|---|---|---|
| String | `String.class` | JDBC typed conversion |
| Numeric wrappers | `Integer.class`, `Long.class`, `Short.class`, `Byte.class`, `Double.class`, `Float.class` | JDBC typed conversion; nullable preserves null |
| Boolean wrapper | `Boolean.class` | JDBC conversion; database representation depends on driver support |
| Decimal | `BigDecimal.class` | JDBC conversion suitable for precise decimals |
| Binary | `byte[].class` | JDBC typed conversion |
| JDBC temporal | `java.sql.Date.class`, `java.sql.Time.class`, `java.sql.Timestamp.class` | JDBC typed conversion |
| Local date | `LocalDate.class` | `getDate(column)`, then `toLocalDate()` |
| Local date/time | `LocalDateTime.class` | `getTimestamp(column)`, then `toLocalDateTime()` |
| Local time and other references | For example `LocalTime.class` or `UUID.class` | JDBC conversion if supported by the driver |

`LocalDate` uses JDBC `Date`, and `LocalDateTime` uses JDBC `Timestamp` to preserve
nanoseconds. No explicit time-zone conversion occurs; interpretation depends on
driver/database configuration. Other types, including `BigDecimal`, `LocalTime`,
and driver types, use `ResultSet.getObject(column, type)` with driver-specific support.

No custom conversions such as `"ACTIVE"` to enum or `"Y"` to boolean occur. Read a
driver-supported type first, then convert in the callback. For offset/zoned times,
choose supporting reference types and drivers; `LocalDateTime` does not retain offsets.

#### Domain defaults for nullable columns

For primitive domain fields, choose defaults explicitly after reading wrappers.
This assumes `UserMetrics(long id, int loginCount, boolean verified)` and a policy
that SQL NULL means zero logins and unverified:

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

These are domain defaults, not builder defaults. Use wrappers when null must
remain distinct from `0`/`false`.

#### Lifecycle and reuse

`build()` requires a non-null callback. Built mappers capture current callbacks;
later builder changes do not affect them. Builders are not thread-safe; mappers
can be shared if callbacks are thread-safe. `SQLException` and callback runtime
exceptions propagate without rewrapping. Callbacks may return null under Spring's
`RowMapper` contract, independently of column nullability.

Build stateless mappers once in `private static final RowMapper<T>` fields, as in
the main example. Store only built mappers; avoid shared builders or mutable
cross-request state in callbacks. Create mutable objects within callbacks even
when using mappers in parallel.

#### Failures and troubleshooting

| Condition | Result | Fix |
|---|---|---|
| `.build()` before `.map(...)` | `IllegalStateException` | Attach a callback first |
| `.map(null)` | `NullPointerException` | Supply a non-null callback |
| Null label or type | `NullPointerException` | Supply a valid label and class token |
| Blank label, primitive token, or `Void.class` | `IllegalArgumentException` | Use valid aliases and reference types |
| Mandatory SQL NULL | `SQLDataException`, SQL state `22004` | Check projection, data, and join nullability |
| Missing column or unsupported conversion | Driver `SQLException` | Check SELECT/aliases and driver support |
| Nullable unboxing without default | Callback `NullPointerException` | Retain wrappers or define defaults |
| Empty/invalid JSON or incompatible shape | `SQLDataException`, SQL state `22000` | Check data/decoder; parsing cause retained |
| Mandatory JSON literal `null` or null decoded result | `SQLDataException`, SQL state `22004` | Fix data or choose nullable |
| JSON exceeds reader limit | `SQLDataException`, SQL state `22001` | Adjust limit or reduce document size |
| Constructor, setter, or conversion failure | Callback runtime exception | Fix domain validation/conversion |

Direct `mapper.mapRow(resultSet, rowNumber)` calls, such as tests, propagate the
original `SQLException`. Through `JdbcTemplate` in default `QueryProcessor`, JDBC
exceptions are translated to `DataAccessException`. The Spring type depends on
the translator and driver; `SQLDataException` is not always outermost. Preserve
exceptions and causes in application translation. Do not log sensitive columns
or complete query results for diagnosis.

#### Mapping JSON columns and nested JSON

Use `getNullableJson(column, decoder)` or `getMandatoryJson(column, decoder)`.
Ordinary typed reads do not auto-detect JSON; JSON methods explicitly decode each column.

JDBC reading and decoding are separate contracts:

| Component | Contract |
|---|---|
| `JdbcJsonColumnReader` | Reads one column as JSON text; only SQL NULL returns null |
| `JdbcJsonDecoder<T>` | Converts complete JSON into `T`, including nested structures and generic collections |

Contracts are in `reyga.starter.foundation.common_database.util`. Public APIs expose
no vendor JSON types and select no parser library. Decoders can be lambdas/method
references to consumer-configured parsers. No new bean or activation property is
needed; `JdbcResultSetBuilder` creates no parser per row.

This uses `customer_document` with mandatory `id` and JSON `profile`, containing
nested objects, object arrays, and maps with array values:

```json
{
  "displayName": "Customer",
  "address": {"city": "Jakarta", "postalCode": "10110"},
  "contacts": [{"type": "email", "value": "example@test.invalid"}],
  "preferences": {"notifications": ["EMAIL", "SMS"]}
}
```

Prepare these models, each public record in its own Java file, and import them
in the repository:

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

The decoder adapter uses Jackson 3 for the application's Spring Boot line. Jackson
types are consumer-only; foundation signatures use `JdbcJsonDecoder<T>`. Use the
configured application parser and build readers before callbacks:

```java
import java.util.List;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common_database.util.JdbcJsonDecoder;
import reyga.starter.foundation.common_database.util.JdbcResultSetBuilder;

// Use the configured application JsonMapper; do this once when
// preparing the mapper, not for every result row.
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

`address` becomes `CustomerAddress`, each contact becomes `CustomerContact`, and
`preferences` remains `Map<String, List<String>>`. No automatic flattening occurs.
Missing fields, unknown properties, dates, enums, and numeric conversion follow
decoder configuration. Mandatory checks the document and non-null result, not
all nested properties; run domain validation for required properties.

For root arrays or other generics, capture the complete type in the decoder,
for example with Jackson 3 `TypeReference`:

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

`profilesMapper` produces one `List<CustomerProfile>` per JDBC row.
`queryProcessor.fetchRaw(sql, profilesMapper)` returns `List<List<CustomerProfile>>`;
arrays are not merged across rows. Use the full `TypeReference` for
`Map<String, List<CustomerProfile>>`; avoid `List.class` when element types matter.

#### Selecting JSON readers by storage type

Default is `JdbcJsonColumnReaders.text()`, using `ResultSet.getString(column)`.
Standard readers use JDBC without vendor name guessing or vendor `toString()`:

| Reader | JDBC getter | Usage |
|---|---|---|
| `text()` | `getString(column)` | Text/native JSON when the driver supports text conversion |
| `characterStream()` | `getCharacterStream(column)` | Text/CLOB through character streams; NCLOB depends on driver support |
| `utf8BinaryStream()` | `getBinaryStream(column)` | UTF-8 JSON bytes in binary/BLOB storage |
| Consumer implementation | Consumer-selected | Native or driver-specific reads absent from standard readers |

UTF-8 readers cannot directly read vendor binary JSON such as OSON or compressed
formats. Use database/driver JSON-to-text conversion or custom readers. Database
support depends on driver versions, storage, and getters; JDBC does not guarantee
identical behavior across all combinations.

Standard readers default to **1,048,576 Java UTF-16 code units**, not bytes.
Use positive `maxCharacters` overloads to adjust. Streams limit buffering and
close on success/failure. `text()` checks length after driver String creation;
use streaming for large LOBs. No automatic getter fallback hides driver errors.

Choose one default reader when all mapper JSON columns share storage:

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

For mixed storage, use `getNullableJson(column, reader, decoder)` or
`getMandatoryJson(column, reader, decoder)`. Prepare readers once before callbacks:

```java
import reyga.starter.foundation.common_database.util.JdbcJsonColumnReader;

JdbcJsonColumnReader blobReader = JdbcJsonColumnReaders.utf8BinaryStream();
RowMapper<CustomerProfile> blobMapper = JdbcResultSetBuilder.<CustomerProfile>builder()
        .map((row, rowNumber) -> row.getNullableJson("profile_blob", blobReader, profileDecoder))
        .build();
```

Custom readers must preserve SQL NULL, consume and close opened streams, and not
advance/close ResultSet. Reused readers/decoders must be thread-safe. `build()`
captures current readers/callbacks; later changes do not affect existing mappers.

#### Nulls and JSON decoding failures

| Column value | `getNullableJson` | `getMandatoryJson` |
|---|---|---|
| SQL NULL | Java null; decoder not called | SQL state `22004`; decoder not called |
| JSON literal `null` | Passed to decoder; may yield Java null or JSON null node | SQL state `22004`; decoder not called |
| Empty/blank string | SQL state `22000` | SQL state `22000` |
| Valid matching JSON | Decoder result | Non-null decoder result |
| `{}` or `[]` | Accepted if supported by decoder | Same, with non-null result |
| Invalid JSON, wrong shape, or decoder failure | `SQLDataException`, SQL state `22000`, original cause retained | Same |

Nullable does not ignore invalid JSON. To distinguish SQL NULL from JSON `null`,
use tree decoders such as `jsonMapper::readTree`: SQL NULL remains Java null while
JSON null can become a null node. Mandatory rejects null results from non-null documents.

JDBC reader exceptions propagate directly. Decoder `IOException` and runtime
exceptions become `SQLDataException`, translated by Spring through `JdbcTemplate`.
Wrapper messages contain only column/failure context, but parser causes may expose
payload fragments; avoid sensitive payload logging. Configure parser nesting/token
limits and polymorphism policies. Reject trailing input and incompatible shapes;
the Jackson example explicitly checks trailing tokens.

#### Migrating existing mappers

`QueryProcessor` still accepts `RowMapper`. New builder mappers must use helper
reads, but existing mappers and `MapperColumnProcessor` remain supported. Static
`JdbcResultSetMapper.Nullable` and `Mandatory` getters retain behavior; legacy
mandatory getters do not validate SQL NULL. For strict legacy reads use
`Nullable.getObject(resultSet, column, type)` or `Mandatory.getObject(resultSet, column, type)`.

Move object construction to callbacks, replace direct `resultSet.getXxx(...)`
reads with typed helpers, and explicitly set nullability for every label:

| Previous read | Builder replacement |
|---|---|
| `resultSet.getLong("id")` for required IDs | `row.getMandatory("id", Long.class)` |
| `Nullable.getInt(resultSet, "login_count")` | `row.getNullable("login_count", Integer.class)` with domain null handling |
| `Mandatory.getString(resultSet, "username")` | `row.getMandatory("username", String.class)` |
| `resultSet.getTimestamp("created_at").toLocalDateTime()` | Mandatory `LocalDateTime.class` or nullable according to results |

Strict reads can reveal SQL NULL previously accepted as null or JDBC defaults.
Nullable primitive getter migration preserves null, requiring handling before
unboxing. Migration is optional; legacy APIs have not been removed.

### Execution metadata logging

Default implementation logs one `DATABASE_QUERY` event at `DEBUG` after each
JDBC operation, containing only operational metadata, for example:

```text
| {DATABASE_QUERY=operation=SELECT | execution=FETCH_MANY | outcome=SUCCESS | durationMs=4 | rows=3} |
```

Batch metadata summarizes result counts, affected rows, unknown row counts, and
failures. Failures contain exception types without messages. `DefaultQueryProcessor`
never logs SQL, table/column names, bindings, exception messages, or mapped data.
Enable its `DEBUG` only for database diagnostics; events do not appear at production `INFO`:

```yaml
logging:
  level:
    reyga.starter.foundation.common_database.processor.DefaultQueryProcessor: DEBUG
```

## F. QueryBuilder

### Aliases with as

Aliases are **optional**. `.as(...)` does not change SQL or parameter order for
existing builders that do not call it. `select`, `from`, JOIN, INSERT, UPDATE,
DELETE, pagination, and count queries remain usable as before. Inline aliases such
as `.select("u.id AS user_id")`, `.from("app_user u")`, `.join("roles r")`, and
`.join("roles", "r")` remain supported; no migration is required. Legacy patterns
and `.as(...)` can coexist for different expressions. Do not add `.as(...)` to
expressions already containing aliases. New validation applies only to `.as(...)`.

Example without aliases, SQL to build:

```sql
SELECT id, username FROM app_user WHERE status = ?
```

QueryBuilder usage:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("id", "username")
        .from("app_user")
        .where().equals("status", "ACTIVE").done();
```

Example with existing inline aliases, SQL to build:

```sql
SELECT u.id AS user_id FROM app_user u WHERE u.status = ?
```

QueryBuilder usage:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("u.id AS user_id")
        .from("app_user u")
        .where().equals("u.status", "ACTIVE").done();
```

Use `.as("alias")` immediately after `select(...)` or `from(...)`, or on JOIN builders.
For multiple SELECT columns, only the last receives the alias; call `select` again
for later projections. Aliases must be simple identifiers without spaces,
qualification, or quoting. Empty/null aliases, SQL fragments, missing targets,
and repeated calls are rejected. Supply unaliased expressions; raw SQL is not
parsed. Column aliases use `AS`; table aliases omit it for Oracle compatibility.
Aliases are application constants, not user input. Bindings are unchanged.

SQL to build:

```sql
SELECT u.id AS user_id, u.username AS login_name
FROM app_user u
LEFT JOIN user_role r ON u.id = r.user_id
WHERE u.status = ?
```

QueryBuilder usage:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("u.id").as("user_id")
        .select("u.username").as("login_name")
        .from("app_user").as("u")
        .join(QueryJoinType.LEFT, "user_role").as("r")
            .on("u.id", QueryOperator.EQUALS, "r.user_id").done()
        .where().equals("u.status", "ACTIVE").done();
```

### Typed query operators

Import `reyga.starter.foundation.common_database.enumeration.QueryOperator`.
The enum limits supported SQL operators. `QueryJoinType` selects JOIN types,
while `QueryOperator` selects predicates.

| Operator | SQL | Values in `condition(...)` |
|---|---|---|
| `EQUALS`, `NOT_EQUALS` | `=`, `<>` | 1 |
| `GREATER_THAN`, `GREATER_THAN_OR_EQUALS` | `>`, `>=` | 1 |
| `LESS_THAN`, `LESS_THAN_OR_EQUALS` | `<`, `<=` | 1 |
| `LIKE`, `NOT_LIKE` | `LIKE`, `NOT LIKE` | 1 pattern |
| `IN`, `NOT_IN` | `IN`, `NOT IN` | At least 1 |
| `BETWEEN`, `NOT_BETWEEN` | `BETWEEN`, `NOT BETWEEN` | 2 |
| `IS_NULL`, `IS_NOT_NULL` | `IS NULL`, `IS NOT NULL` | 0 |
| `IS_TRUE`, `IS_NOT_TRUE` | `IS TRUE`, `IS NOT TRUE` | 0 |
| `IS_FALSE`, `IS_NOT_FALSE` | `IS FALSE`, `IS NOT FALSE` | 0 |
| `IS_UNKNOWN`, `IS_NOT_UNKNOWN` | `IS UNKNOWN`, `IS NOT UNKNOWN` | 0 |
| `IS_DISTINCT_FROM`, `IS_NOT_DISTINCT_FROM` | `IS DISTINCT FROM`, `IS NOT DISTINCT FROM` | 1 |

This covers modeled predicates, not every SQL dialect operator. Arithmetic, JSON,
regex, and `ANY`/`ALL` subquery operators are not modeled. `EXISTS` is available
through `WhereBuilder.exists`. Combine with `.and()`/`.or()` and negate with
`NOT_*`. Boolean and DISTINCT support depends on the target database.

SQL to build (values use JDBC placeholders):

```sql
SELECT u.id, r.name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.age >= ? AND u.status IN (?, ?) AND u.deleted_at IS NULL
```

QueryBuilder usage:

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

Execute through `queryProcessor.fetch(query, ROW_MAPPER)`. Add `.paginate(pageable)`
for paged SQL. `buildCount()` excludes LIMIT/OFFSET; `getCountParameters()` contains
filter parameters only. For `Page<T>`, use the QueryProcessor pagination contract below.

`condition(...)` values are bound JDBC parameters. Both operands of
`on(left, operator, right)` are trusted SQL/column expressions, not request values.
Typed ON accepts one right operand; IN, BETWEEN, and unary predicates are rejected.
Null operators, wrong value counts, and empty expressions fail before mutation.

`condition("name", QueryOperator.LIKE, "Rey%")` retains the pattern.
`.like("name", "Rey")` and `.notLike(...)` add `%` on both sides. Other helpers:
`greaterThanOrEquals`, `lessThanOrEquals`, `notBetween`, `notIn`. Pass SQL null as
`(Object) null`, not a null varargs array; use `IS_NULL`, not `EQUALS`, for null tests.


### SELECT with filters, joins, grouping, and ordering

SQL to build (values use JDBC placeholders):

```sql
SELECT u.id, u.username, COUNT(r.id) AS role_count
FROM app_user u
INNER JOIN user_role r ON u.id = r.user_id
WHERE u.status = ? AND u.username IS NOT NULL
GROUP BY u.id, u.username
ORDER BY u.username ASC
```

QueryBuilder usage:

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

`ACTIVE` is a JDBC parameter. Tables, columns, join conditions, functions, and
`orderBy` expressions are SQL fragments and must come from constants or trusted sources.

### INSERT

SQL to build (values use JDBC placeholders):

```sql
INSERT INTO app_user (id, username, status) VALUES (?, ?, ?)
```

QueryBuilder usage:

```java
QueryBuilder query = QueryBuilder.builder()
        .insertInto("app_user", "id", "username", "status")
        .values(user.id(), user.username(), user.status());

int affectedRows = queryProcessor.execute(query);
```

Check `affectedRows` as appropriate, such as exactly one row for changes by ID.

### UPDATE

SQL to build (values use JDBC placeholders):

```sql
UPDATE app_user SET username = ?, status = ? WHERE id = ?
```

QueryBuilder usage:

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

SQL to build (values use JDBC placeholders):

```sql
DELETE FROM app_user WHERE id = ?
```

QueryBuilder usage:

```java
QueryBuilder query = QueryBuilder.builder()
        .deleteFrom("app_user")
        .where()
            .equals("id", userId)
        .done();

int affectedRows = queryProcessor.execute(query);
```

## G. Pagination with Pageable and Page

Spring Data `Pageable` is zero-based. The first page is `0`.

`Page<User>` provides page content and total row count. For paged requests,
content and totals are calculated using these queries:

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

QueryBuilder usage:

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

Count queries wrap the main SELECT to preserve `DISTINCT`, join, and `GROUP BY`
semantics. `LIMIT`/`OFFSET` parameters are not passed to count queries.

### Pageable.unpaged()

```java
Pageable pageable = Pageable.unpaged(Sort.by("username"));
Page<User> page = queryProcessor.fetchPage(query, USER_ROW_MAPPER, pageable);
```

`Pageable.unpaged()` retrieves all results without limits. Use only for controlled data volumes.

### Pagination by page number

Without `Pageable`, page numbering starts at 1:

SQL to build (values use JDBC placeholders):

```sql
SELECT * FROM app_user LIMIT ? OFFSET ?
```

QueryBuilder usage:

```java
QueryBuilder query = QueryBuilder.builder()
        .select("*")
        .from("app_user")
        .paginate(1, 20); // one-based: the first page is 1
```

Use `paginate(Pageable)` or `QueryProcessor.fetchPage(...)` for new implementations.

### SQL dialect

Pagination generates `LIMIT ? OFFSET ?`. Ensure dialect support. For other syntax,
use native raw SQL as in the following raw pagination example, constructing `PageImpl` explicitly.

## H. Raw Queries

Use raw queries for database-specific SQL or queries not expressible through
`QueryBuilder`. Values must still use placeholders.

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

### Raw UPDATE or DELETE

```java
int affectedRows = queryProcessor.executeRaw(
        "UPDATE app_user SET status = ? WHERE id = ?",
        "INACTIVE",
        userId
);
```

### Raw pagination for specific dialects

This requires `pageable.isPaged() == true`. Use regular raw SELECT for unpaged
requests. The example uses `OFFSET ... ROWS FETCH NEXT ... ROWS ONLY`.

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

Do not insert request filters/sorts directly into SQL strings. Map API sort
properties to an allowlist of columns first.

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

`QueryFunction` builds SQL expressions for projections, conditions, grouping, and ordering.

SQL to build (values use JDBC placeholders):

```sql
SELECT id, UPPER(username) AS normalized_username, COALESCE(status, 'UNKNOWN') AS resolved_status, CURRENT_TIMESTAMP AS queried_at, TO_DATE('2026-01-01', 'YYYY-MM-DD') AS cutoff_date
FROM app_user
WHERE UPPER(username) = ?
```

QueryBuilder usage:

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

Condition values are bound parameters. Expressions such as `TO_TIMESTAMP(...)`
that must be evaluated by the database belong in trusted raw SQL. `QueryFunction`
strings supplied as values are not automatically executed as SQL.

### General and database functions

Additional general helpers: `sum`, `average`, `minimum`, `maximum`, `absolute`,
`round`, `trim`, `replace`, and `nullIf`.

Choose your database's group; the library neither detects dialects from connections
nor automatically translates expressions.

| Group | Helpers |
|---|---|
| `QueryFunction.PostgreSql` | `dateTrunc`, `toChar`, `stringAgg`, `jsonExtractPathText`, `jsonbExtractPathText`, `jsonAgg` |
| `QueryFunction.MySql` | `ifNull`, `dateFormat`, `strToDate`, `groupConcat`, `jsonExtract`, `jsonUnquote`, `jsonArrayAgg`, `concatWs` |
| `QueryFunction.Oracle` | `toChar`, `trunc`, `addMonths`, `monthsBetween`, `listAgg`, `jsonValue`, `jsonQuery` |
| `QueryFunction.SqlServer` | `isNull`, `len`, `dateAdd`, `dateDiff`, `stringAgg`, `jsonValue`, `jsonQuery` |
| `QueryFunction.Sqlite` | `strftime`, `ifNull`, `groupConcat`, `jsonExtract`, `jsonGroupArray` |

New helper arguments are **SQL expressions**, including numbers, formats,
delimiters, and JSON paths. Quote SQL literals beforehand; never pass user input.
Unlike legacy `coalesce(column, defaultValue)` and `toDate(value, format)`, new
helpers do not turn Java values into literals. Null throws `NullPointerException`;
empty arguments throw `IllegalArgumentException`. Use `"NULL"` for SQL NULL.

SQL to build (values use JDBC placeholders):

```sql
SELECT id, JSON_UNQUOTE(JSON_EXTRACT(payload, '$.profile.address.city')) AS city, DATE_FORMAT(created_at, '%Y-%m-%d') AS created_date
FROM customer
WHERE JSON_UNQUOTE(JSON_EXTRACT(payload, '$.profile.address.city')) = ?
```

QueryBuilder usage:

```java
// MySQL: extract a scalar from nested JSON.
String city = QueryFunction.MySql.jsonUnquote(
        QueryFunction.MySql.jsonExtract("payload", "'$.profile.address.city'"));
QueryBuilder query = QueryBuilder.builder()
        .select("id", city + " AS city",
                QueryFunction.MySql.dateFormat("created_at", "'%Y-%m-%d'") + " AS created_date")
        .from("customer")
        .where()
            .equals(city, requestedCity)
        .done();

// Formats and arguments follow each database's syntax.
String month = QueryFunction.PostgreSql.dateTrunc("'month'", "created_at");
String names = QueryFunction.Oracle.listAgg("name", "','", "id ASC");
String tomorrow = QueryFunction.SqlServer.dateAdd("day", "1", "created_at");
String sqliteDate = QueryFunction.Sqlite.strftime("'%Y-%m-%d'", "created_at");
```

`jsonExtractPathText` and `jsonbExtractPathText` accept one path key. For nested
PostgreSQL keys or functions without helpers, use the `call` extension point:

```java
String city = QueryFunction.call("JSONB_EXTRACT_PATH_TEXT",
        "payload", "'profile'", "'address'", "'city'");
String custom = QueryFunction.call("app.normalize_name", "name");
```

`call` accepts an unquoted, optionally schema-qualified identifier and zero or
more arguments. Name validation does not ensure argument safety. `?` remains
unbound; bind values in order through raw execution APIs. `QueryBuilder` does not
automatically add parameters from SELECT expressions.

Results follow database rules: nulls, time zones, `LEN` trailing spaces, JSON/text
types, aggregation order, and length limits may differ. `stringAgg` without ORDER
BY has no order guarantee; use trusted SQL for advanced aggregation clauses.
MySQL `groupConcat` and Oracle `listAgg` offer ordering arguments. Helpers are not
available on every version. SQL Server `STRING_AGG` needs 2017+; JSON functions
need support in the selected version/build. SQLite may lack JSON support.

Legacy SQL remains unchanged: `length` uses `LENGTH`, `concat` uses `CONCAT`, and
`nvl` uses `NVL`. Helper selection does not change pagination or other builder
behavior. This utility is stateless and thread-safe.

Syntax references: [PostgreSQL](https://www.postgresql.org/docs/18/functions.html),
[MySQL](https://dev.mysql.com/doc/refman/8.4/en/functions.html),
[Oracle](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Functions.html),
[SQL Server](https://learn.microsoft.com/en-us/sql/t-sql/functions/functions),
and [SQLite](https://www.sqlite.org/lang_corefunc.html).


### Advanced helpers for each database

These helpers use the same expression contract. Parameters follow database
argument order, not Java value conversion.

| Database | Additional helpers |
|---|---|
| `PostgreSql` | `splitPart`, `substring`, `left`, `right`, `lpad`, `rpad`, `regexpReplace`, `datePart`, `age`, `makeDate`, `toDate`, `toTimestamp`, `arrayAgg`, `jsonbAgg`, `jsonbArrayLength`, `jsonbTypeof`, `jsonbSet`, `jsonbPathExists`, `jsonbObjectAgg`, `now`, `floor`, `ceiling`, `power` |
| `MySql` | `substring`, `substringIndex`, `locate`, `left`, `right`, `lpad`, `rpad`, `charLength`, `regexpReplace`, `timestampAdd`, `timestampDiff`, `dateDiff`, `lastDay`, `year`, `month`, `day`, `jsonSet`, `jsonRemove`, `jsonContains`, `jsonLength`, `jsonValid`, `jsonObjectAgg`, `now`, `floor`, `ceiling`, `power` |
| `Oracle` | `substr`, `instr`, `lpad`, `rpad`, `regexpReplace`, `regexpSubstr`, `nvl2`, `toNumber`, `toDate`, `toTimestamp`, `lastDay`, `nextDay`, `roundDate`, `jsonExists`, `jsonArrayAgg`, `sysGuid`, `floor`, `ceiling`, `power` |
| `SqlServer` | `substring`, `charIndex`, `patIndex`, `left`, `right`, `replicate`, `stuff`, `datePart`, `dateName`, `eomonth`, `dateFromParts`, `format`, `convert`, `tryConvert`, `isJson`, `jsonModify`, `getDate`, `sysDateTime`, `newId`, `floor`, `ceiling`, `power` |
| `Sqlite` | `substr`, `instr`, `length`, `hex`, `printf`, `date`, `time`, `dateTime`, `julianday`, `unixepoch`, `total`, `json`, `jsonArrayLength`, `jsonType`, `jsonValid`, `jsonSet`, `jsonRemove`, `jsonGroupObject`, `random`, `floor`, `ceiling`, `power` |

Composition examples:

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

- PostgreSQL `jsonbSet` needs a `text[]` path and `jsonb` value. `jsonbPathExists`
  uses JSONPath rather than `jsonbSet`'s key path.
- MySQL `timestampDiff(unit, start, end)` calculates end minus start in the selected
  unit; `dateDiff(end, start)` returns the date difference in days only.
- Oracle `nextDay` uses the session language for day names. Oracle/PostgreSql
  `toDate`/`toTimestamp` accept SQL expressions; legacy main-class methods take Java literals.
- SQL Server date helpers require unquoted date-part keywords such as `"day"`.
  `convert`/`tryConvert` need trusted SQL types such as `"decimal(18, 2)"`.
  `format` follows SQL Server format/culture rules.
- SQLite date/time helpers provide one modifier; use `call` for zero or multiple
  modifiers. `floor`, `ceiling`, and `power` require `SQLITE_ENABLE_MATH_FUNCTIONS`.
- Regex helpers do not unify engines, flags, or replacement rules. MySQL
  `regexpReplace` needs MySQL 8.0 or another supporting version.
- `now`, `random`, `sysGuid`, and `newId` follow database evaluation lifecycles,
  not JVM time/UUID generation.

Window `OVER (...)`, `FILTER`, `RETURNING`, JSON error clauses, and other aggregation
options can be composed as trusted SQL. Helpers are not a complete vendor catalog;
use `call` for other functions.

SQLite details: [date functions](https://www.sqlite.org/lang_datefunc.html) and
[math functions](https://www.sqlite.org/lang_mathfunc.html).


## J. Batch Execution

One statement with multiple parameter groups:

SQL to build (values use JDBC placeholders):

```sql
UPDATE app_user SET status = ? WHERE id = ?
```

QueryBuilder usage:

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

## K. Managing Transactions

`QueryProcessor` abstracts execution, not transaction boundaries. Put
`@Transactional` on service/repository operations combining multiple atomic statements.

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

## L. Testing Application Repositories

Repository unit tests should verify results and interactions with `QueryProcessor`.

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

For builder tests, verify final SQL, parameter order, count parameters, and
negative cases such as invalid pages and unsafe sorts.

## Subqueries in QueryBuilder

`conditionSubquery(column, operator, subquery)` supports `IN`, `NOT_IN`, and binary
operators including scalar comparisons. `exists(subquery)` and `notExists(subquery)`
accept SELECT queries with arbitrary projections.

SQL to build (values use JDBC placeholders):

```sql
SELECT u.id, u.name FROM users u WHERE u.active = ? AND u.id IN (SELECT o.user_id FROM orders o WHERE o.status = ?)
```

QueryBuilder usage:

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

For correlated existence checks:

SQL to build (values use JDBC placeholders):

```sql
SELECT u.id FROM users u WHERE EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = ?)
```

QueryBuilder usage:

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

Import `reyga.starter.foundation.common_database.util.QueryBuilder` and
`reyga.starter.foundation.common_database.enumeration.QueryOperator`. Create a new
builder per repository operation; builders are not thread-safe. Bind user input;
use application constants for aliases/columns. Do not concatenate `subquery.build()`
into SQL strings: string APIs do not copy bindings.

SQL and bindings are copied on attachment; later child changes do not affect the
parent. Snapshots are not deep copies of values. Subqueries must be separate SELECT
builders. Nested queries and UPDATE/DELETE predicates are supported. Add UPDATE
assignments before WHERE. IN/NOT IN must return one column; scalar queries one
column and at most one row. The database validates shape. Consider NOT IN's NULL
semantics; use correlated NOT EXISTS when appropriate for the domain.

`buildCount()` removes parent pagination but keeps child pagination/bindings
because they affect filters. LIMIT/OFFSET must suit your dialect. Support is
limited to WHERE; FROM/JOIN derived tables and SELECT projection subqueries have
no dedicated binding APIs yet. Evaluate execution plans and indexes for frequent queries.