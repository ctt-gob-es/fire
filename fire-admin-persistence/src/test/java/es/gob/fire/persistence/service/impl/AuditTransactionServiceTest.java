package es.gob.fire.persistence.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

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

        this.expectedListAuditTransactions = new ArrayList<>();

        for (int i = 1; i < 5; i++) {
        	if (i == 1) {
        		this.expectedAuditTransaction = generateSampleTransaction(i);
        	}
        	this.expectedListAuditTransactions.add(generateSampleTransaction(i));
        }

    }

    private AuditTransaction generateSampleTransaction(final int idAuditTransaction) {
    	final AuditTransaction at = new AuditTransaction();

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

    private AuditSignature generateSampleSignature(final String auditTransaction) {
    	final AuditSignature as = new AuditSignature();

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
    	when(this.auditTransactionRepository.findByIdAuditTransaction(1)).thenReturn(this.expectedAuditTransaction);

        final AuditTransaction result = this.auditTransactionService.getAuditTransactionByAuditTransactionId(1);

        verify(this.auditTransactionRepository).findByIdAuditTransaction(1);

        assertEquals(this.expectedAuditTransaction, result);
	}

    @Test
    public void testGetAllAuditTransactions() {
        when(this.auditTransactionRepository.findAll()).thenReturn(this.expectedListAuditTransactions);

        final List<AuditTransaction> result = this.auditTransactionService.getAllAuditTransactions();

        verify(this.auditTransactionRepository).findAll();

        assertEquals(this.expectedListAuditTransactions, result);
    }

    @Test
    public void testGetAllAuditTransactionsDataTablesInput() {
    	final DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<>();
    	final DataTablesInput input = new DataTablesInput();

    	expectedResult.setData(this.expectedListAuditTransactions);

    	when(this.auditTransactionDataTablesRepository.findAll(input)).thenReturn(expectedResult);

    	final DataTablesOutput<AuditTransaction> result = this.auditTransactionService.getAllAuditTransactions(input);

    	verify(this.auditTransactionDataTablesRepository).findAll(input);

    	assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterCompareMode1() {
		final DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<>();
    	final DataTablesInput input = new DataTablesInput();

    	expectedResult.setData(this.expectedListAuditTransactions);

    	final String startDateFilterString = "2020-01-01";
    	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (final ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		final Date endDate = new Date();

		when(this.auditTransactionRepository.findByDateBetween(startDate, endDate)).thenReturn(this.expectedListAuditTransactions);

		final DataTablesOutput<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate);

		verify(this.auditTransactionRepository).findByDateBetween(startDate, endDate);

    	assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterCompareMode2() {
		final DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<>();
    	final DataTablesInput input = new DataTablesInput();

    	expectedResult.setData(this.expectedListAuditTransactions);

		final Date startDate = null;
		final Date endDate = new Date();

		when(this.auditTransactionRepository.findByDateBefore(endDate)).thenReturn(this.expectedListAuditTransactions);

		final DataTablesOutput<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate);

		verify(this.auditTransactionRepository).findByDateBefore(endDate);

    	assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterCompareMode3() {
		final DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<>();
    	final DataTablesInput input = new DataTablesInput();

    	expectedResult.setData(this.expectedListAuditTransactions);

    	final String startDateFilterString = "2020-01-01";
    	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    	Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (final ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		final Date endDate = null;

		when(this.auditTransactionRepository.findByDateAfter(startDate)).thenReturn(this.expectedListAuditTransactions);

		final DataTablesOutput<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate);

		verify(this.auditTransactionRepository).findByDateAfter(startDate);

    	assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterCompareModeDefault() {
		final DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<>();
    	final DataTablesInput input = new DataTablesInput();

    	expectedResult.setData(this.expectedListAuditTransactions);

		final Date startDate = null;
		final Date endDate = null;

		when(this.auditTransactionRepository.findAll()).thenReturn(this.expectedListAuditTransactions);

		final DataTablesOutput<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate);

		verify(this.auditTransactionRepository).findAll();

    	assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAllAuditSignaturesOfTransaction() {


		final List<AuditSignature> expectedListAuditSignatures = generateExpectedSignatures();

        when(this.auditSignatureRepository.findByIdTransaction(this.expectedAuditTransaction.getIdTransaction())).thenReturn(expectedListAuditSignatures);

        final DataTablesOutput<AuditSignature> result = this.auditTransactionService.getAllAuditSignaturesOfTransaction(new DataTablesInput(), this.expectedAuditTransaction);

        assertNotNull(result);
        assertEquals(expectedListAuditSignatures.size(), result.getData().size());
	}

	private List<AuditSignature> generateExpectedSignatures() {
		final List<AuditSignature> expectedListAuditSignatures = new ArrayList<>();

		for (int i = 1; i < 3; i++){
			expectedListAuditSignatures.add(generateSampleSignature(this.expectedAuditTransaction.getIdTransaction()));
		}

		return expectedListAuditSignatures;
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterListCompareMode1() {
		final String startDateFilterString = "2020-01-01";
    	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (final ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		final Date endDate = new Date();

		when(this.auditTransactionRepository.findByDateBetween(startDate, endDate)).thenReturn(this.expectedListAuditTransactions);

        final List<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(startDate, endDate);

        assertNotNull(result);
        assertEquals(this.expectedListAuditTransactions.size(), result.size());
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterListCompareMode2() {
		final Date startDate = null;
		final Date endDate = new Date();

		when(this.auditTransactionRepository.findByDateBefore(endDate)).thenReturn(this.expectedListAuditTransactions);

        final List<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(startDate, endDate);

        assertNotNull(result);
        assertEquals(this.expectedListAuditTransactions.size(), result.size());
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterListCompareMode3() {
    	final String startDateFilterString = "2020-01-01";
    	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    	Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (final ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		final Date endDate = null;

		when(this.auditTransactionRepository.findByDateAfter(startDate)).thenReturn(this.expectedListAuditTransactions);

        final List<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(startDate, endDate);

        assertNotNull(result);
        assertEquals(this.expectedListAuditTransactions.size(), result.size());
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterListCompareModeDefault() {
		final Date startDate = null;
		final Date endDate = null;

		when(this.auditTransactionRepository.findAll()).thenReturn(this.expectedListAuditTransactions);

        final List<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(startDate, endDate);

        assertNotNull(result);
        assertEquals(this.expectedListAuditTransactions.size(), result.size());
	}

	@Test
	public void testGetAllAuditSignature() {
		final List<AuditSignature> expectedListAuditSignatures = generateExpectedSignatures();

		when(this.auditSignatureRepository.findAll()).thenReturn(expectedListAuditSignatures);

		final List<AuditSignature> result = this.auditTransactionService.getAllAuditSignature();

		assertNotNull(result);
		assertEquals(expectedListAuditSignatures.size(), result.size());
	}

	@Test
	public void testGetAuditTransactionsWithDateFilterWithApp() {
		final DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<>();
    	final DataTablesInput input = new DataTablesInput();

    	expectedResult.setData(this.expectedListAuditTransactions);

    	final String startDateFilterString = "2020-01-01";
    	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    	Date startDate = null;
		try {
			startDate = dateFormat.parse(startDateFilterString);
		} catch (final ParseException e) {
			fail("Ha ocurrido un error al convertir la fecha de inicio.");
		}
		final Date endDate = new Date();

		final String app = "testApp";

		when(this.auditTransactionRepository.findByDateRangeAndApplication(startDate, endDate, app)).thenReturn(this.expectedListAuditTransactions);

		final DataTablesOutput<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsWithDateFilter(input, startDate, endDate, app);

		assertNotNull(result);
		assertEquals(expectedResult, result);
	}

	@Test
	public void testGetAuditTransactionsFirstQuery() {
		final DataTablesOutput<AuditTransaction> expectedResult = new DataTablesOutput<>();
    	final DataTablesInput input = new DataTablesInput();

    	expectedResult.setData(this.expectedListAuditTransactions);

    	final Integer minutesProperty = 20;

    	when(this.auditTransactionRepository.findByDateAfter(any(Date.class)))
        .thenReturn(this.expectedListAuditTransactions);
    	
		final DataTablesOutput<AuditTransaction> result = this.auditTransactionService.getAuditTransactionsFirstQuery(input, minutesProperty);

		assertNotNull(result);
		assertEquals(expectedResult.getData().size(), result.getData().size());
	}

	@Test
	public void testGetApplicationsDropdown() {
		final List<String> expectedResult = new ArrayList<>();

		when(this.auditTransactionRepository.findDistinctApp()).thenReturn(expectedResult);

		final List<String> result = this.auditTransactionService.getApplicationsDropdown();

		assertNotNull(result);
		assertEquals(expectedResult.size(), result.size());
	}
}
