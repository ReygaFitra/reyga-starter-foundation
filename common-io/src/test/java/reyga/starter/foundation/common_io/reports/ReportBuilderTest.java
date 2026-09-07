package reyga.starter.foundation.common_io.reports;

import net.sf.jasperreports.engine.design.JasperDesign;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportBuilderTest {

    private ClassLoader originalContextClassLoader;

    @BeforeEach
    void setUp() {
        originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        TestReportBuilderProvider.reset();
    }

    @AfterEach
    void tearDown() {
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Test
    void should_ReturnProviderCompileStep_When_InputStreamIsValid() throws IOException {
        // Given
        byte[] jrxml = "<jasperReport/>".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(jrxml);

        // When
        ReportBuilder.CompileStep result = ReportBuilder.from(inputStream);

        // Then
        assertSame(TestReportBuilderProvider.COMPILE_STEP, result);
        assertArrayEquals(jrxml, TestReportBuilderProvider.receivedBytes);
        assertEquals(1, TestReportBuilderProvider.byteInvocations);
        assertEquals(0, TestReportBuilderProvider.pathInvocations);
        assertEquals(0, TestReportBuilderProvider.designInvocations);
    }

    @Test
    void should_PropagateIOExceptionWithoutCallingProvider_When_InputStreamReadFails() {
        // Given
        IOException expectedCause = new IOException("read failed");
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw expectedCause;
            }
        };

        // When
        IOException exception = assertThrows(IOException.class, () -> ReportBuilder.from(inputStream));

        // Then
        assertSame(expectedCause, exception);
        assertEquals(0, TestReportBuilderProvider.byteInvocations);
        assertNull(TestReportBuilderProvider.receivedBytes);
    }

    @Test
    void should_ThrowNullPointerException_When_InputStreamIsNull() {
        // Given
        InputStream inputStream = null;

        // When
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ReportBuilder.from(inputStream)
        );

        // Then
        assertEquals("jrxmlInputStream must not be null", exception.getMessage());
        assertEquals(0, TestReportBuilderProvider.byteInvocations);
    }

    @Test
    void should_ReturnProviderCompileStep_When_JrxmlPathIsValid() {
        // Given
        String path = "reports/invoice.jrxml";

        // When
        ReportBuilder.CompileStep result = ReportBuilder.from(path);

        // Then
        assertSame(TestReportBuilderProvider.COMPILE_STEP, result);
        assertEquals(path, TestReportBuilderProvider.receivedPath);
        assertEquals(1, TestReportBuilderProvider.pathInvocations);
        assertEquals(0, TestReportBuilderProvider.byteInvocations);
        assertEquals(0, TestReportBuilderProvider.designInvocations);
    }

    @Test
    void should_ThrowExpectedExceptionWithoutCallingProvider_When_JrxmlPathIsInvalid() {
        // Given
        String nullPath = null;
        String blankPath = "   ";

        // When
        NullPointerException nullException = assertThrows(
                NullPointerException.class,
                () -> ReportBuilder.from(nullPath)
        );
        IllegalArgumentException blankException = assertThrows(
                IllegalArgumentException.class,
                () -> ReportBuilder.from(blankPath)
        );

        // Then
        assertEquals("jrxmlPath must not be null", nullException.getMessage());
        assertEquals("jrxmlPath must not be blank", blankException.getMessage());
        assertEquals(0, TestReportBuilderProvider.pathInvocations);
        assertNull(TestReportBuilderProvider.receivedPath);
    }

    @Test
    void should_ReturnProviderCompileStep_When_JasperDesignIsValid() {
        // Given
        JasperDesign design = new JasperDesign();

        // When
        ReportBuilder.CompileStep result = ReportBuilder.from(design);

        // Then
        assertSame(TestReportBuilderProvider.COMPILE_STEP, result);
        assertSame(design, TestReportBuilderProvider.receivedDesign);
        assertEquals(1, TestReportBuilderProvider.designInvocations);
        assertEquals(0, TestReportBuilderProvider.byteInvocations);
        assertEquals(0, TestReportBuilderProvider.pathInvocations);
    }

    @Test
    void should_ThrowNullPointerExceptionWithoutCallingProvider_When_JasperDesignIsNull() {
        // Given
        JasperDesign design = null;

        // When
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ReportBuilder.from(design)
        );

        // Then
        assertEquals("jasperDesign must not be null", exception.getMessage());
        assertEquals(0, TestReportBuilderProvider.designInvocations);
        assertNull(TestReportBuilderProvider.receivedDesign);
    }

    @Test
    void should_ReturnDiscoveredProvider_When_ServiceRegistrationExists() {
        // Given
        TestReportBuilderProvider.reset();

        // When
        ReportBuilderProvider result = ReportBuilderProvider.getProvider();

        // Then
        assertEquals(TestReportBuilderProvider.class, result.getClass());
        assertEquals(0, TestReportBuilderProvider.byteInvocations);
        assertEquals(0, TestReportBuilderProvider.pathInvocations);
        assertEquals(0, TestReportBuilderProvider.designInvocations);
    }

    @Test
    void should_ThrowIllegalStateException_When_ServiceRegistrationIsMissing() {
        // Given
        String serviceResource = "META-INF/services/" + ReportBuilderProvider.class.getName();
        ClassLoader noProviderClassLoader = new ClassLoader(originalContextClassLoader) {
            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if (serviceResource.equals(name)) {
                    return Collections.emptyEnumeration();
                }
                return super.getResources(name);
            }
        };
        Thread.currentThread().setContextClassLoader(noProviderClassLoader);

        // When
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                ReportBuilderProvider::getProvider
        );

        // Then
        assertEquals(
                "No implementation of ReportBuilderProvider found. Please make sure the internal module is included.",
                exception.getMessage()
        );
        assertEquals(0, TestReportBuilderProvider.byteInvocations);
        assertEquals(0, TestReportBuilderProvider.pathInvocations);
        assertEquals(0, TestReportBuilderProvider.designInvocations);
    }
}
