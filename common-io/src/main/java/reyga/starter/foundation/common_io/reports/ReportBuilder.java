package reyga.starter.foundation.common_io.reports;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.json.export.JsonExporter;
import net.sf.jasperreports.json.export.SimpleJsonExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.poi.export.JRXlsExporter;
import reyga.starter.foundation.common_io.enumeration.ReportType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public final class ReportBuilder {

    private ReportBuilder() {
    }

    public static CompileStep from(InputStream jrxmlInputStream) throws IOException {
        Objects.requireNonNull(jrxmlInputStream, "jrxmlInputStream must not be null");
        return new Builder(jrxmlInputStream.readAllBytes());
    }

    public static CompileStep from(String jrxmlPath) {
        Objects.requireNonNull(jrxmlPath, "jrxmlPath must not be null");
        if (jrxmlPath.isBlank()) {
            throw new IllegalArgumentException("jrxmlPath must not be blank");
        }
        return new Builder(jrxmlPath);
    }

    public static CompileStep from(JasperDesign jasperDesign) {
        Objects.requireNonNull(jasperDesign, "jasperDesign must not be null");
        return new Builder(jasperDesign);
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

    private static final class Builder implements CompileStep, FillStep, ExportStep {
        private final byte[] jrxmlBytes;
        private final String jrxmlPath;
        private final JasperDesign jasperDesign;
        private final Map<String, Object> parameters = new HashMap<>();
        private JasperReport compiledReport;
        private JasperPrint jasperPrint;
        private JRDataSource dataSource;
        private Connection connection;

        private Builder(byte[] jrxmlBytes) {
            this.jrxmlBytes = jrxmlBytes;
            this.jrxmlPath = null;
            this.jasperDesign = null;
        }

        private Builder(String jrxmlPath) {
            this.jrxmlBytes = null;
            this.jrxmlPath = jrxmlPath;
            this.jasperDesign = null;
        }

        private Builder(JasperDesign jasperDesign) {
            this.jrxmlBytes = null;
            this.jrxmlPath = null;
            this.jasperDesign = jasperDesign;
        }

        @Override
        public FillStep compile() throws JRException {
            if (jrxmlPath != null) {
                this.compiledReport = JasperCompileManager.compileReport(jrxmlPath);
                return this;
            }
            if (jasperDesign != null) {
                this.compiledReport = JasperCompileManager.compileReport(jasperDesign);
                return this;
            }
            if (jrxmlBytes == null) {
                throw new JRException("No jrxml source provided for compilation");
            }
            try (InputStream inputStream = new ByteArrayInputStream(jrxmlBytes)) {
                this.compiledReport = JasperCompileManager.compileReport(inputStream);
            } catch (IOException e) {
                throw new JRRuntimeException("Unexpected Error while compiling data", e);
            }
            return this;
        }

        @Override
        public FillStep withParameters(Map<String, Object> parameters) {
            if (parameters != null && !parameters.isEmpty()) {
                this.parameters.putAll(parameters);
            }
            return this;
        }

        @Override
        public FillStep withLocale(Locale locale) {
            if (locale != null) {
                this.parameters.put(JRParameter.REPORT_LOCALE, locale);
            }
            return this;
        }

        @Override
        public FillStep withTimeZone(TimeZone timeZone) {
            if (timeZone != null) {
                this.parameters.put(JRParameter.REPORT_TIME_ZONE, timeZone);
            }
            return this;
        }

        @Override
        public FillStep withDataSource(JRDataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        @Override
        public FillStep withConnection(Connection connection) {
            this.connection = connection;
            return this;
        }

        @Override
        public ExportStep fill() throws JRException {
            if (compiledReport == null) {
                throw new JRException("Report is not compiled");
            }
            if (connection != null) {
                this.jasperPrint = JasperFillManager.fillReport(compiledReport, parameters, connection);
            } else {
                JRDataSource resolvedDataSource = dataSource != null ? dataSource : new JREmptyDataSource();
                this.jasperPrint = JasperFillManager.fillReport(compiledReport, parameters, resolvedDataSource);
            }
            return this;
        }

        @Override
        public JasperPrint getPrint() {
            return jasperPrint;
        }

        @Override
        public ExportStep exportTo(ReportType type, OutputStream outputStream) throws JRException {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(outputStream, "outputStream must not be null");
            if (jasperPrint == null) {
                throw new JRException("Report is not filled");
            }
            switch (type) {
                case HTML -> exportHtml(outputStream);
                case PDF -> exportPdf(outputStream);
                case XLS -> exportXls(outputStream);
                case XLSX -> exportXlsx(outputStream);
                case CSV -> exportCsv(outputStream);
                case DOCX -> exportDocx(outputStream);
                case PPTX -> exportPptx(outputStream);
                case RTF -> exportRtf(outputStream);
                case ODT -> exportOdt(outputStream);
                case ODS -> exportOds(outputStream);
                case XML -> exportXml(outputStream);
                case JSON -> exportJson(outputStream);
                case TEXT -> exportText(outputStream);
                case JRPRINT -> exportJrPrint(outputStream);
                case ODP, PNG, JPEG, GIF, SVG -> throw new JRException(
                        "Exporter not available. Add specific exporter module for: " + type
                );
            }
            return this;
        }

        @Override
        public byte[] exportTo(ReportType type) throws JRException {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                exportTo(type, outputStream);
                return outputStream.toByteArray();
            } catch (IOException ex) {
                throw new JRException("Failed to export report", ex);
            }
        }

        private void exportHtml(OutputStream outputStream) throws JRException {
            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream, StandardCharsets.UTF_8.name()));
            exporter.exportReport();
        }

        private void exportPdf(OutputStream outputStream) throws JRException {
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
        }

        private void exportXls(OutputStream outputStream) throws JRException {
            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setDetectCellType(true);
            configuration.setCollapseRowSpan(false);
            exporter.setConfiguration(configuration);
            exporter.exportReport();
        }

        private void exportXlsx(OutputStream outputStream) throws JRException {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
            configuration.setDetectCellType(true);
            configuration.setCollapseRowSpan(false);
            exporter.setConfiguration(configuration);
            exporter.exportReport();
        }

        private void exportCsv(OutputStream outputStream) throws JRException {
            JRCsvExporter exporter = new JRCsvExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream, StandardCharsets.UTF_8.name()));
            exporter.exportReport();
        }

        private void exportDocx(OutputStream outputStream) throws JRException {
            JRDocxExporter exporter = new JRDocxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
        }

        private void exportPptx(OutputStream outputStream) throws JRException {
            JRPptxExporter exporter = new JRPptxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
        }

        private void exportRtf(OutputStream outputStream) throws JRException {
            JRRtfExporter exporter = new JRRtfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream, StandardCharsets.UTF_8.name()));
            exporter.exportReport();
        }

        private void exportOdt(OutputStream outputStream) throws JRException {
            JROdtExporter exporter = new JROdtExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
        }

        private void exportOds(OutputStream outputStream) throws JRException {
            JROdsExporter exporter = new JROdsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
        }

        private void exportXml(OutputStream outputStream) throws JRException {
            JRXmlExporter exporter = new JRXmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleXmlExporterOutput(outputStream, StandardCharsets.UTF_8.name()));
            exporter.exportReport();
        }

        private void exportJson(OutputStream outputStream) throws JRException {
            JsonExporter exporter = new JsonExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleJsonExporterOutput(outputStream, StandardCharsets.UTF_8.name()));
            exporter.exportReport();
        }

        private void exportText(OutputStream outputStream) throws JRException {
            JRTextExporter exporter = new JRTextExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream, StandardCharsets.UTF_8.name()));
            exporter.exportReport();
        }

        private void exportJrPrint(OutputStream outputStream) throws JRException {
            JRSaver.saveObject(jasperPrint, outputStream);
        }
    }
}
