package es.gob.fire.statistics.entity;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuditSignatureCubeTest {

    private AuditSignatureCube sampleSignatureCube;

	@Before
    public void setUp() {
		sampleSignatureCube = new AuditSignatureCube();
		
    	sampleSignatureCube.setIdTransaction("123");
        sampleSignatureCube.setIdIntLote("ABC");
        sampleSignatureCube.setCryptoOperation("SIGN");
        sampleSignatureCube.setFormat("PKCS7");
        sampleSignatureCube.setImprovedFormat("CMS");
        sampleSignatureCube.setDataSize(1024);
        sampleSignatureCube.setErrorDetail("ErrorDetail");
        sampleSignatureCube.setResult(true);
    }

	@Test
	public void testHashCode() {
        int result = sampleSignatureCube.hashCode();

        assertNotEquals(0, result);
	}

	@Test
    public void testParse() {
        String registry = "3810e59c-50a3-4e8c-8067-ce659bc7c040;3;sign;CAdES;;913609;;0";
        AuditSignatureCube result = AuditSignatureCube.parse(registry);

        assertEquals("3810e59c-50a3-4e8c-8067-ce659bc7c040", result.getIdTransaction());
        assertEquals("3", result.getIdIntLote());
        assertEquals("sign", result.getCryptoOperation());
        assertEquals("CAdES", result.getFormat());
        assertEquals("", result.getImprovedFormat());
        assertEquals(Long.parseLong("913609"), result.getDataSize());
        assertEquals("", result.getErrorDetail());
        assertEquals(false, result.isResult());
    }

	@Test
	public void testEqualsObject() {
		AuditSignatureCube testSignatureCube = new AuditSignatureCube();
		testSignatureCube.setIdTransaction("123");
		testSignatureCube.setIdIntLote("ABC");
		testSignatureCube.setCryptoOperation("SIGN");
		testSignatureCube.setFormat("PKCS7");
		testSignatureCube.setImprovedFormat("CMS");
		testSignatureCube.setDataSize(1024);
		testSignatureCube.setErrorDetail("ErrorDetail");
		testSignatureCube.setResult(true);
		
		AuditSignatureCube testSignatureCube2 = new AuditSignatureCube();
		testSignatureCube2.setIdTransaction("123");
		testSignatureCube2.setIdIntLote("ABC");
		testSignatureCube2.setCryptoOperation("SIGN");
		testSignatureCube2.setFormat("PKCS7");
		testSignatureCube2.setImprovedFormat("CMS");
		testSignatureCube2.setDataSize(1024);
		testSignatureCube2.setErrorDetail("ErrorDetail");
		testSignatureCube2.setResult(true);
		
		boolean result = testSignatureCube2.equals(testSignatureCube);
		
		assertEquals(true, result);
	}

	@Test
	public void testToString() {
		AuditSignatureCube testSignatureCube = new AuditSignatureCube();
		testSignatureCube.setIdTransaction("123");
		testSignatureCube.setIdIntLote("ABC");
		testSignatureCube.setCryptoOperation("SIGN");
		testSignatureCube.setFormat("PKCS7");
		testSignatureCube.setImprovedFormat("CMS");
		testSignatureCube.setDataSize(1024);
		testSignatureCube.setErrorDetail("ErrorDetail");
		testSignatureCube.setResult(true);
		
		String result = testSignatureCube.toString();
		
		assertEquals("123;ABC;SIGN;PKCS7;CMS;1024;ErrorDetail;1", result);
	}

	@Test
    public void testCheckErrorDetailBothNull() {
        AuditSignatureCube signature1 = new AuditSignatureCube();
        signature1.setErrorDetail(null);
        
        AuditSignatureCube signature2 = new AuditSignatureCube();
        signature2.setErrorDetail(null);

        int result = signature1.checkErrorDetail(signature2);

        assertEquals(1, result);
    }
	
	@Test
    public void testCheckErrorDetailBothEqual() {
		AuditSignatureCube signature1 = new AuditSignatureCube();
        signature1.setErrorDetail("ErrorDetail");
        
        AuditSignatureCube signature2 = new AuditSignatureCube();
        signature2.setErrorDetail("ErrorDetail");

        int result = signature1.checkErrorDetail(signature2);

        assertEquals(1, result);
    }
	
	@Test
    public void testCheckErrorDetailBothDifferent() {
		AuditSignatureCube signature1 = new AuditSignatureCube();
        signature1.setErrorDetail("ErrorDetail1");
        
        AuditSignatureCube signature2 = new AuditSignatureCube();
        signature2.setErrorDetail("ErrorDetail2");

        int result = signature1.checkErrorDetail(signature2);

        assertEquals(2, result);
    }
	
	@Test
    public void testCheckErrorDetailFirstNull() {
		AuditSignatureCube signature1 = new AuditSignatureCube();
        signature1.setErrorDetail(null);
        
        AuditSignatureCube signature2 = new AuditSignatureCube();
        signature2.setErrorDetail("ErrorDetail");

        int result = signature1.checkErrorDetail(signature2);

        assertEquals(3, result);
    }
	
	@Test
    public void testCheckErrorDetailSecondNull() {
		AuditSignatureCube signature1 = new AuditSignatureCube();
        signature1.setErrorDetail("ErrorDetail");
        
        AuditSignatureCube signature2 = new AuditSignatureCube();
        signature2.setErrorDetail(null);

        int result = signature1.checkErrorDetail(signature2);

        assertEquals(4, result);
    }

}
