package reyga.starter.foundation.common_io.reports;

import net.sf.jasperreports.engine.design.JasperDesign;

import java.util.ServiceLoader;

public interface ReportBuilderProvider {
    ReportBuilder.CompileStep from(byte[] jrxmlBytes);
    ReportBuilder.CompileStep from(String jrxmlPath);
    ReportBuilder.CompileStep from(JasperDesign jasperDesign);

    static ReportBuilderProvider getProvider() {
        return ServiceLoader.load(ReportBuilderProvider.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No implementation of ReportBuilderProvider found. Please make sure the internal module is included."));
    }
}
