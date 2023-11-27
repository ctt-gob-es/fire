package es.gob.fire.report.common;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import es.gob.fire.report.petition.AuditReport;

public class ReportGeneratorTest {

    @Test
    public void testGetAuditReport() {
        Map<Object, Object> parameters = new HashMap<>();
        parameters.put("parametro", "valor");

        Report report = ReportGenerator.getReport("AUDIT", parameters);

        assertNotNull(report);
        assertTrue(report instanceof AuditReport);
    }
    
    @Test
    public void testGetNullReport() {
        Map<Object, Object> parameters = new HashMap<>();
        parameters.put("parametro", "valor");

        Report report = ReportGenerator.getReport("NULL", parameters);

        assertNull(report);
    }
}
