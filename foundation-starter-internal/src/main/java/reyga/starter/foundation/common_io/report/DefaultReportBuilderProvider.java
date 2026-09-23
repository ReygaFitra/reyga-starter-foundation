package reyga.starter.foundation.common_io.report;

import net.sf.jasperreports.engine.design.JasperDesign;
import reyga.starter.foundation.common_io.reports.ReportBuilder;
import reyga.starter.foundation.common_io.reports.ReportBuilderProvider;

public class DefaultReportBuilderProvider implements ReportBuilderProvider {
    @Override
    public ReportBuilder.CompileStep from(byte[] jrxmlBytes) {
        return new DefaultReportBuilder(jrxmlBytes);
    }

    @Override
    public ReportBuilder.CompileStep from(String jrxmlPath) {
        return new DefaultReportBuilder(jrxmlPath);
    }

    @Override
    public ReportBuilder.CompileStep from(JasperDesign jasperDesign) {
        return new DefaultReportBuilder(jasperDesign);
    }
}
