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
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
        return this.repository.findBySignatureId(signatureId);
    }

    // --------------------- METODOS COMUNES Y GENERICOS ---------------------

    private <T> List<T> executeStatisticsQuery(
            final String[] selectColumns,
            final String[] groupByColumns,
            final Integer sm, final Integer sy, final Integer em, final Integer ey,
            final List<String> apps, final List<String> orgs,
            final Function<Object[], T> mapper) {

        final String sql = new StringBuilder()
            .append("SELECT ").append(String.join(", ", selectColumns))
            .append(" FROM tb_firmas f")
            .append(" WHERE ").append(dateCondition(sm, sy, em, ey))
            .append(buildFilterClause(apps, orgs))
            .append(" GROUP BY ").append(String.join(", ", groupByColumns))
            .toString();

        final Map<String,Object> params = buildParameters(sm, sy, em, ey, apps, orgs);
        return executeQueryNamed(sql, mapper, params);
    }

    private String dateCondition(final Integer sm, final Integer sy, final Integer em, final Integer ey) {
        if (em == null || ey == null) {
            return "EXTRACT(MONTH FROM f.fecha) = :month AND EXTRACT(YEAR FROM f.fecha) = :year";
        }
        return "(EXTRACT(YEAR FROM f.fecha)*100 + EXTRACT(MONTH FROM f.fecha)) BETWEEN :startBoundary AND :endBoundary";
    }

    private Map<String, Object> buildParameters(final Integer sm, final Integer sy, final Integer em, final Integer ey, final List<String> apps, final List<String> orgs) {
		final Map<String, Object> params = new HashMap<>();

		if (em == null || ey == null) {
			params.put("month", sm);
			params.put("year", sy);
		} else {
			params.put("startBoundary", sy * 100 + sm);
			params.put("endBoundary", ey * 100 + em);
		}

		if (apps != null && !apps.isEmpty()) {
			params.put("applications", apps);
		}

		// Agregar parametro "organizations" solo si hay valores distintos de "__UNDEFINED__"
		if (orgs != null && !orgs.isEmpty()) {
			final boolean containsOnlyUndefined = orgs.stream().allMatch(o -> "__UNDEFINED__".equals(o));
			if (!containsOnlyUndefined) {
				final List<String> definedOrgs = orgs.stream()
					.filter(o -> !"__UNDEFINED__".equals(o))
					.collect(Collectors.toList());
				params.put("organizations", definedOrgs);
			}
		}

		return params;
	}

    /**
     * M&eacute;todo auxiliar para ejecutar queries nativas utilizando par&aacute;metros nombrados.
     */
    private <T> List<T> executeQueryNamed(final String sql,
            final Function<Object[],T> mapper,
            final Map<String,Object> params) {

        final Query q = this.entityManager.createNativeQuery(sql);
        for (final Map.Entry<String,Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
        @SuppressWarnings("unchecked")
		final List<Object[]> rows = q.getResultList();
        final List<T> result = new ArrayList<>();
        for (final Object[] row : rows) {
            result.add(mapper.apply(row));
        }
        return result;
    }

    /**
     * M&eacute;todo que arma la cl&aacute;usula opcional de filtrado por f.aplicacion o f.dir3_code.
     */
    private String buildFilterClause(final List<String> applications, final List<String> organizations) {
		final StringBuilder filter = new StringBuilder();

		if (applications != null && !applications.isEmpty() || organizations != null && !organizations.isEmpty()) {
			filter.append(" AND (");
			boolean added = false;

			if (applications != null && !applications.isEmpty()) {
				filter.append("f.aplicacion IN (:applications)");
				added = true;
			}

			if (organizations != null && !organizations.isEmpty()) {
				if (added) {
					filter.append(" OR ");
				}

				final boolean includeUndefined = organizations.contains("__UNDEFINED__");
				final List<String> definedOrganizations = new ArrayList<>();
				for (final String org : organizations) {
					if (!"__UNDEFINED__".equals(org)) {
						definedOrganizations.add(org);
					}
				}

				if (!definedOrganizations.isEmpty() && includeUndefined) {
					filter.append("(f.dir3_code IN (:organizations) OR f.dir3_code IS NULL OR TRIM(f.dir3_code) = '')");
				} else if (!definedOrganizations.isEmpty()) {
					filter.append("f.dir3_code IN (:organizations)");
				} else if (includeUndefined) {
					filter.append("f.dir3_code IS NULL OR TRIM(f.dir3_code) = ''");
				}
			}

			filter.append(") ");
		}

		return filter.toString();
	}

    // =================== METODOS SIN RANGO (filtro por mes y anyo) ===================

    // --- getSignaturesByApplication ---
    @Override
    public List<SignatureDTO> getSignaturesByApplication(final Integer month, final Integer year) {
        return getSignaturesByApplication(month, year, (List<String>) null, (List<String>) null);
    }

    @Override
	public List<SignatureDTO> getSignaturesByApplication(final Integer month, final Integer year,
         final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.aplicacion",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.dir3_code"
        };

        final String[] groupByCols = { "f.aplicacion", "f.dir3_code" };

        final List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
                final String name         = row[0] != null ? (String)row[0] : "No definido";
                final int    corrects     = ((BigDecimal)row[1]).intValue();
                final int    incorrects   = ((BigDecimal)row[2]).intValue();
                final String application  = name;
                final String organization = row[3] != null ? (String)row[3] : "No definido";
                return new SignatureDTO(
                    name, corrects,
                    incorrects,
                    corrects + incorrects,
                    application,
                    organization
                );
            }
        );

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
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

    @Override
	public List<SignatureDTO> getSignaturesByProvider(final Integer month, final Integer year,
    		final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.proveedor",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };

        final String[] groupByCols = { "f.proveedor", "f.aplicacion", "f.dir3_code" };

        final List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
            	if (row[0] != null && !((String) row[0]).equalsIgnoreCase("indefinido")) {
            		final String name         = row[0] != null ? (String)row[0] : "No definido";
                    final int    corrects     = ((BigDecimal)row[1]).intValue();
                    final int    incorrects   = ((BigDecimal)row[2]).intValue();
                    final String application  = row[3] != null ? (String)row[3] : "No definido";
                    final String organization = row[4] != null ? (String)row[4] : "No definido";
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

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
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

    @Override
	public List<SignatureDTO> getSignaturesByFormat(final Integer month, final Integer year,
         final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.formato",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };

        final String[] groupByCols = { "f.formato", "f.aplicacion", "f.dir3_code" };

        final List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
                final String name         = row[0] != null ? (String)row[0] : "No definido";
                final int    corrects     = ((BigDecimal)row[1]).intValue();
                final int    incorrects   = ((BigDecimal)row[2]).intValue();
                final String application  = row[3] != null ? (String)row[3] : "No definido";
                final String dir3 = row[4] != null ? (String)row[4] : "No definido";
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

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
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

    @Override
	public List<SignatureDTO> getSignaturesByImprovedFormat(final Integer month, final Integer year,
    		final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.formato_mejorado",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };
        final String[] groupByCols = { "f.formato_mejorado", "f.aplicacion", "f.dir3_code" };

        final List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
            	if (row[0] != null) {
            		final String name         = row[0] != null ? (String)row[0] : "No definido";
                    final int    corrects     = ((BigDecimal)row[1]).intValue();
                    final int    incorrects   = ((BigDecimal)row[2]).intValue();
                    final String application  = row[3] != null ? (String)row[3] : "No definido";
                    final String organization = row[4] != null ? (String)row[4] : "No definido";
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

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // =================== METODOS CON RANGO (startMonth/startYear - endMonth/endYear) ===================

    // --- getSignaturesByApplication (rango) ---
    @Override
    public List<SignatureDTO> getSignaturesByApplication(final Integer startMonth, final Integer startYear,
                                                         final Integer endMonth, final Integer endYear) {
        return getSignaturesByApplication(startMonth, startYear, endMonth, endYear, (List<String>) null, (List<String>) null);
    }

    @Override
	public List<SignatureDTO> getSignaturesByApplication(final Integer startMonth, final Integer startYear,
                                                         final Integer endMonth, final Integer endYear,
                                                         final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.aplicacion",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.dir3_code"
        };
        final String[] groupByCols = { "f.aplicacion", "f.dir3_code" };

        final List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
                final String name         = row[0] != null ? (String)row[0] : "No definido";
                final int    corrects     = ((BigDecimal)row[1]).intValue();
                final int    incorrects   = ((BigDecimal)row[2]).intValue();
                final String application  = name;
                final String organization = row[3] != null ? (String)row[3] : "No definido";
                return new SignatureDTO(
                    name, corrects, incorrects,
                    corrects + incorrects,
                    application, organization
                );
            }
        );

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
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

    @Override
	public List<SignatureDTO> getSignaturesByProvider(final Integer startMonth, final Integer startYear,
                                                      final Integer endMonth, final Integer endYear,
                                                      final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.proveedor",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };

        final String[] groupByCols = { "f.proveedor", "f.aplicacion", "f.dir3_code" };

        final List<SignatureDTO> raw = executeStatisticsQuery(
    		selectCols, groupByCols,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
            	if (row[0] != null && !((String) row[0]).equalsIgnoreCase("indefinido")) {
            		final String name         = row[0] != null ? (String)row[0] : "No definido";
                    final int    corrects     = ((BigDecimal)row[1]).intValue();
                    final int    incorrects   = ((BigDecimal)row[2]).intValue();
                    final String application  = row[3] != null ? (String)row[3] : "No definido";
                    final String organization = row[4] != null ? (String)row[4] : "No definido";
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

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
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

    @Override
	public List<SignatureDTO> getSignaturesByFormat(final Integer startMonth, final Integer startYear,
                                                    final Integer endMonth, final Integer endYear,
                                                    final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.formato",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        	};

        final String[] groupByCols = {
    		"f.formato",
    		"f.aplicacion",
    		"f.dir3_code"
    		};

        final List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
                final String name         = row[0] != null ? (String)row[0] : "No definido";
                final int    corrects     = ((BigDecimal)row[1]).intValue();
                final int    incorrects   = ((BigDecimal)row[2]).intValue();
                final String application  = row[3] != null ? (String)row[3] : "No definido";
                final String dir3 = row[4] != null ? (String)row[4] : "No definido";
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

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
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

    @Override
	public List<SignatureDTO> getSignaturesByImprovedFormat(final Integer startMonth, final Integer startYear,
                                                            final Integer endMonth, final Integer endYear,
                                                            final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.formato_mejorado",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects",
            "f.aplicacion",
            "f.dir3_code"
        };

        final String[] groupByCols = { "f.formato_mejorado", "f.aplicacion", "f.dir3_code" };

        final List<SignatureDTO> raw = executeStatisticsQuery(
                selectCols, groupByCols,
                startMonth, startYear, endMonth, endYear,
                apps, orgs,
                row -> {
                    final String improved = row[0] == null ? null : ((String) row[0]).trim();
                    if (improved == null || improved.isEmpty()) {
                        return null; // skip row with no improved format
                    }

                    final int corrects   = row[1] != null ? ((BigDecimal) row[1]).intValue() : 0;
                    final int incorrects = row[2] != null ? ((BigDecimal) row[2]).intValue() : 0;
                    final String application = row[3] != null ? (String) row[3] : "No definido";
                    final String dir3        = row[4] != null ? (String) row[4] : "No definido";

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
        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
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

    @Override
	public List<SignatureDTO> getSignaturesByOrganism(final Integer month, final Integer year,
                                                      final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.dir3_code",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects"
        };

        final String[] groupByCols = { "f.dir3_code" };

        final List<SignatureDTO> raw = executeStatisticsQuery(
            selectCols, groupByCols,
            month, year, null, null,
            apps, orgs,
            row -> {
                final String name       = row[0] != null ? (String)row[0] : "No definido";
                final int    corrects   = ((BigDecimal)row[1]).intValue();
                final int    incorrects = ((BigDecimal)row[2]).intValue();
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

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
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

    @Override
	public List<SignatureDTO> getSignaturesByOrganism(final Integer startMonth, final Integer startYear,
                                                      final Integer endMonth, final Integer endYear,
                                                      final List<String> apps, final List<String> orgs) {
    	final String[] selectCols = {
            "f.dir3_code",
            "SUM(CASE WHEN f.correcta = '1' THEN total ELSE 0 END) AS corrects",
            "SUM(CASE WHEN f.correcta = '0' THEN total ELSE 0 END) AS incorrects"
        };

        final String[] groupByCols = { "f.dir3_code" };

        final List<SignatureDTO> raw =  executeStatisticsQuery(
            selectCols, groupByCols,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
                final String name       = row[0] != null ? (String)row[0] : "No definido";
                final int    corrects   = ((BigDecimal)row[1]).intValue();
                final int    incorrects = ((BigDecimal)row[2]).intValue();
                return new SignatureDTO(
                    name, corrects, incorrects,
                    corrects + incorrects,
                    null, null
                );
            }
        );

        final List<SignatureDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            final SignatureDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

	@Override
	public List<String> getDifferentApplications() {
		return this.repository.findDistinctApplications();
	}

	@Override
	public List<OrganizationDTO> getDifferentOrganizations() {
		return this.repository.findOrganizations();
	}
}
