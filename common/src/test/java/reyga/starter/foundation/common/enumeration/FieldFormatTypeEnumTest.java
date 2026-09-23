package reyga.starter.foundation.common.enumeration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class FieldFormatTypeEnumTest {
    @ParameterizedTest
    @MethodSource("scenarios")
    void should_ReturnExpectedValidity_When_FormatInputIsChecked(FieldFormatTypeEnum format, String value, boolean expected) {
        // given
        String original = value;
        // when
        boolean result = format.matches(value);
        // then
        assertEquals(expected, result);
        assertEquals(original, value);
        assertNotNull(format.getRegex());
        if (format != FieldFormatTypeEnum.DATE) assertEquals(expected, value.matches(format.getRegex()));
    }

    @ParameterizedTest
    @EnumSource(FieldFormatTypeEnum.class)
    void should_RejectNull_When_InputIsNull(FieldFormatTypeEnum format) {
        // given
        String value = null;
        // when
        boolean result = format.matches(value);
        // then
        assertFalse(result);
    }

    @ParameterizedTest
    @EnumSource(value = FieldFormatTypeEnum.class, names = "ALLOW_ALL", mode = EnumSource.Mode.EXCLUDE)
    void should_RejectEmptyOrTrailingNewline_When_FormatIsRestricted(FieldFormatTypeEnum format) {
        // given
        String empty = "";
        // when
        boolean result = format.matches(empty);
        boolean newline = format.matches("value\n");
        // then
        assertFalse(result);
        assertFalse(newline);
    }

    static Stream<Arguments> scenarios() {
        return Stream.of(
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-41d4-a716-446655440000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "00000000-0000-0000-0000-000000000000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "1-1-1-1-1", false),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-91d4-a716-446655440000", false),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-41d4-c716-446655440000", false),
                Arguments.of(FieldFormatTypeEnum.UUID, "{550e8400-e29b-41d4-a716-446655440000}", false),
                Arguments.of(FieldFormatTypeEnum.UUID, "urn:uuid:550e8400-e29b-41d4-a716-446655440000", false),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400e29b41d4a716446655440000", false),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-41d4-a716-44665544000z", false),
                Arguments.of(FieldFormatTypeEnum.UUID, " 550e8400-e29b-41d4-a716-446655440000", false),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "0.0.0.0", true),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "255.255.255.255", true),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "192.168.1.1", true),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "256.1.1.1", false),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "1.999.1.1", false),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "1.1.256.1", false),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "1.1.1.256", false),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "01.2.3.4", false),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "::1", false),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "1.2.3", false),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "-1.2.3.4", false),
                Arguments.of(FieldFormatTypeEnum.DATE, "2024-02-29", true),
                Arguments.of(FieldFormatTypeEnum.DATE, "2000-02-29", true),
                Arguments.of(FieldFormatTypeEnum.DATE, "0001-01-01", true),
                Arguments.of(FieldFormatTypeEnum.DATE, "9999-12-31", true),
                Arguments.of(FieldFormatTypeEnum.DATE, "1900-02-29", false),
                Arguments.of(FieldFormatTypeEnum.DATE, "2025-02-29", false),
                Arguments.of(FieldFormatTypeEnum.DATE, "2026-04-31", false),
                Arguments.of(FieldFormatTypeEnum.DATE, "2026-13-01", false),
                Arguments.of(FieldFormatTypeEnum.DATE, "2026-00-01", false),
                Arguments.of(FieldFormatTypeEnum.DATE, "2026-01-00", false),
                Arguments.of(FieldFormatTypeEnum.DATE, "2026-1-01", false),
                Arguments.of(FieldFormatTypeEnum.DATE, "0000-01-01", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a@b.com", true),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "first.last+tag@example.co.id", true),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "o'connor@example.com", true),
                Arguments.of(FieldFormatTypeEnum.EMAIL, ".first@example.com", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a..b@example.com", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a.@example.com", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a@-example.com", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a@example-.com", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a@example..com", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a@localhost", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a b@example.com", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@example.com", false),
                Arguments.of(FieldFormatTypeEnum.EMAIL, "a@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.com", false),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "+62 812-345", true),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "+62 (812) 345-6789", true),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "(021) 1234-5678", true),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "08123456789", true),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "++628123", false),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "(021 1234", false),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "021) 1234", false),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "0812--345", false),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "0812 ", false),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, " 0812", false),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "+62\t812", false),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Strong1!", true),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Strong1#", true),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Strong1_", true),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Strong1~", true),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Strong1 ", false),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "strong1!", false),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "STRONG1!", false),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Strong!!", false),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Strong12", false),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Ab1!", false),
                Arguments.of(FieldFormatTypeEnum.LETTER_ONLY, "Abc", true),
                Arguments.of(FieldFormatTypeEnum.LETTER_ONLY, "Ab1", false),
                Arguments.of(FieldFormatTypeEnum.LETTER_ONLY, "é", false),
                Arguments.of(FieldFormatTypeEnum.ALPHANUMERIC, "Ab12", true),
                Arguments.of(FieldFormatTypeEnum.ALPHANUMERIC, "Ab_12", false),
                Arguments.of(FieldFormatTypeEnum.NUMERIC_ONLY, "0123", true),
                Arguments.of(FieldFormatTypeEnum.NUMERIC_ONLY, "-1", false),
                Arguments.of(FieldFormatTypeEnum.NUMERIC_ONLY, "1.2", false),
                Arguments.of(FieldFormatTypeEnum.NUMERIC_ONLY, "١٢", false),
                Arguments.of(FieldFormatTypeEnum.ALLOW_ALL, "line1\nline2", true),
                Arguments.of(FieldFormatTypeEnum.ALLOW_ALL, "", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-11d4-a716-446655440000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-21d4-a716-446655440000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-31d4-a716-446655440000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-41d4-a716-446655440000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-51d4-a716-446655440000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-61d4-a716-446655440000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-71d4-a716-446655440000", true),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-81d4-a716-446655440000", true)
        );
    }
}
