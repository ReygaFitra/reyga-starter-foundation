package reyga.starter.foundation.common_database.util;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryFunctionNameValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"A", "Z", "a", "z", "_", "_0", "A09_Zaz", "schema.function",
            "catalog.schema.function", "_schema._function9"})
    void should_AcceptName_When_IdentifiersFollowAsciiRules(String name) {
        assertEquals(name + "(value)", QueryFunction.call(name, "value"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", ".", ".name", "name.", "name..other", "0name",
            "schema.0name", "name other", "name-other", "name/other", "name;other",
            "name()", "\"name\"", "name$other", "naïve", "名前", "schema.é",
            "@name", "[name]", "name\n", "name\t", "name:other", "name\\other"})
    void should_RejectName_When_IdentifierOrSeparatorIsInvalid(String name) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> QueryFunction.call(name));
        assertEquals("functionName must contain unquoted SQL identifiers", exception.getMessage());
    }

    @Test
    void should_RenderCall_When_NameHasVeryLongIdentifier() {
        String name = "a" + "0_Az".repeat(25_000);
        assertEquals(name + "()", QueryFunction.call(name));
    }

    @Test
    void should_RenderCall_When_NameHasManyQualifiedIdentifiers() {
        String name = "schema.".repeat(20_000) + "function";
        assertEquals(name + "(?)", QueryFunction.call(name, "?"));
    }

    @Test
    void should_RejectName_When_LongNameHasInvalidTail() {
        String name = "schema.".repeat(20_000) + "function";
        assertThrows(IllegalArgumentException.class, () -> QueryFunction.call(name + "."));
        assertThrows(IllegalArgumentException.class, () -> QueryFunction.call(name + "$"));
        assertThrows(IllegalArgumentException.class, () -> QueryFunction.call(name + ".9invalid"));
    }

    @Test
    void should_PreserveRawSql_When_ArgumentsContainPlaceholdersAndLiterals() {
        String[] arguments = {"?", "'O''Brien'", "NULL", "COALESCE(a, b)", " value "};
        assertEquals("custom(?, 'O''Brien', NULL, COALESCE(a, b),  value )",
                QueryFunction.call("custom", arguments));
        assertEquals("?", arguments[0]);
    }

    @Test
    void should_ReportMandatoryArgument_When_NameOrArgumentArrayIsNull() {
        assertEquals("functionName", assertThrows(NullPointerException.class,
                () -> QueryFunction.call(null)).getMessage());
        assertEquals("arguments", assertThrows(NullPointerException.class,
                () -> QueryFunction.call("custom", (String[]) null)).getMessage());
    }

    @Test
    void should_BindConditionValue_When_FunctionIsUsedInQueryBuilder() {
        String city = QueryFunction.MySql.jsonUnquote(
                QueryFunction.MySql.jsonExtract("payload", "'$.profile.address.city'"));
        QueryBuilder query = QueryBuilder.builder()
                .select(city + " AS city")
                .from("customer")
                .where()
                    .equals(city, "Jakarta")
                .done();

        assertEquals("SELECT " + city + " AS city FROM customer WHERE " + city + " = ?", query.build());
        assertEquals(List.of("Jakarta"), query.getParameters());
    }
}
