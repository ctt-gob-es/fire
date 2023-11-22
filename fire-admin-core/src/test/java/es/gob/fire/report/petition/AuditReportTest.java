package es.gob.fire.report.petition;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import es.gob.fire.persistence.entity.AuditSignature;
import es.gob.fire.persistence.entity.AuditTransaction;

public class AuditReportTest {

	private AuditReport auditReport;

    @Before
    public void setUp() {
        Map<Object, Object> parameters = new HashMap<>();
        // Aquí debes configurar los parámetros necesarios para las pruebas
        // Puedes agregar datos de ejemplo para listTransactions y listSignatures.
        List<AuditTransaction> listTransactions = new ArrayList<>();
        for (int i = 0; i < 5 ; i++){
        	listTransactions.add(generateSampleTransaction(i + 1));
        }
        listTransactions.add(generateSampleBatchTransaction(listTransactions.size() + 1));
        
        // Agregar transacciones de ejemplo a listTransactions
        List<AuditSignature> listSignatures = new ArrayList<>();
        listSignatures.add(generateSampleSignature("8ecc86b5-a3a8-4585-85fe-c8624cd9fb6e"));
        listSignatures.add(generateSampleErrorSignature("8ecc86b5-a3a8-4585-85fe-c8624cd9fb6f"));
        
        
        // Agregar firmas de ejemplo a listSignatures
        parameters.put("listTransactions", listTransactions);
        parameters.put("listSignatures", listSignatures);
        
        auditReport = new AuditReport(parameters);
    }

	@Test
    public void testGetReport() {
        try {
            byte[] report = auditReport.getReport();
            assertNotNull(report);
            // Aquí puedes agregar más aserciones para verificar el contenido del informe generado si es necesario.
        } catch (Exception e) {
            fail("Se produjo una excepción al generar el informe: " + e.getMessage());
        }
    }
	
	private AuditTransaction generateSampleTransaction(int idAuditTransaction) {
    	AuditTransaction at = new AuditTransaction();
    	
    	at.setIdAuditTransaction(idAuditTransaction);
    	at.setIdApp("0000001");
    	at.setNameApp("testApp");
    	at.setIdTransaction("8ecc86b5-a3a8-4585-85fe-c8624cd9fb6e");
    	at.setOperation("SIGN");
    	at.setCryptoOperation("sign");
    	at.setFormat("CAdES");
    	at.setAlgorithm("SHA256withRSA");
    	at.setProvider("test");
    	at.setForcedProvider(false);
    	at.setBrowser("Chrome");
    	at.setSize(100000);
    	at.setNode("testNode");
    	at.setResult(true);
    	at.setDate(new Date());
		return at;
    }
	
	private AuditTransaction generateSampleBatchTransaction(int idAuditTransaction) {
    	AuditTransaction at = new AuditTransaction();
    	
    	at.setIdAuditTransaction(idAuditTransaction);
    	at.setIdApp("0000001");
    	at.setNameApp("testApp");
    	at.setIdTransaction("8ecc86b5-a3a8-4585-85fe-c8624cd9fb6f");
    	at.setOperation("BATCH");
    	at.setCryptoOperation("sign");
    	at.setFormat("CAdES");
    	at.setAlgorithm("SHA256withRSA");
    	at.setProvider("test");
    	at.setForcedProvider(false);
    	at.setBrowser("Chrome");
    	at.setSize(100000);
    	at.setNode("testNode");
    	at.setResult(true);
    	at.setDate(new Date());
		return at;
    }
	
	private AuditSignature generateSampleSignature(String auditTransaction) {
    	AuditSignature as = new AuditSignature();
    	
    	as.setIdIntLote("1");
    	as.setIdTransaction(auditTransaction);
    	as.setCryptoOperation("sign");
    	as.setFormat("CAdES");
    	as.setSize(100000);
    	as.setResult(true);
    	
		return as;
    }
	
	private AuditSignature generateSampleErrorSignature(String auditTransaction) {
    	AuditSignature as = new AuditSignature();
    	
    	as.setIdIntLote("1");
    	as.setIdTransaction(auditTransaction);
    	as.setCryptoOperation("sign");
    	as.setFormat("CAdES");
    	as.setSize(100000);
    	as.setResult(false);
    	as.setErrorDetail("Error en la generacion");
    	
		return as;
    }
}
