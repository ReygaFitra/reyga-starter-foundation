package reyga.starter.foundation.common_io.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.json.export.JsonExporter;
import net.sf.jasperreports.json.export.SimpleJsonExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.poi.export.JRXlsExporter;
import reyga.starter.foundation.common_io.enumeration.ReportType;
import reyga.starter.foundation.common_io.reports.ReportBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public final class DefaultReportBuilder implements ReportBuilder.CompileStep, ReportBuilder.FillStep, ReportBuilder.ExportStep {
    private final byte[] jrxmlBytes;
    private final String jrxmlPath;
    private final JasperDesign jasperDesign;
    private final Map<String, Object> parameters = new HashMap<>();
    private JasperReport compiledReport;
    private JasperPrint jasperPrint;
    private JRDataSource dataSource;
    private Connection connection;

    public DefaultReportBuilder(byte[] jrxmlBytes) {
        this.jrxmlBytes = jrxmlBytes;
        this.jrxmlPath = null;
        this.jasperDesign = null;
    }

    public DefaultReportBuilder(String jrxmlPath) {
        this.jrxmlBytes = null;
        this.jrxmlPath = jrxmlPath;
        this.jasperDesign = null;
    }

    public DefaultReportBuilder(JasperDesign jasperDesign) {
        this.jrxmlBytes = null;
        this.jrxmlPath = null;
        this.jasperDesign = jasperDesign;
    }

    @Override
    public ReportBuilder.FillStep compile() throws JRException {
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
    public ReportBuilder.FillStep withParameters(Map<String, Object> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            this.parameters.putAll(parameters);
        }
        return this;
    }

    @Override
    public ReportBuilder.FillStep withLocale(Locale locale) {
        if (locale != null) {
            this.parameters.put(JRParameter.REPORT_LOCALE, locale);
        }
        return this;
    }

    @Override
    public ReportBuilder.FillStep withTimeZone(TimeZone timeZone) {
        if (timeZone != null) {
            this.parameters.put(JRParameter.REPORT_TIME_ZONE, timeZone);
        }
        return this;
    }

    @Override
    public ReportBuilder.FillStep withDataSource(JRDataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    @Override
    public ReportBuilder.FillStep withConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    @Override
    public ReportBuilder.ExportStep fill() throws JRException {
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
    public ReportBuilder.ExportStep exportTo(ReportType type, OutputStream outputStream) throws JRException {
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
