package reyga.starter.foundation.common.enumeration;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Built-in syntactic validation policies. No DNS, phone ownership, or password breach
 * checks are performed. Use matches for complete validation, especially calendar dates.
 */
public enum FieldFormatTypeEnum {
    /** ASCII dot-atom mailbox with DNS labels; no quoted local parts or internationalized email. */
    EMAIL("(?=.{1,254}$)(?=[^@]{1,64}@)[A-Za-z0-9_%+!#$&'*+/=?^" + "\u0060" + "{|}~-]+(?:\\.[A-Za-z0-9_%+!#$&'*+/=?^" + "\u0060" + "{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}"),
    /** ASCII digit groups separated by a space or dash, optionally parenthesized; optional leading plus. */
    PHONE_NUMBER("(?!\\+\\()\\+?(?:[0-9]++|\\([0-9]++\\))(?:[ -](?:[0-9]++|\\([0-9]++\\)))*"),
    /** IPv4 only: four octets from 0 to 255, with no ambiguous leading zeros. */
    IP_ADDRESS("(?:(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])"),
    /** Four-digit ISO date, years 0001-9999. matches also checks calendar validity and leap years. */
    DATE("(?!0000)[0-9]{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])"),
    /** Non-empty ASCII letters only. */
    LETTER_ONLY("[A-Za-z]+"),
    /** Non-empty ASCII letters and digits only. */
    ALPHANUMERIC("[A-Za-z0-9]+"),
    /** Non-empty ASCII digits only; signs, decimal points and exponents are not allowed. */
    NUMERIC_ONLY("[0-9]+"),
    /** Canonical UUID versions 1-8 with RFC variant, plus Nil and Max UUIDs; case insensitive. */
    UUID("(?:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-8][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}|00000000-0000-0000-0000-000000000000|[fF]{8}-[fF]{4}-[fF]{4}-[fF]{4}-[fF]{12})"),
    /** At least eight printable ASCII characters with upper/lowercase, digit and punctuation; no spaces. */
    PASSWORD_STRONG("(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!-/:-@\\[-" + "\u0060" + "{-~])[!-~]{8,}"),
    /** Any non-null string, including empty and multiline text. */
    ALLOW_ALL("(?s:.*)");

    private final String regex;
    private final Pattern pattern;

    FieldFormatTypeEnum(String regex) {
        this.regex = "\\A(?:" + regex + ")\\z";
        this.pattern = Pattern.compile(this.regex);
    }

    /**
     * Returns the lexical pattern for full-string matching. DATE additionally requires
     * a calendar check; prefer matches instead of using this regex alone.
     * @return regex for Pattern.matches
     */
    public String getRegex() { return regex; }

    /**
     * Checks an entire value without trimming or normalizing it.
     * @param value input text; null is invalid for every format
     * @return whether syntax and any semantic checks pass
     */
    public boolean matches(String value) {
        if (value == null || !pattern.matcher(value).matches()) return false;
        if (this == DATE) {
            try {
                LocalDate.parse(value);
            } catch (DateTimeParseException error) {
                return false;
            }
        }
        return true;
    }
}
