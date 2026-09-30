package reyga.starter.foundation.common_database.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryFunctionDialectTest {

    @Test
    void should_RenderVendorFunctions_When_PostgreSqlIsSelected() {
        assertEquals("DATE_TRUNC(precision, expression)", QueryFunction.PostgreSql.dateTrunc("precision", "expression"));
        assertEquals("TO_CHAR(expression, format)", QueryFunction.PostgreSql.toChar("expression", "format"));
        assertEquals("STRING_AGG(expression, delimiter)", QueryFunction.PostgreSql.stringAgg("expression", "delimiter"));
        assertEquals("JSON_EXTRACT_PATH_TEXT(expression, path)", QueryFunction.PostgreSql.jsonExtractPathText("expression", "path"));
        assertEquals("JSONB_EXTRACT_PATH_TEXT(expression, path)", QueryFunction.PostgreSql.jsonbExtractPathText("expression", "path"));
        assertEquals("JSON_AGG(expression)", QueryFunction.PostgreSql.jsonAgg("expression"));
    }

    @Test
    void should_RenderVendorFunctions_When_MySqlIsSelected() {
        assertEquals("GROUP_CONCAT(name ORDER BY id DESC SEPARATOR ',')",
                QueryFunction.MySql.groupConcat("name", "','", "id DESC"));
        assertEquals("IFNULL(expression, fallback)", QueryFunction.MySql.ifNull("expression", "fallback"));
        assertEquals("DATE_FORMAT(expression, format)", QueryFunction.MySql.dateFormat("expression", "format"));
        assertEquals("STR_TO_DATE(expression, format)", QueryFunction.MySql.strToDate("expression", "format"));
        assertEquals("JSON_EXTRACT(expression, path)", QueryFunction.MySql.jsonExtract("expression", "path"));
        assertEquals("JSON_UNQUOTE(expression)", QueryFunction.MySql.jsonUnquote("expression"));
        assertEquals("JSON_ARRAYAGG(expression)", QueryFunction.MySql.jsonArrayAgg("expression"));
        assertEquals("CONCAT_WS(separator, first, second)", QueryFunction.MySql.concatWs("separator", "first", "second"));
    }

    @Test
    void should_RenderVendorFunctions_When_OracleIsSelected() {
        assertEquals("LISTAGG(name, ',') WITHIN GROUP (ORDER BY id)",
                QueryFunction.Oracle.listAgg("name", "','", "id"));
        assertEquals("TO_CHAR(expression, format)", QueryFunction.Oracle.toChar("expression", "format"));
        assertEquals("TRUNC(expression, format)", QueryFunction.Oracle.trunc("expression", "format"));
        assertEquals("ADD_MONTHS(expression, months)", QueryFunction.Oracle.addMonths("expression", "months"));
        assertEquals("MONTHS_BETWEEN(first, second)", QueryFunction.Oracle.monthsBetween("first", "second"));
        assertEquals("JSON_VALUE(expression, path)", QueryFunction.Oracle.jsonValue("expression", "path"));
        assertEquals("JSON_QUERY(expression, path)", QueryFunction.Oracle.jsonQuery("expression", "path"));
    }

    @Test
    void should_RenderVendorFunctions_When_SqlServerIsSelected() {
        assertEquals("ISNULL(expression, fallback)", QueryFunction.SqlServer.isNull("expression", "fallback"));
        assertEquals("LEN(expression)", QueryFunction.SqlServer.len("expression"));
        assertEquals("DATEADD(datePart, amount, expression)", QueryFunction.SqlServer.dateAdd("datePart", "amount", "expression"));
        assertEquals("DATEDIFF(datePart, start, end)", QueryFunction.SqlServer.dateDiff("datePart", "start", "end"));
        assertEquals("STRING_AGG(expression, delimiter)", QueryFunction.SqlServer.stringAgg("expression", "delimiter"));
        assertEquals("JSON_VALUE(expression, path)", QueryFunction.SqlServer.jsonValue("expression", "path"));
        assertEquals("JSON_QUERY(expression, path)", QueryFunction.SqlServer.jsonQuery("expression", "path"));
    }

    @Test
    void should_RenderVendorFunctions_When_SqliteIsSelected() {
        assertEquals("STRFTIME(format, expression)", QueryFunction.Sqlite.strftime("format", "expression"));
        assertEquals("IFNULL(expression, fallback)", QueryFunction.Sqlite.ifNull("expression", "fallback"));
        assertEquals("GROUP_CONCAT(expression, delimiter)", QueryFunction.Sqlite.groupConcat("expression", "delimiter"));
        assertEquals("JSON_EXTRACT(expression, path)", QueryFunction.Sqlite.jsonExtract("expression", "path"));
        assertEquals("JSON_GROUP_ARRAY(expression)", QueryFunction.Sqlite.jsonGroupArray("expression"));
    }

    @Test
    void should_RenderCommonFunctions_When_ExpressionsAreProvided() {
        assertEquals("SUM(amount)", QueryFunction.sum("amount"));
        assertEquals("AVG(amount)", QueryFunction.average("amount"));
        assertEquals("MIN(amount)", QueryFunction.minimum("amount"));
        assertEquals("MAX(amount)", QueryFunction.maximum("amount"));
        assertEquals("ABS(amount)", QueryFunction.absolute("amount"));
        assertEquals("ROUND(amount, 2)", QueryFunction.round("amount", "2"));
        assertEquals("TRIM(name)", QueryFunction.trim("name"));
        assertEquals("REPLACE(name, ?, ?)", QueryFunction.replace("name", "?", "?"));
        assertEquals("NULLIF(amount, 0)", QueryFunction.nullIf("amount", "0"));
    }

    @Test
    void should_PreserveExpressions_When_NestedJsonAndCustomFunctionsAreUsed() {
        assertEquals("JSON_UNQUOTE(JSON_EXTRACT(payload, '$.profile.address.city'))",
                QueryFunction.MySql.jsonUnquote(
                        QueryFunction.MySql.jsonExtract("payload", "'$.profile.address.city'")));
        assertEquals("app.normalize(name)", QueryFunction.call("app.normalize", "name"));
        assertEquals("NOW()", QueryFunction.call("NOW"));
        assertEquals("JSONB_EXTRACT_PATH_TEXT(payload, 'profile', 'address', 'city')",
                QueryFunction.call("JSONB_EXTRACT_PATH_TEXT", "payload", "'profile'", "'address'", "'city'"));
    }

    @Test
    void should_RejectInvalidArguments_When_NewHelpersAreCalled() {
        assertThrows(NullPointerException.class, () -> QueryFunction.call(null));
        assertThrows(NullPointerException.class, () -> QueryFunction.call("SUM", (String[]) null));
        assertThrows(NullPointerException.class, () -> QueryFunction.sum(null));
        assertThrows(IllegalArgumentException.class, () -> QueryFunction.sum(" "));
        assertThrows(IllegalArgumentException.class, () -> QueryFunction.call("SUM); DROP TABLE users; --", "amount"));
        assertThrows(IllegalArgumentException.class, () -> QueryFunction.call(""));
        assertThrows(IllegalArgumentException.class, () -> QueryFunction.Oracle.listAgg("name", "','", " "));
        assertThrows(NullPointerException.class, () -> QueryFunction.MySql.groupConcat("name", null, "id"));
    }
}
