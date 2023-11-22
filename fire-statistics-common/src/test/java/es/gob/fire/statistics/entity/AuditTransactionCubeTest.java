package es.gob.fire.statistics.entity;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class AuditTransactionCubeTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testHashCode() {
		AuditTransactionCube transactionCube = new AuditTransactionCube();
        transactionCube.setIdTransaction("123");
        transactionCube.setDate(new Date());
        transactionCube.setIdApplication("AppId");
        transactionCube.setNameApplication("AppName");
        transactionCube.setOperation("Operation");
        transactionCube.setCryptoOperation("CryptoOp");
        transactionCube.setFormat("Format");
        transactionCube.setImprovedFormat("ImprovedFormat");
        transactionCube.setAlgorithm("Algorithm");
        transactionCube.setProvider("Provider");
        transactionCube.setMandatoryProvider(true);
        transactionCube.setBrowser("Browser");
        transactionCube.setNode("Node");
        transactionCube.setErrorDetail("ErrorDetail");
        transactionCube.setResult(true);
        
        int result = transactionCube.hashCode();

        assertNotEquals(0, result);
	}

	@Test
	public void testParse() {
		String registry = "3b54a6db-3841-45aa-9f3c-56c38cb4116a;27/09/2023 00:59;16086E7DDBF6;testApp;BATCH;sign;CAdES;;SHA1withRSA;local;0;Chrome;ESMAD01LFMWYV93;;1";
		
		AuditTransactionCube result = AuditTransactionCube.parse(registry);
		
		assertEquals("3b54a6db-3841-45aa-9f3c-56c38cb4116a", result.getIdTransaction());
        assertEquals("16086E7DDBF6", result.getIdApplication());
        assertEquals("testApp", result.getNameApplication());
        assertEquals("BATCH", result.getOperation());
        assertEquals("sign", result.getCryptoOperation());
        assertEquals("CAdES", result.getFormat());
        assertEquals("", result.getImprovedFormat());
        assertEquals("SHA1withRSA", result.getAlgorithm());
        assertEquals("local", result.getProvider());
        assertEquals(false, result.isMandatoryProvider());
        assertEquals("Chrome", result.getBrowser());
        assertEquals("ESMAD01LFMWYV93", result.getNode());
        assertEquals("", result.getErrorDetail());
        assertEquals(true, result.isResult());
	}

	@Test
	public void testEqualsObject() {
		AuditTransactionCube cube1 = new AuditTransactionCube();
        cube1.setIdTransaction("123");
        cube1.setDate(new Date());
        cube1.setIdApplication("AppId");
        cube1.setNameApplication("AppName");
        cube1.setOperation("Operation");
        cube1.setCryptoOperation("CryptoOp");
        cube1.setFormat("Format");
        cube1.setImprovedFormat("ImprovedFormat");
        cube1.setAlgorithm("Algorithm");
        cube1.setProvider("Provider");
        cube1.setMandatoryProvider(true);
        cube1.setBrowser("Browser");
        cube1.setNode("Node");
        cube1.setErrorDetail("ErrorDetail");
        cube1.setResult(true);
        
        AuditTransactionCube cube2 = new AuditTransactionCube();
        cube2.setIdTransaction("123");
        cube2.setDate(new Date());
        cube2.setIdApplication("AppId");
        cube2.setNameApplication("AppName");
        cube2.setOperation("Operation");
        cube2.setCryptoOperation("CryptoOp");
        cube2.setFormat("Format");
        cube2.setImprovedFormat("ImprovedFormat");
        cube2.setAlgorithm("Algorithm");
        cube2.setProvider("Provider");
        cube2.setMandatoryProvider(true);
        cube2.setBrowser("Browser");
        cube2.setNode("Node");
        cube2.setErrorDetail("ErrorDetail");
        cube2.setResult(true);
        
        boolean result = cube2.equals(cube2);
		
		assertEquals(true, result);
	}

	@Test
	public void testToString() throws ParseException {
		String expectedResult = "3b54a6db-3841-45aa-9f3c-56c38cb4116a;27/09/2023 00:59;16086E7DDBF6;testApp;BATCH;sign;CAdES;;SHA1withRSA;local;0;Chrome;ESMAD01LFMWYV93;;1";
	
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date date = format.parse("27/09/2023 00:59");
		AuditTransactionCube transactionCube = new AuditTransactionCube();
        transactionCube.setIdTransaction("3b54a6db-3841-45aa-9f3c-56c38cb4116a");
        transactionCube.setDate(date);
        transactionCube.setIdApplication("16086E7DDBF6");
        transactionCube.setNameApplication("testApp");
        transactionCube.setOperation("BATCH");
        transactionCube.setCryptoOperation("sign");
        transactionCube.setFormat("CAdES");
        transactionCube.setImprovedFormat(null);
        transactionCube.setAlgorithm("SHA1withRSA");
        transactionCube.setProvider("local");
        transactionCube.setMandatoryProvider(false);
        transactionCube.setBrowser("Chrome");
        transactionCube.setNode("ESMAD01LFMWYV93");
        transactionCube.setErrorDetail(null);
        transactionCube.setResult(true);
        
        assertEquals(expectedResult, transactionCube.toString());
	}

}
