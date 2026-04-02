package reyga.starter.foundation.common_io.enumeration;

import lombok.Getter;

@Getter
public enum ReportType {
    PDF("pdf", "application/pdf"),
    HTML("html", "text/html"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV("csv", "text/csv"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    RTF("rtf", "application/rtf"),
    ODT("odt", "application/vnd.oasis.opendocument.text"),
    ODS("ods", "application/vnd.oasis.opendocument.spreadsheet"),
    ODP("odp", "application/vnd.oasis.opendocument.presentation"),
    XML("xml", "application/xml"),
    JSON("json", "application/json"),
    TEXT("txt", "text/plain"),
    JRPRINT("jrprint", "application/octet-stream"),
    PNG("png", "image/png"),
    JPEG("jpg", "image/jpeg"),
    GIF("gif", "image/gif"),
    SVG("svg", "image/svg+xml");

    private final String extension;
    private final String mimeType;

    ReportType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }
}
