package reyga.starter.foundation.common_io.report;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import net.sf.jasperreports.export.TextReportConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reyga.starter.foundation.common_io.enumeration.ReportType;
import reyga.starter.foundation.common_io.reports.ReportBuilder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultReportBuilderTest {

    @TempDir
    Path tempDirectory;

    @Test
    void should_CompileReport_When_BytePathOrDesignSourceIsValid() throws Exception {
        // Given
        JasperDesign design = validDesign();
        byte[] jrxml = jrxmlBytes(design);
        Path jrxmlPath = Files.write(tempDirectory.resolve("report.jrxml"), jrxml);
        DefaultReportBuilder byteBuilder = new DefaultReportBuilder(jrxml);
        DefaultReportBuilder pathBuilder = new DefaultReportBuilder(jrxmlPath.toString());
        DefaultReportBuilder designBuilder = new DefaultReportBuilder(design);

        // When
        ReportBuilder.FillStep byteResult = byteBuilder.compile();
        ReportBuilder.FillStep pathResult = pathBuilder.compile();
        ReportBuilder.FillStep designResult = designBuilder.compile();

        // Then
        assertSame(byteBuilder, byteResult);
        assertSame(pathBuilder, pathResult);
        assertSame(designBuilder, designResult);
        assertNull(byteBuilder.getPrint());
        assertNull(pathBuilder.getPrint());
        assertNull(designBuilder.getPrint());
    }

    @Test
    void should_ThrowExpectedException_When_CompilationSourceIsMissingOrInvalid() {
        // Given
        DefaultReportBuilder missing = new DefaultReportBuilder((byte[]) null);
        DefaultReportBuilder invalidBytes = new DefaultReportBuilder(new byte[]{1, 2, 3});
        DefaultReportBuilder invalidPath = new DefaultReportBuilder(tempDirectory.resolve("missing.jrxml").toString());

        // When
        JRException missingException = assertThrows(JRException.class, missing::compile);
        JRException bytesException = assertThrows(JRException.class, invalidBytes::compile);
        JRException pathException = assertThrows(JRException.class, invalidPath::compile);

        // Then
        assertEquals("No jrxml source provided for compilation", missingException.getMessage());
        assertNotNull(bytesException.getMessage());
        assertNotNull(pathException.getMessage());
        assertNull(missing.getPrint());
        assertNull(invalidBytes.getPrint());
        assertNull(invalidPath.getPrint());
    }

    @Test
    void should_ReturnSameBuilderAndApplyOptions_When_FillOptionsAreConfigured() throws Exception {
        // Given
        DefaultReportBuilder builder = compiledBuilder();
        Locale locale = Locale.forLanguageTag("id-ID");
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Jakarta");
        JRDataSource dataSource = mock(JRDataSource.class);
        when(dataSource.next()).thenReturn(false);

        // When
        ReportBuilder.FillStep parametersResult = builder.withParameters(Map.of("title", "Foundation"));
        ReportBuilder.FillStep emptyParametersResult = builder.withParameters(Map.of());
        ReportBuilder.FillStep nullParametersResult = builder.withParameters(null);
        ReportBuilder.FillStep localeResult = builder.withLocale(locale);
        ReportBuilder.FillStep nullLocaleResult = builder.withLocale(null);
        ReportBuilder.FillStep timeZoneResult = builder.withTimeZone(timeZone);
        ReportBuilder.FillStep nullTimeZoneResult = builder.withTimeZone(null);
        ReportBuilder.FillStep dataSourceResult = builder.withDataSource(dataSource);
        ReportBuilder.FillStep connectionResult = builder.withConnection(null);
        ReportBuilder.ExportStep fillResult = builder.fill();

        // Then
        assertSame(builder, parametersResult);
        assertSame(builder, emptyParametersResult);
        assertSame(builder, nullParametersResult);
        assertSame(builder, localeResult);
        assertSame(builder, nullLocaleResult);
        assertSame(builder, timeZoneResult);
        assertSame(builder, nullTimeZoneResult);
        assertSame(builder, dataSourceResult);
        assertSame(builder, connectionResult);
        assertSame(builder, fillResult);
        assertNotNull(builder.getPrint());
        assertEquals(locale.toLanguageTag().replace('-', '_'), builder.getPrint().getLocaleCode());
        assertEquals(timeZone.getID(), builder.getPrint().getTimeZoneId());
        verify(dataSource).next();
    }

    @Test
    void should_PreferConnectionAndSkipDataSource_When_BothFillSourcesAreConfigured() throws Exception {
        // Given
        DefaultReportBuilder builder = compiledBuilder();
        JRDataSource dataSource = mock(JRDataSource.class);
        Connection connection = mock(Connection.class);

        // When
        ReportBuilder.ExportStep result = builder.withDataSource(dataSource)
                .withConnection(connection)
                .fill();

        // Then
        assertSame(builder, result);
        assertNotNull(builder.getPrint());
        verify(dataSource, never()).next();
    }

    @Test
    void should_UseEmptyDataSource_When_NoFillSourceIsConfigured() throws Exception {
        // Given
        DefaultReportBuilder builder = compiledBuilder();

        // When
        ReportBuilder.ExportStep result = builder.fill();

        // Then
        assertSame(builder, result);
        assertNotNull(builder.getPrint());
        assertFalse(builder.getPrint().getPages().isEmpty());
    }

    @Test
    void should_ThrowJRException_When_ReportIsFilledBeforeCompilation() {
        // Given
        DefaultReportBuilder builder = new DefaultReportBuilder(validDesign());

        // When
        JRException exception = assertThrows(JRException.class, builder::fill);

        // Then
        assertEquals("Report is not compiled", exception.getMessage());
        assertNull(builder.getPrint());
    }

    @ParameterizedTest
    @MethodSource("supportedExportTypes")
    void should_WriteExportAndReturnSameBuilder_When_SupportedTypeIsRequested(ReportType type) throws Exception {
        // Given
        DefaultReportBuilder builder = filledBuilder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        ReportBuilder.ExportStep result = builder.exportTo(type, outputStream);

        // Then
        assertSame(builder, result);
        assertNotNull(builder.getPrint());
        if (type == ReportType.CSV) {
            assertEquals(0, outputStream.size());
        } else {
            assertFalse(outputStream.toByteArray().length == 0, () -> "No bytes exported for " + type);
        }
    }

    @Test
    void should_ReturnExportedBytes_When_ByteArrayExportIsRequested() throws Exception {
        // Given
        DefaultReportBuilder builder = filledBuilder();
        ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream();
        builder.exportTo(ReportType.JRPRINT, expectedOutput);

        // When
        byte[] result = builder.exportTo(ReportType.JRPRINT);

        // Then
        assertArrayEquals(expectedOutput.toByteArray(), result);
        assertNotNull(builder.getPrint());
        assertFalse(result.length == 0);
    }

    @ParameterizedTest
    @EnumSource(value = ReportType.class, names = {"ODP", "PNG", "JPEG", "GIF", "SVG"})
    void should_ThrowJRException_When_ExporterIsUnavailable(ReportType type) throws Exception {
        // Given
        DefaultReportBuilder builder = filledBuilder();
        OutputStream outputStream = new ByteArrayOutputStream();

        // When
        JRException exception = assertThrows(JRException.class,
                () -> builder.exportTo(type, outputStream));

        // Then
        assertEquals("Exporter not available. Add specific exporter module for: " + type,
                exception.getMessage());
        assertEquals(0, ((ByteArrayOutputStream) outputStream).size());
    }

    @Test
    void should_ThrowExpectedException_When_ExportArgumentsOrStateAreInvalid() throws Exception {
        // Given
        DefaultReportBuilder unfilled = compiledBuilder();
        DefaultReportBuilder filled = filledBuilder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        NullPointerException typeException = assertThrows(NullPointerException.class,
                () -> filled.exportTo(null, outputStream));
        NullPointerException streamException = assertThrows(NullPointerException.class,
                () -> filled.exportTo(ReportType.PDF, null));
        JRException stateException = assertThrows(JRException.class,
                () -> unfilled.exportTo(ReportType.PDF, outputStream));

        // Then
        assertEquals("type must not be null", typeException.getMessage());
        assertEquals("outputStream must not be null", streamException.getMessage());
        assertEquals("Report is not filled", stateException.getMessage());
        assertEquals(0, outputStream.size());
        assertNull(unfilled.getPrint());
        assertNotNull(filled.getPrint());
    }

    private static Stream<ReportType> supportedExportTypes() {
        return Stream.of(
                ReportType.HTML, ReportType.PDF, ReportType.XLS, ReportType.XLSX,
                ReportType.CSV, ReportType.DOCX, ReportType.PPTX, ReportType.RTF,
                ReportType.ODT, ReportType.ODS, ReportType.XML, ReportType.JSON,
                ReportType.TEXT, ReportType.JRPRINT
        );
    }

    private DefaultReportBuilder compiledBuilder() throws JRException {
        DefaultReportBuilder builder = new DefaultReportBuilder(validDesign());
        builder.compile();
        return builder;
    }

    private DefaultReportBuilder filledBuilder() throws JRException {
        DefaultReportBuilder builder = compiledBuilder();
        builder.fill();
        return builder;
    }

    private JasperDesign validDesign() {
        JasperDesign design = new JasperDesign();
        design.setName("common_io_test_report");
        design.setPageWidth(595);
        design.setPageHeight(842);
        design.setColumnWidth(555);
        design.setLeftMargin(20);
        design.setRightMargin(20);
        design.setTopMargin(20);
        design.setBottomMargin(20);
        design.setProperty(JRParameter.REPORT_LOCALE, Locale.ENGLISH.toLanguageTag());
        design.setProperty(TextReportConfiguration.PROPERTY_PAGE_WIDTH, "80");
        design.setProperty(TextReportConfiguration.PROPERTY_PAGE_HEIGHT, "60");
        return design;
    }

    private byte[] jrxmlBytes(JasperDesign design) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRXmlWriter.writeReport(design, outputStream, "UTF-8");
        return outputStream.toByteArray();
    }
}
