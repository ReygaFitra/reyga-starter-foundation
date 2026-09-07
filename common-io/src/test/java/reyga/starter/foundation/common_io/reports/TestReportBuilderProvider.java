package reyga.starter.foundation.common_io.reports;

import net.sf.jasperreports.engine.design.JasperDesign;

public final class TestReportBuilderProvider implements ReportBuilderProvider {

    static byte[] receivedBytes;
    static String receivedPath;
    static JasperDesign receivedDesign;
    static int byteInvocations;
    static int pathInvocations;
    static int designInvocations;

    static final ReportBuilder.CompileStep COMPILE_STEP = () -> null;

    static void reset() {
        receivedBytes = null;
        receivedPath = null;
        receivedDesign = null;
        byteInvocations = 0;
        pathInvocations = 0;
        designInvocations = 0;
    }

    @Override
    public ReportBuilder.CompileStep from(byte[] jrxmlBytes) {
        receivedBytes = jrxmlBytes;
        byteInvocations++;
        return COMPILE_STEP;
    }

    @Override
    public ReportBuilder.CompileStep from(String jrxmlPath) {
        receivedPath = jrxmlPath;
        pathInvocations++;
        return COMPILE_STEP;
    }

    @Override
    public ReportBuilder.CompileStep from(JasperDesign jasperDesign) {
        receivedDesign = jasperDesign;
        designInvocations++;
        return COMPILE_STEP;
    }
}
