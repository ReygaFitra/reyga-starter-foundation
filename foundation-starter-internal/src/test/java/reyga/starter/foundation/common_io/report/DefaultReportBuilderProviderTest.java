package reyga.starter.foundation.common_io.report;

import net.sf.jasperreports.engine.design.JasperDesign;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common_io.reports.ReportBuilder;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DefaultReportBuilderProviderTest {

    @Test
    void should_ReturnDefaultBuilder_When_ByteSourceIsProvided() {
        // Given
        DefaultReportBuilderProvider provider = new DefaultReportBuilderProvider();
        byte[] source = new byte[]{1, 2, 3};

        // When
        ReportBuilder.CompileStep result = provider.from(source);

        // Then
        assertInstanceOf(DefaultReportBuilder.class, result);
    }

    @Test
    void should_ReturnDefaultBuilder_When_PathSourceIsProvided() {
        // Given
        DefaultReportBuilderProvider provider = new DefaultReportBuilderProvider();
        String source = "report.jrxml";

        // When
        ReportBuilder.CompileStep result = provider.from(source);

        // Then
        assertInstanceOf(DefaultReportBuilder.class, result);
    }

    @Test
    void should_ReturnDefaultBuilder_When_DesignSourceIsProvided() {
        // Given
        DefaultReportBuilderProvider provider = new DefaultReportBuilderProvider();
        JasperDesign source = new JasperDesign();

        // When
        ReportBuilder.CompileStep result = provider.from(source);

        // Then
        assertInstanceOf(DefaultReportBuilder.class, result);
    }
}
