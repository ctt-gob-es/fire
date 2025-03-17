package es.gob.fire.web.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import es.gob.fire.persistence.service.IAuditTransactionService;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class AuditRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private AuditRestController auditRestController;

    @Mock
    private IAuditTransactionService auditTransactionService;


    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.auditRestController).build();
        MockitoAnnotations.initMocks(this);
        this.auditRestController.setAuditTimeProperty("20");
    }

    @Test
    public void testGetPetitionsDatatable() throws Exception {

        this.mockMvc.perform(get("/auditTransactionDatatable"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetPetitionsDatatableWithFilter() throws Exception {
        this.mockMvc.perform(get("/auditTransactionDatatableWithFilter")
            .param("from", "2020/01/01 00:00:00")
            .param("to", "2023/12/31 23:59:59")
            .param("app", "testApp"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditSignaturesOfTransaction() throws Exception {

        this.mockMvc.perform(get("/getAuditSignaturesOfTransaction")
            .param("idAuditTransaction", "1"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGeneratePetitionsReport() throws Exception {

        this.mockMvc.perform(get("/exportAudit")
            .param("from", "2020/01/01 00:00:00")
            .param("to", "2023/12/31 23:59:59"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetPetitionsFirstLoad() throws Exception {

        this.mockMvc.perform(get("/getAuditTransactionsFirstLoad"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditFirstLoadFromDate() throws Exception {

        this.mockMvc.perform(get("/getAuditTransactionsFirstLoadFromDate"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditFirstLoadToDate() throws Exception {

        this.mockMvc.perform(get("/getAuditTransactionsFirstLoadToDate"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditFilterLoadDate1() throws Exception {

        this.mockMvc.perform(get("/getAuditTransactionsFilterLoadDate")
    		.param("from", "2020/01/01 00:00:00")
            .param("to", "2023/12/31 23:59:59")
            .param("app", "testApp"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditFilterLoadDate2() throws Exception {

        this.mockMvc.perform(get("/getAuditTransactionsFilterLoadDate")
    		.param("from", "2020/01/01 00:00:00")
            .param("to", "")
            .param("app", "testApp"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditFilterLoadDate3() throws Exception {

        this.mockMvc.perform(get("/getAuditTransactionsFilterLoadDate")
            .param("from", "")
            .param("to", "2023/12/31 23:59:59")
            .param("app", "testApp"))
            .andExpect(status().isOk());
    }

    @Test
    public void testGetAuditFilterLoadDateDefault() throws Exception {

        this.mockMvc.perform(get("/getAuditTransactionsFilterLoadDate")
            .param("from", "")
            .param("to", "")
            .param("app", "testApp"))
            .andExpect(status().isOk());
    }
}

