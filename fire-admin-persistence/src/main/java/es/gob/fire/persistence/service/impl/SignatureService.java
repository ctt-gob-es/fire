/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <b>File:</b><p>es.gob.fire.persistence.service.impl.SignatureService.java.</p> 
 * <b>Description:</b><p>Class that implements the communication with the operations of the persistence layer.</p>
 * <b>Project:</b><p>Platform for signing documents.</p>
 * <b>Date:</b><p>15/06/2018.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 15/06/2018.
 */
package es.gob.fire.persistence.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.dto.ApplicationDTO;
import es.gob.fire.persistence.dto.OrganizationDTO;
import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.entity.Signature;
import es.gob.fire.persistence.repository.SignatureRepository;
import es.gob.fire.persistence.service.ISignatureService;

/**
 * <p>
 * Class that implements the communication with the operations of the
 * persistence layer.
 * </p>
 * <b>Project:</b>
 * <p>
 * Platform for detection and validation of certificates recognized in European
 * TSL.
 * </p>
 * 
 * @version 1.0, 15/06/2018.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SignatureService implements ISignatureService {

    @Autowired
    private SignatureRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Override
    public Signature getSignatureBySignatureId(final Long signatureId) {
        return repository.findBySignatureId(signatureId);
    }
    
    // --------------------- MÉTODOS COMUNES Y GENÉRICOS ---------------------

    private <T> List<T> executeStatisticsQuery(
            String[] selectColumns,
            String[] groupByColumns,
            Integer sm, Integer sy, Integer em, Integer ey,
            List<String> apps, List<String> orgs,
            Function<Object[], T> mapper) {

        String sql = new StringBuilder()
            .append("SELECT ").append(String.join(", ", selectColumns))
            .append(" FROM tb_firmas f")
            .append(" WHERE ").append(dateCondition(sm, sy, em, ey))
            .append(buildFilterClause(apps, orgs))
            .append(" GROUP BY ").append(String.join(", ", groupByColumns))
            .toString();

        Map<String,Object> params = buildParameters(sm, sy, em, ey, apps, orgs);
        return executeQueryNamed(sql, mapper, params);
    }

    private String dateCondition(Integer sm, Integer sy, Integer em, Integer ey) {
        if (em == null || ey == null) {
            return "EXTRACT(MONTH FROM f.fecha) = :month AND EXTRACT(YEAR FROM f.fecha) = :year";
        }
        return "(EXTRACT(YEAR FROM f.fecha)*100 + EXTRACT(MONTH FROM f.fecha)) BETWEEN :startBoundary AND :endBoundary";
    }
    
    private Map<String,Object> buildParameters(
            Integer sm, Integer sy, Integer em, Integer ey,
            List<String> apps, List<String> orgs) {
        Map<String,Object> p = new HashMap<>();
        if (em == null || ey == null) {
            p.put("month", sm);
            p.put("year", sy);
        } else {
            p.put("startBoundary", sy * 100 + sm);
            p.put("endBoundary",   ey * 100 + em);
        }
        if (apps != null && !apps.isEmpty()) {
            p.put("aplicaciones", apps);
        }
        if (orgs != null && !orgs.isEmpty()) {
            p.put("dir3Codes", orgs);
        }
        return p;
    }
    
    /**
     * Método auxiliar para ejecutar queries nativas utilizando parámetros nombrados.
     */
    private <T> List<T> executeQueryNamed(String sql,
            Function<Object[],T> mapper,
            Map<String,Object> params) {

        Query q = entityManager.createNativeQuery(sql);
        for (Map.Entry<String,Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<T> result = new ArrayList<T>();
        for (Object[] row : rows) {
            result.add(mapper.apply(row));
        }
        return result;
    }

    /**
     * Método que arma la cláusula opcional de filtrado por f.aplicacion o f.dir3_code.
     */
    private String buildFilterClause(List<String> apps, List<String> orgs) {
        if ((apps == null || apps.isEmpty()) && (orgs == null || orgs.isEmpty())) {
            return "";
        }
        StringBuilder f = new StringBuilder(" AND (");
        boolean primero = true;
        if (apps != null && !apps.isEmpty()) {
            f.append("f.aplicacion IN (:aplicaciones)");
            primero = false;
        }
        if (orgs != null && !orgs.isEmpty()) {
            if (!primero) f.append(" OR ");
            f.append("f.dir3_code IN (:dir3Codes)");
        }
        f.append(")");
        return f.toString();
    }

    // =================== MÉTODOS SIN RANGO (filtro por mes y año) ===================

    // --- getSignaturesByApplication ---
    @Override
    public List<SignatureDTO> getSignaturesByApplication(final Integer month, final Integer year) {
        return getSignaturesByApplication(month, year, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByApplication(final Integer month, final Integer year,
         final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.aplicacion",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.dir3_code"
        };
    	
        String[] groupByCols = { "f.aplicacion", "f.dir3_code" };

        List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
                String name         = row[0] != null ? (String)row[0] : "Indeterminado";
                int    corrects     = ((BigDecimal)row[1]).intValue();
                int    incorrects   = ((BigDecimal)row[2]).intValue();
                String application  = name;
                String dir3 = row[3] != null ? (String)row[3] : "Indeterminado";
                return new SignatureDTO(
                    name, corrects, 
                    incorrects,
                    corrects + incorrects,
                    application, 
                    dir3
                );
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // --- getSignaturesByProvider ---
    @Override
    public List<SignatureDTO> getSignaturesByProvider(final Integer month, final Integer year) {
        return getSignaturesByProvider(month, year, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByProvider(final Integer month, final Integer year,
    		final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.proveedor",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };
    	
        String[] groupByCols = { "f.proveedor", "f.aplicacion", "f.dir3_code" };

        List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
            	if (row[0] != null) {
            		String name         = row[0] != null ? (String)row[0] : "Indeterminado";
                    int    corrects     = ((BigDecimal)row[1]).intValue();
                    int    incorrects   = ((BigDecimal)row[2]).intValue();
                    String application  = row[3] != null ? (String)row[3] : "Indeterminado";
                    String organization = row[4] != null ? (String)row[4] : "Indeterminado";
                    return new SignatureDTO(
                        name, 
                        corrects, 
                        incorrects,
                        corrects + incorrects,
                        application, 
                        organization
                    );
            	} else {
            		return null;
            	}
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // --- getSignaturesByFormat ---
    @Override
    public List<SignatureDTO> getSignaturesByFormat(final Integer month, final Integer year) {
        return getSignaturesByFormat(month, year, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByFormat(final Integer month, final Integer year,
         final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.formato",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };
    	
        String[] groupByCols = { "f.formato", "f.aplicacion", "f.dir3_code" };

        List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
                String name         = row[0] != null ? (String)row[0] : "Indeterminado";
                int    corrects     = ((BigDecimal)row[1]).intValue();
                int    incorrects   = ((BigDecimal)row[2]).intValue();
                String application  = row[3] != null ? (String)row[3] : "Indeterminado";
                String dir3 = row[4] != null ? (String)row[4] : "Indeterminado";
                return new SignatureDTO(
                    name, 
                    corrects, 
                    incorrects,
                    corrects + incorrects,
                    application, 
                    dir3
                );
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // --- getSignaturesByImprovedFormat ---
    @Override
    public List<SignatureDTO> getSignaturesByImprovedFormat(final Integer month, final Integer year) {
        return getSignaturesByImprovedFormat(month, year, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByImprovedFormat(final Integer month, final Integer year,
    		final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.formato_mejorado",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };
        String[] groupByCols = { "f.formato_mejorado", "f.aplicacion", "f.dir3_code" };

        List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
            	if (row[0] != null) {
            		String name         = row[0] != null ? (String)row[0] : "Indeterminado";
                    int    corrects     = ((BigDecimal)row[1]).intValue();
                    int    incorrects   = ((BigDecimal)row[2]).intValue();
                    String application  = row[3] != null ? (String)row[3] : "Indeterminado";
                    String organization = row[4] != null ? (String)row[4] : "Indeterminado";
                    return new SignatureDTO(
                        name, corrects, incorrects,
                        corrects + incorrects,
                        application, organization
                    );
            	} else {
            		return null;
            	}
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // =================== MÉTODOS CON RANGO (startMonth/startYear - endMonth/endYear) ===================

    // --- getSignaturesByApplication (rango) ---
    @Override
    public List<SignatureDTO> getSignaturesByApplication(final Integer startMonth, final Integer startYear,
                                                         final Integer endMonth, final Integer endYear) {
        return getSignaturesByApplication(startMonth, startYear, endMonth, endYear, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByApplication(final Integer startMonth, final Integer startYear,
                                                         final Integer endMonth, final Integer endYear,
                                                         final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.aplicacion",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.dir3_code"
        };
        String[] groupByCols = { "f.aplicacion", "f.dir3_code" };

        List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
                String name         = row[0] != null ? (String)row[0] : "Indeterminado";
                int    corrects     = ((BigDecimal)row[1]).intValue();
                int    incorrects   = ((BigDecimal)row[2]).intValue();
                String application  = name;
                String organization = row[3] != null ? (String)row[3] : "Indeterminado";
                return new SignatureDTO(
                    name, corrects, incorrects,
                    corrects + incorrects,
                    application, organization
                );
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // --- getSignaturesByProvider (rango) ---
    @Override
    public List<SignatureDTO> getSignaturesByProvider(final Integer startMonth, final Integer startYear,
                                                      final Integer endMonth, final Integer endYear) {
        return getSignaturesByProvider(startMonth, startYear, endMonth, endYear, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByProvider(final Integer startMonth, final Integer startYear,
                                                      final Integer endMonth, final Integer endYear,
                                                      final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.proveedor",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };
    	
        String[] groupByCols = { "f.proveedor", "f.aplicacion", "f.dir3_code" };

        List<SignatureDTO> raw = executeStatisticsQuery(
    		selectCols, groupByCols,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
            	if (row[0] != null) {
            		String name         = row[0] != null ? (String)row[0] : "Indeterminado";
                    int    corrects     = ((BigDecimal)row[1]).intValue();
                    int    incorrects   = ((BigDecimal)row[2]).intValue();
                    String application  = row[3] != null ? (String)row[3] : "Indeterminado";
                    String organization = row[4] != null ? (String)row[4] : "Indeterminado";
                    return new SignatureDTO(
                        name, corrects, incorrects,
                        corrects + incorrects,
                        application, organization
                    );
            	} else {
            		return null;
            	}
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // --- getSignaturesByFormat (rango) ---
    @Override
    public List<SignatureDTO> getSignaturesByFormat(final Integer startMonth, final Integer startYear,
                                                    final Integer endMonth, final Integer endYear) {
        return getSignaturesByFormat(startMonth, startYear, endMonth, endYear, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByFormat(final Integer startMonth, final Integer startYear,
                                                    final Integer endMonth, final Integer endYear,
                                                    final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.formato",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        	};
    	
        String[] groupByCols = { 
    		"f.formato", 
    		"f.aplicacion", 
    		"f.dir3_code" 
    		};

        List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
                String name         = row[0] != null ? (String)row[0] : "Indeterminado";
                int    corrects     = ((BigDecimal)row[1]).intValue();
                int    incorrects   = ((BigDecimal)row[2]).intValue();
                String application  = row[3] != null ? (String)row[3] : "Indeterminado";
                String dir3 = row[4] != null ? (String)row[4] : "Indeterminado";
                return new SignatureDTO(
                    name, 
                    corrects, 
                    incorrects,
                    corrects + incorrects,
                    application, 
                    dir3
                );
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // --- getSignaturesByImprovedFormat (rango) ---
    @Override
    public List<SignatureDTO> getSignaturesByImprovedFormat(final Integer startMonth, final Integer startYear,
                                                            final Integer endMonth, final Integer endYear) {
        return getSignaturesByImprovedFormat(startMonth, startYear, endMonth, endYear, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByImprovedFormat(final Integer startMonth, final Integer startYear,
                                                            final Integer endMonth, final Integer endYear,
                                                            final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.formato_mejorado",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };
    	
        String[] groupByCols = { "f.formato_mejorado", "f.aplicacion", "f.dir3_code" };

        List<SignatureDTO> raw = executeStatisticsQuery(
                selectCols, groupByCols,
                startMonth, startYear, endMonth, endYear,
                apps, orgs,
                row -> {
                    String improved = row[0] == null ? null : ((String) row[0]).trim();
                    if (improved == null || improved.isEmpty()) {
                        return null; // skip row with no improved format
                    }

                    int corrects   = row[1] != null ? ((BigDecimal) row[1]).intValue() : 0;
                    int incorrects = row[2] != null ? ((BigDecimal) row[2]).intValue() : 0;
                    String application = row[3] != null ? (String) row[3] : "Indeterminado";
                    String dir3        = row[4] != null ? (String) row[4] : "Indeterminado";

                    return new SignatureDTO(
                        improved,
                        corrects,
                        incorrects,
                        corrects + incorrects,
                        application,
                        dir3
                    );
                }
            );

        // Filter out nulls without method references
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // --- getSignaturesByOrganism (sin rango) ---
    @Override
    public List<SignatureDTO> getSignaturesByOrganism(final Integer month, final Integer year) {
        return getSignaturesByOrganism(month, year, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByOrganism(final Integer month, final Integer year,
                                                      final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.dir3_code",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects"
        };
    	
        String[] groupByCols = { "f.dir3_code" };

        List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
                String name       = row[0] != null ? (String)row[0] : "Indeterminado";
                int    corrects   = ((BigDecimal)row[1]).intValue();
                int    incorrects = ((BigDecimal)row[2]).intValue();
                return new SignatureDTO(
                    name, 
                    corrects, 
                    incorrects,
                    corrects + incorrects,
                    null, 
                    null
                );
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<SignatureDTO> getSignaturesByOrganism(final Integer startMonth, final Integer startYear,
                                                      final Integer endMonth, final Integer endYear) {
        return getSignaturesByOrganism(startMonth, startYear, endMonth, endYear, (List<String>) null, (List<String>) null);
    }

    public List<SignatureDTO> getSignaturesByOrganism(final Integer startMonth, final Integer startYear,
                                                      final Integer endMonth, final Integer endYear,
                                                      final List<String> apps, final List<String> orgs) {
    	String[] selectCols = {
            "f.dir3_code",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects"
        };
    	
        String[] groupByCols = { "f.dir3_code" };

        List<SignatureDTO> raw =  executeStatisticsQuery(
            selectCols, groupByCols,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
                String name       = row[0] != null ? (String)row[0] : "Indeterminado";
                int    corrects   = ((BigDecimal)row[1]).intValue();
                int    incorrects = ((BigDecimal)row[2]).intValue();
                return new SignatureDTO(
                    name, corrects, incorrects,
                    corrects + incorrects,
                    null, null
                );
            }
        );
        
        List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

	@Override
	public List<String> getDifferentApplications() {
		return repository.findDistinctApplications();
	}

	@Override
	public List<OrganizationDTO> getDifferentOrganizations() {
		return repository.findOrganizations();
	}
}
