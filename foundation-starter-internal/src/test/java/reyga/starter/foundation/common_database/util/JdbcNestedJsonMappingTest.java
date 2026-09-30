package reyga.starter.foundation.common_database.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.RowMapper;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcNestedJsonMappingTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    private static final String PROFILE_JSON = """
            {
              "name": "customer",
              "address": {"city": "Jakarta", "coordinates": {"latitude": -6.2, "longitude": 106.8}},
              "contacts": [{"type": "email", "value": "example@test.invalid"}],
              "preferences": {"notifications": ["EMAIL", "SMS"]},
              "note": null
            }
            """;

    @Test
    void should_MapNestedRecordsAndCollections_When_JsonColumnContainsProfile() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id", Long.class)).thenReturn(10L);
        when(resultSet.getString("profile")).thenReturn(PROFILE_JSON);
        ObjectReader profileReader = JSON_MAPPER.readerFor(Profile.class);
        RowMapper<UserRow> mapper = JdbcResultSetBuilder.<UserRow>builder()
                .map((row, rowNumber) -> new UserRow(
                        row.getMandatory("id", Long.class),
                        row.getMandatoryJson("profile", profileReader::readValue)
                ))
                .build();

        UserRow result = mapper.mapRow(resultSet, 0);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals("Jakarta", result.profile().address().city());
        assertEquals(new BigDecimal("-6.2"), result.profile().address().coordinates().latitude());
        assertEquals(new BigDecimal("106.8"), result.profile().address().coordinates().longitude());
        assertEquals(List.of(new Contact("email", "example@test.invalid")), result.profile().contacts());
        assertEquals(List.of("EMAIL", "SMS"), result.profile().preferences().get("notifications"));
        assertNull(result.profile().note());
    }

    @Test
    void should_PreserveGenericElementTypes_When_JsonRootIsNestedCollection() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("profiles")).thenReturn("[" + PROFILE_JSON + "]");
        ObjectReader profilesReader = JSON_MAPPER.readerFor(new TypeReference<List<Profile>>() {});
        RowMapper<List<Profile>> mapper = JdbcResultSetBuilder.<List<Profile>>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("profiles", profilesReader::readValue))
                .build();

        List<Profile> result = mapper.mapRow(resultSet, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertInstanceOf(Profile.class, result.getFirst());
        assertInstanceOf(Address.class, result.getFirst().address());
        assertInstanceOf(Contact.class, result.getFirst().contacts().getFirst());
    }

    @Test
    void should_MapNestedGenericMap_When_JsonRootContainsTypedLists() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("groups")).thenReturn("{\"customers\":[" + PROFILE_JSON + "]}");
        ObjectReader groupsReader = JSON_MAPPER.readerFor(new TypeReference<Map<String, List<Profile>>>() {});
        RowMapper<Map<String, List<Profile>>> mapper = JdbcResultSetBuilder.<Map<String, List<Profile>>>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("groups", groupsReader::readValue))
                .build();

        Map<String, List<Profile>> result = mapper.mapRow(resultSet, 0);

        assertNotNull(result);
        Profile profile = result.get("customers").getFirst();
        assertEquals("Jakarta", profile.address().city());
        assertInstanceOf(Contact.class, profile.contacts().getFirst());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"address\":", "[]"})
    void should_PreserveParserFailure_When_JsonIsMalformedOrWrongShape(String json) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("profile")).thenReturn(json);
        ObjectReader profileReader = JSON_MAPPER.readerFor(Profile.class);
        RowMapper<Profile> mapper = JdbcResultSetBuilder.<Profile>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("profile", profileReader::readValue))
                .build();

        SQLDataException exception = assertThrows(SQLDataException.class, () -> mapper.mapRow(resultSet, 0));

        assertEquals("22000", exception.getSQLState());
        assertEquals("Cannot decode JSON column 'profile'", exception.getMessage());
        assertInstanceOf(JacksonException.class, exception.getCause());
    }

    @Test
    void should_RejectTrailingDocument_When_DecoderRequiresExactlyOneJsonValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("profile")).thenReturn(PROFILE_JSON + "{}");
        ObjectReader profileReader = JSON_MAPPER.readerFor(Profile.class);
        RowMapper<Profile> mapper = JdbcResultSetBuilder.<Profile>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("profile", profileReader::readValue))
                .build();

        SQLDataException exception = assertThrows(SQLDataException.class, () -> mapper.mapRow(resultSet, 0));

        assertEquals("22000", exception.getSQLState());
        assertInstanceOf(JacksonException.class, exception.getCause());
    }

    @Test
    void should_PreserveJsonNullTree_When_NullableDecoderDistinguishesSqlAndJsonNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("profile")).thenReturn("null");
        RowMapper<JsonNode> mapper = JdbcResultSetBuilder.<JsonNode>builder()
                .map((row, rowNumber) -> row.getNullableJson("profile", JSON_MAPPER::readTree))
                .build();

        JsonNode result = mapper.mapRow(resultSet, 0);

        assertNotNull(result);
        assertTrue(result.isNull());
    }

    @Test
    void should_ReturnJavaNull_When_NullableTypedDecoderReadsJsonNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("profile")).thenReturn("null");
        ObjectReader profileReader = JSON_MAPPER.readerFor(Profile.class);
        RowMapper<Profile> mapper = JdbcResultSetBuilder.<Profile>builder()
                .map((row, rowNumber) -> row.getNullableJson("profile", profileReader::readValue))
                .build();

        assertNull(mapper.mapRow(resultSet, 0));
    }

    private record UserRow(Long id, Profile profile) {
    }

    private record Profile(String name, Address address, List<Contact> contacts,
                           Map<String, List<String>> preferences, String note) {
    }

    private record Address(String city, Coordinates coordinates) {
    }

    private record Coordinates(BigDecimal latitude, BigDecimal longitude) {
    }

    private record Contact(String type, String value) {
    }
}
