package es.gob.fire.report.common;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import es.gob.fire.report.petition.AuditReport;

public class ReportTest {
	
	private Report report;
    private Map<Object, Object> expectedParameters;

	@Before
	public void setUp() throws Exception {
		expectedParameters = new HashMap<>();
		expectedParameters.put("parametro", "valor");
		report = new AuditReport(expectedParameters);
	}

	@Test
    public void testGetParameters() {
        Map<Object, Object> parameters = report.getParameters();
        assertNotNull(parameters);
        assertEquals(expectedParameters, parameters);
    }

	@Test
    public void testSetParameters() {
        Map<Object, Object> newParameters = new HashMap<>();
        newParameters.put("parametro", "valor");
        report.setParameters(newParameters);

        Map<Object, Object> parameters = report.getParameters();
        assertNotNull(parameters);
        assertEquals(newParameters, parameters);
    }
}
