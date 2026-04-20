package reyga.starter.foundation.common_io.reports;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.JasperDesign;
import reyga.starter.foundation.common_io.enumeration.ReportType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public final class ReportBuilder {

    private ReportBuilder() {
    }

    public static CompileStep from(InputStream jrxmlInputStream) throws IOException {
        Objects.requireNonNull(jrxmlInputStream, "jrxmlInputStream must not be null");
        return ReportBuilderProvider.getProvider().from(jrxmlInputStream.readAllBytes());
    }

    public static CompileStep from(String jrxmlPath) {
        Objects.requireNonNull(jrxmlPath, "jrxmlPath must not be null");
        if (jrxmlPath.isBlank()) {
            throw new IllegalArgumentException("jrxmlPath must not be blank");
        }
        return ReportBuilderProvider.getProvider().from(jrxmlPath);
    }

    public static CompileStep from(JasperDesign jasperDesign) {
        Objects.requireNonNull(jasperDesign, "jasperDesign must not be null");
        return ReportBuilderProvider.getProvider().from(jasperDesign);
    }

    public interface CompileStep {
        FillStep compile() throws JRException;
    }

    public interface FillStep {
        FillStep withParameters(Map<String, Object> parameters);

        FillStep withLocale(Locale locale);

        FillStep withTimeZone(TimeZone timeZone);

        FillStep withDataSource(JRDataSource dataSource);

        FillStep withConnection(Connection connection);

        ExportStep fill() throws JRException;
    }

    public interface ExportStep {
        JasperPrint getPrint();

        ExportStep exportTo(ReportType type, OutputStream outputStream) throws JRException;

        byte[] exportTo(ReportType type) throws JRException;
    }

}
