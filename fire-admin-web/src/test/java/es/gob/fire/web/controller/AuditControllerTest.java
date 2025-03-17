package es.gob.fire.web.controller;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import es.gob.fire.persistence.entity.AuditTransaction;
import es.gob.fire.persistence.service.IAuditTransactionService;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class AuditControllerTest {

	@Autowired
	private MockMvc mockMvc;

    @InjectMocks
    private AuditController auditController;

    @Mock
    private IAuditTransactionService auditTransactionService;

    private AuditTransaction expectedAuditTransaction;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.auditController).build();

        this.expectedAuditTransaction = generateSampleTransaction(1);
    }

    private AuditTransaction generateSampleTransaction(final int idAuditTransaction) {
    	final AuditTransaction at = new AuditTransaction();

    	at.setIdAuditTransaction(idAuditTransaction);
    	at.setIdApp("0000001");
    	at.setNameApp("testApp");
    	at.setIdTransaction("8ecc86b5-a3a8-4585-85fe-c8624cd9fb6e");
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
    	final String sampleDateString = "2022-01-01";
    	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	try {
			at.setDate(dateFormat.parse(sampleDateString));
		} catch (final ParseException e) {
			fail("Error al convertir la fecha de la transaccion");
		}

		return at;
    }

	@Test
    public void testGetAuditAdmin() throws Exception {

        this.mockMvc.perform(get("/auditadmin"))
            .andExpect(status().isOk())
            .andExpect(view().name("fragments/auditadmin.html"))
            .andExpect(model().attributeExists("applicationsDropdown"));
    }

    @Test
    public void testViewAuditTransactionDetails() throws Exception {

    	when(this.auditTransactionService.getAuditTransactionByAuditTransactionId(1)).thenReturn(this.expectedAuditTransaction);

        this.mockMvc.perform(get("/viewAuditTransactionDetails").param("idAuditTransaction", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("modal/auditTransactionView.html"))
            .andExpect(model().attributeExists("auditTransactionViewForm"))
            .andExpect(model().attribute("isBatch", true))
            .andExpect(model().attribute("disableInputs", true));
    }

}
