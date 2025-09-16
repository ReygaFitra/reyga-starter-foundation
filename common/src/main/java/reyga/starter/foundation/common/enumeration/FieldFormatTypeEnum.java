package reyga.starter.foundation.common.enumeration;

import lombok.Getter;

@Getter
public enum FieldFormatTypeEnum {

    /**
     * Email format: basic validation for user@domain.tld
     */
    EMAIL("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"),

    /**
     * Phone number: allows digits, optional +, spaces, dashes, or parentheses
     */
    PHONE_NUMBER("\\+?([ -]?\\d+)+|\\(\\d+\\)([ -]\\d+)"),

    /**
     * IPv4 address: validates format like 192.168.1.1
     */
    IP_ADDRESS("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$"),

    /**
     * ISO Date format: yyyy-MM-dd (e.g., 2025-06-11)
     */
    DATE("^\\d{4}-\\d{2}-\\d{2}$"),

    /**
     * Only alphabetic letters (A-Z, a-z)
     */
    LETTER_ONLY("^[A-Za-z]+$"),

    /**
     * Only alphanumeric characters (A-Z, a-z, 0-9)
     */
    ALPHANUMERIC("^[A-Za-z0-9]+$"),

    /**
     * Only digits (0-9)
     */
    NUMERIC_ONLY("^\\d+$"),

    /**
     * UUID format: 8-4-4-4-12 hexadecimal characters
     */
    UUID("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"),

    /**
     * Strong password: at least 8 characters, includes uppercase, lowercase, number, and special character
     */
    PASSWORD_STRONG("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"),

    /**
     * Allow anything (no restriction)
     */
    ALLOW_ALL(".*");

    private final String regex;

    private FieldFormatTypeEnum(String r) {
        this.regex = r;
    }

}
