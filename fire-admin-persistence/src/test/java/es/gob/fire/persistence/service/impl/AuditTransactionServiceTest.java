package es.gob.fire.persistence.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import es.gob.fire.persistence.entity.AuditSignature;
import es.gob.fire.persistence.entity.AuditTransaction;
import es.gob.fire.persistence.repository.AuditSignatureRepository;
import es.gob.fire.persistence.repository.AuditTransactionRepository;
import es.gob.fire.persistence.repository.datatable.AuditSignatureDataTablesRepository;
import es.gob.fire.persistence.repository.datatable.AuditTransactionDataTablesRepository;

@RunWith(MockitoJUnitRunner.class)
public class AuditTransactionServiceTest {

    @InjectMocks
    private AuditTransactionService auditTransactionService;
    
    @Mock
    private AuditTransactionRepository auditTransactionRepository;

    @Mock
    private AuditSignatureRepository auditSignatureRepository;

    @Mock
    private AuditTransactionDataTablesRepository auditTransactionDataTablesRepository;

    @Mock
    private AuditSignatureDataTablesRepository auditSignatureDataTablesRepository;
    
    private AuditTransaction expectedAuditTransaction;
    
    private List<AuditTransaction> expectedListAuditTransactions;
    

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        expectedListAuditTransactions = new ArrayList<AuditTransaction>();
        
        for (int i = 1; i < 5; i++) {
        	if (i == 1) {
        		expectedAuditTransaction = generateSampleTransaction(i);
        	}
        	expectedListAuditTransactions.add(generateSampleTransaction(i));
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
    
    @Test
	public void testGetAuditTransactionByAuditTransactionId() {
    	when(auditTransactionRepository.findByIdAuditTransaction(1)).thenReturn(expectedAuditTransaction);

        AuditTransaction result = auditTransactionService.getAuditTransactionByAuditTransactionId(1);

        verify(auditTransactionRepository).findByIdAuditTransaction(1);

        assertEquals(expectedAuditTransaction, result);
	}
    
    @Test
    public void testGetAllAuditTransactions() {
        when(auditTransactionRepository.findAll()).thenReturn(expectedListAuditTransactions);

        List<AuditTransaction> result = auditTransactionService.getAllAuditTransactions();

        verify(auditTransactionRepository).findAll();

        assertEquals(expectedListAuditTransactions, result);
    }
    
    @Test
	public void testGetAllAuditTransactionsDataTablesInput() {
    	DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<AuditTransaction>();
    	DataTablesInput input = new DataTablesInput();
    	
    	expectedResult.setData(expectedListAuditTransactions);
    	
    	when(auditTransactionDataTablesRepository.findAll(input)).thenReturn(expectedResult);
    	
    	DataTablesOutput<AuditTransaction> result = auditTransactionService.getAllAuditTransactions(input);
    	
    	verify(auditTransactionDataTablesRepository).findAll(input);
    	
    	assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterCompareMode1() {
		DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<AuditTransaction>();
    	DataTablesInput input = new DataTablesInput();
    	
    	expectedResult.setData(expectedListAuditTransactions);
    	
    	String startDateFilterString = "2020-01-01";
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		Date endDate = new Date();
		
		when(auditTransactionRepository.findByDateBetween(startDate, endDate)).thenReturn(expectedListAuditTransactions);
		
		DataTablesOutput<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate);
    	
		verify(auditTransactionRepository).findByDateBetween(startDate, endDate);
    	
    	assertEquals(expectedResult, result);
	}
	
	@Test
	public void testGetAuditTransactionsWithDateFilterCompareMode2() {
		DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<AuditTransaction>();
    	DataTablesInput input = new DataTablesInput();
    	
    	expectedResult.setData(expectedListAuditTransactions);
		
		Date startDate = null;
		Date endDate = new Date();
		
		when(auditTransactionRepository.findByDateBefore(endDate)).thenReturn(expectedListAuditTransactions);
		
		DataTablesOutput<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate);
    	
		verify(auditTransactionRepository).findByDateBefore(endDate);
    	
    	assertEquals(expectedResult, result);
	}
	
	@Test
	public void testGetAuditTransactionsWithDateFilterCompareMode3() {
		DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<AuditTransaction>();
    	DataTablesInput input = new DataTablesInput();
    	
    	expectedResult.setData(expectedListAuditTransactions);
    	
    	String startDateFilterString = "2020-01-01";
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		Date endDate = null;
		
		when(auditTransactionRepository.findByDateAfter(startDate)).thenReturn(expectedListAuditTransactions);
		
		DataTablesOutput<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate);
    	
		verify(auditTransactionRepository).findByDateAfter(startDate);
    	
    	assertEquals(expectedResult, result);
	}
	
	@Test
	public void testGetAuditTransactionsWithDateFilterCompareModeDefault() {
		DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<AuditTransaction>();
    	DataTablesInput input = new DataTablesInput();
    	
    	expectedResult.setData(expectedListAuditTransactions);
    	
		Date startDate = null;
		Date endDate = null;
		
		when(auditTransactionRepository.findAll()).thenReturn(expectedListAuditTransactions);
		
		DataTablesOutput<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate);
    	
		verify(auditTransactionRepository).findAll();
    	
    	assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAllAuditSignaturesOfTransaction() {

	    
		List<AuditSignature> expectedListAuditSignatures = generateExpectedSignatures();
		
        when(auditSignatureRepository.findByIdTransaction(expectedAuditTransaction.getIdTransaction())).thenReturn(expectedListAuditSignatures);

        DataTablesOutput<AuditSignature> result = auditTransactionService.getAllAuditSignaturesOfTransaction(new DataTablesInput(), expectedAuditTransaction);

        assertNotNull(result);
        assertEquals(expectedListAuditSignatures.size(), result.getData().size());
	}

	private List<AuditSignature> generateExpectedSignatures() {
		List<AuditSignature> expectedListAuditSignatures = new ArrayList<>();
		
		for (int i = 1; i < 3; i++){
			expectedListAuditSignatures.add(generateSampleSignature(expectedAuditTransaction.getIdTransaction()));
		}
		
		return expectedListAuditSignatures;
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterListCompareMode1() {
		String startDateFilterString = "2020-01-01";
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		Date endDate = new Date();
		
		when(auditTransactionRepository.findByDateBetween(startDate, endDate)).thenReturn(expectedListAuditTransactions);

        List<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(startDate, endDate);

        assertNotNull(result);
        assertEquals(expectedListAuditTransactions.size(), result.size());
	}
	
	@Test
	public void testGetAuditTransactionsWithDateFilterListCompareMode2() {
		Date startDate = null;
		Date endDate = new Date();
		
		when(auditTransactionRepository.findByDateBefore(endDate)).thenReturn(expectedListAuditTransactions);

        List<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(startDate, endDate);

        assertNotNull(result);
        assertEquals(expectedListAuditTransactions.size(), result.size());
	}
	
	@Test
	public void testGetAuditTransactionsWithDateFilterListCompareMode3() {
    	String startDateFilterString = "2020-01-01";
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		Date endDate = null;
		
		when(auditTransactionRepository.findByDateAfter(startDate)).thenReturn(expectedListAuditTransactions);

        List<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(startDate, endDate);

        assertNotNull(result);
        assertEquals(expectedListAuditTransactions.size(), result.size());
	}
	
	@Test
	public void testGetAuditTransactionsWithDateFilterListCompareModeDefault() {
		Date startDate = null;
		Date endDate = null;
		
		when(auditTransactionRepository.findAll()).thenReturn(expectedListAuditTransactions);

        List<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(startDate, endDate);

        assertNotNull(result);
        assertEquals(expectedListAuditTransactions.size(), result.size());
	}

	@Test
	public void testGetAllAuditSignature() {
		List<AuditSignature> expectedListAuditSignatures = generateExpectedSignatures();
		
		when(auditSignatureRepository.findAll()).thenReturn(expectedListAuditSignatures);
		
		List<AuditSignature> result = auditTransactionService.getAllAuditSignature();
		
		assertNotNull(result);
		assertEquals(expectedListAuditSignatures.size(), result.size());
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterWithApp() {
		DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<AuditTransaction>();
    	DataTablesInput input = new DataTablesInput();
    	
    	expectedResult.setData(expectedListAuditTransactions);
    	
    	String startDateFilterString = "2020-01-01";
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		Date endDate = new Date();
		
		String app = "testApp";
		
		when(auditTransactionRepository.findByDateRangeAndApplication(startDate, endDate, app)).thenReturn(expectedListAuditTransactions);
		
		DataTablesOutput<AuditTransaction> result = auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate, app);
	
		assertNotNull(result);
		assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAuditTransactionsFirstQuery() {
		DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<AuditTransaction>();
    	DataTablesInput input = new DataTablesInput();
    	
    	expectedResult.setData(expectedListAuditTransactions);
    	
    	Integer minutesProperty = 20;
    	
    	when(auditTransactionRepository.findByDateAfter(new Date(System.currentTimeMillis() - minutesProperty * 60 * 1000))).thenReturn(expectedListAuditTransactions);
		
		DataTablesOutput<AuditTransaction> result = auditTransactionService.getAuditTransactionsFirstQuery(input, minutesProperty);
	
		assertNotNull(result);
		assertEquals(expectedResult.getData().size(), result.getData().size());
	}

	@Test
	public void testGetApplicationsDropdown() {
		List<String> expectedResult = new ArrayList<>();
		
		when(auditTransactionRepository.findDistinctApp()).thenReturn(expectedResult);
		
		List<String> result = auditTransactionService.getApplicationsDropdown();
		
		assertNotNull(result);
		assertEquals(expectedResult.size(), result.size());
	}
    
}
