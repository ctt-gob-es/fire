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
 * <b>File:</b><p>es.gob.fire.persistence.service.impl.TransactionService.java.</p>
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
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.dto.OrganizationDTO;
import es.gob.fire.persistence.dto.TransactionDTO;
import es.gob.fire.persistence.entity.Transaction;
import es.gob.fire.persistence.repository.TransactionRepository;
import es.gob.fire.persistence.repository.datatable.TransactionDataTablesRepository;
import es.gob.fire.persistence.service.ITransactionService;

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
public class TransactionService implements ITransactionService {

	@Autowired
	private TransactionRepository repository;

	@Autowired
	private TransactionDataTablesRepository dtRepository;

	@Autowired
	private EntityManager entityManager;

	@Override
	public Transaction getTransactionByTransactionId(final Long transactionId) {
		return this.repository.findByTransactionId(transactionId);
	}

	@Override
	public Transaction saveTransaction(final Transaction transaction) {
		return this.repository.save(transaction);
	}

	@Override
	public void deleteTransactionById(final Long transactionId) {
		this.repository.deleteById(transactionId);
	}

	@Override
	public Iterable<Transaction> getAllTransaction() {
		return this.repository.findAll();
	}

	@Override
	public DataTablesOutput<Transaction> getAllTransaction(final DataTablesInput input) {
		return this.dtRepository.findAll(input);
	}

	// --------------------- MÉTODOS COMUNES Y GENÉRICOS ---------------------

	/**
	 * Ejecuta cualquier consulta estadística construida dinámicamente.
	 *
	 * @param selectColumns columnas de SELECT
	 * @param groupByColumns columnas de GROUP BY
	 * @param startMonth  mes inicio o mes único
	 * @param startYear año inicio o año único
	 * @param endMonth mes fin (null si no hay rango)
	 * @param endYear año fin (null si no hay rango)
	 * @param apps filtro de aplicaciones
	 * @param orgs filtro de organizaciones
	 * @param mapper función que mapea cada fila a DTO
	 */
	private <T> List<T> executeStatisticsQuery(final String[] selectColumns, final String[] groupByColumns, final Integer startMonth,
			final Integer startYear, final Integer endMonth, final Integer endYear, final List<String> apps, final List<String> orgs,
			final Function<Object[], T> mapper) {

		final String sql = new StringBuilder()
				.append("SELECT ").append(String.join(", ", selectColumns))
				.append(" FROM tb_transacciones t")
				.append(" WHERE ").append(dateCondition(startMonth, startYear, endMonth, endYear))
				.append(buildFilterClause(apps, orgs))
				.append(" GROUP BY ").append(String.join(", ", groupByColumns))
				.toString();

		final Map<String, Object> params = buildParameters(startMonth, startYear, endMonth, endYear, apps, orgs);
		return executeQueryNamed(sql, mapper, params);
	}

	/**
	 * Condición de fecha: mes/año o rango YYYYMM entre startBoundary y
	 * endBoundary.
	 */
	private String dateCondition(final Integer sm, final Integer sy, final Integer em, final Integer ey) {
		if (em == null || ey == null) {
			return "EXTRACT(MONTH FROM t.fecha) = :month AND EXTRACT(YEAR FROM t.fecha) = :year";
		}
		return "(EXTRACT(YEAR FROM t.fecha) * 100 + EXTRACT(MONTH FROM t.fecha)) "
				+ "BETWEEN :startBoundary AND :endBoundary";
	}

	/**
	 * Construye el mapa de parámetros para la consulta.
	 */
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

		// Agregar parámetro "organizations" solo si hay valores distintos de "__UNDEFINED__"
		if (orgs != null && !orgs.isEmpty()) {
			final boolean containsOnlyUndefined = orgs.stream().allMatch("__UNDEFINED__"::equals);
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
	 * Método auxiliar para ejecutar queries nativas usando parámetros
	 * nombrados.
	 */
	private <T> List<T> executeQueryNamed(final String queryString, final java.util.function.Function<Object[], T> mapper,
			final Map<String, Object> params) {
		final Query query = this.entityManager.createNativeQuery(queryString);
		for (final Map.Entry<String, Object> entry : params.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		@SuppressWarnings("unchecked")
		final
		List<Object[]> results = query.getResultList();
		return results.stream().map(mapper).collect(Collectors.toList());
	}

	/**
	 * Método que construye la parte opcional de filtrado por t.aplicacion y
	 * t.organization.
	 */
	private String buildFilterClause(final List<String> applications, final List<String> organizations) {
		final StringBuilder filter = new StringBuilder();

		if (applications != null && !applications.isEmpty() || organizations != null && !organizations.isEmpty()) {
			filter.append(" AND (");
			boolean added = false;

			if (applications != null && !applications.isEmpty()) {
				filter.append("t.aplicacion IN (:applications)");
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
					filter.append("(t.dir3_code IN (:organizations) OR t.dir3_code IS NULL OR TRIM(t.dir3_code) = '')");
				} else if (!definedOrganizations.isEmpty()) {
					filter.append("t.dir3_code IN (:organizations)");
				} else if (includeUndefined) {
					filter.append("t.dir3_code IS NULL OR TRIM(t.dir3_code) = ''");
				}
			}

			filter.append(") ");
		}

		return filter.toString();
	}

	// ----- Métodos SIN rango de fechas (filtro por mes y año) -----
	@Override
	public List<TransactionDTO> getTransactionsByApplication(final Integer month, final Integer year) {
		return getTransactionsByApplication(month, year, (List<String>) null, (List<String>) null);
	}

	// Versión con filtros opcionales por t.aplicacion y t.dir3_code
	@Override
	public List<TransactionDTO> getTransactionsByApplication(final Integer month, final Integer year, final List<String> apps,
			final List<String> orgs) {
		final String[] selectColumns = {
				"t.aplicacion",
		        "SUM(CASE WHEN t.correcta = 1 THEN t.total ELSE 0 END) AS corrects",
				"SUM(CASE WHEN t.correcta = 0 THEN 1 ELSE 0 END) AS incorrects",
				"t.dir3_code"
				};

		final String[] groupByColumns = {
				"t.aplicacion",
				"t.dir3_code"
				};

		final List<TransactionDTO> raw = executeStatisticsQuery(
				selectColumns, groupByColumns,
				month, year, null, null,
				apps, orgs,
				row -> {
					final String app = row[0] != null ? (String) row[0] : "No definido";
					final int corr = ((BigDecimal) row[1]).intValue();
					final int inc = ((BigDecimal) row[2]).intValue();
					final String dir3Code = row[3] != null ? (String) row[3] : "No definido";
			return new TransactionDTO(app, corr, inc, corr + inc, app, dir3Code);
		});

		final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	@Override
	public List<TransactionDTO> getTransactionsByProvider(final Integer month, final Integer year) {
		return getTransactionsByProvider(month, year, (List<String>) null, (List<String>) null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByProvider(final Integer month, final Integer year,
			final List<String> apps, final List<String> orgs) {
		final String[] selectColumns = {
	        "t.proveedor",
	        "SUM(CASE WHEN t.correcta = 1 THEN t.total ELSE 0 END) AS corrects",
	        "SUM(CASE WHEN t.correcta = 0 THEN 1 ELSE 0 END) AS incorrects",
	        "t.aplicacion",
	        "t.dir3_code"
	    };
    	final String[] groupByColumns = {
	        "t.proveedor",
	        "t.aplicacion",
	        "t.dir3_code"
	    };

		final List<TransactionDTO> raw = executeStatisticsQuery(
			selectColumns, groupByColumns,
			month, year, null, null,
			apps, orgs,
			row -> {
				if (row[0] != null && !((String) row[0]).equalsIgnoreCase("indefinido")) {
					final String provider   = row[0] != null ? (String)row[0] : "No definido";
		            final int corrects      = ((BigDecimal)row[1]).intValue();
		            final int incorrects    = ((BigDecimal)row[2]).intValue();
		            final String application= row[3] != null ? (String)row[3] : "No definido";
		            final String organization = row[4] != null ? (String)row[4] : "No definido";
		            return new TransactionDTO(
		                provider,
		                corrects,
		                incorrects,
		                corrects + incorrects,
		                application,
		                organization
		            );
				}
				return null;
		});

		final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	@Override
	public List<TransactionDTO> getTransactionsByDatesSizeApp(final Integer month, final Integer year) {
		return getTransactionsByDatesSizeApp(month, year, (List<String>) null, (List<String>) null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByDatesSizeApp(final Integer month, final Integer year,
			final List<String> apps, final List<String> orgs) {
		final String[] selectColumns = {
	        "t.aplicacion",
	        "SUM(t.tamanno) AS sizeBytes",
	        "t.dir3_code"
	    };

	    final String[] groupByColumns = {
	        "t.aplicacion",
	        "t.dir3_code"
	    };

	    final List<TransactionDTO> raw = executeStatisticsQuery(
            selectColumns, groupByColumns,
            month, year, null, null,
            apps, orgs,
            row -> {
                final String application   = row[0] != null ? (String)row[0] : "No definido";
                final double sizeMb        = Math.floor(((BigDecimal)row[1]).intValue() / (1024 * 1024.0) * 100) / 100;
                final String organization  = row[2] != null ? (String)row[2] : "No definido";
                return new TransactionDTO(
                    application,
                    sizeMb,
                    application,
                    organization
                );
            }
        );

	    final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	@Override
	public List<TransactionDTO> getTransactionsByOperation(final Integer month, final Integer year) {
		return getTransactionsByOperation(month, year, (List<String>) null, (List<String>) null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByOperation(final Integer month, final Integer year,
			final List<String> apps, final List<String> orgs) {
		final String[] selectColumns = {
	        "t.aplicacion",
	        "SUM(CASE WHEN t.operacion = 'SIGN' AND t.correcta = 1 THEN t.total ELSE 0 END) AS correctSimpleSignatures",
	        "SUM(CASE WHEN t.operacion = 'SIGN' AND t.correcta = 0 THEN t.total ELSE 0 END) AS incorrectSimpleSignatures",
	        "SUM(CASE WHEN t.operacion = 'SIGN' THEN t.total ELSE 0 END) AS totalSimple",
	        "SUM(CASE WHEN t.operacion = 'BATCH' AND t.correcta = 1 THEN t.total ELSE 0 END) AS correctBatchSignatures",
	        "SUM(CASE WHEN t.operacion = 'BATCH' AND t.correcta = 0 THEN t.total ELSE 0 END) AS incorrectBatchSignatures",
	        "SUM(CASE WHEN t.operacion = 'BATCH' THEN t.total ELSE 0 END) AS totalBatch",
	        "t.dir3_code"
	    };

	    final String[] groupByColumns = {
	        "t.aplicacion",
	        "t.dir3_code"
	    };

	    final List<TransactionDTO> raw = executeStatisticsQuery(
            selectColumns, groupByColumns,
            month, year, null, null,
            apps, orgs,
            row -> {
                final String aplicacion               = row[0] != null ? (String) row[0] : "No definido";
                final Integer correctSimpleSignatures = ((BigDecimal) row[1]).intValue();
                final Integer incorrectSimpleSignatures = ((BigDecimal) row[2]).intValue();
                final Integer totalSimple             = ((BigDecimal) row[3]).intValue();
                final Integer correctBatchSignatures  = ((BigDecimal) row[4]).intValue();
                final Integer incorrectBatchSignatures = ((BigDecimal) row[5]).intValue();
                final Integer totalBatch              = ((BigDecimal) row[6]).intValue();
                final String organizacion             = row[7] != null ? (String) row[7] : "No definido";

                return new TransactionDTO(
                    aplicacion,
                    correctSimpleSignatures,
                    incorrectSimpleSignatures,
                    totalSimple,
                    correctBatchSignatures,
                    incorrectBatchSignatures,
                    totalBatch,
                    aplicacion,
                    organizacion
                );
            }
        );

	    final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	// ----- Métodos CON rango de fechas (startMonth/startYear - endMonth/endYear) -----

	// Para las queries con rangos se crea un boundary entero (Año * 100 + Mes)

	@Override
	public List<TransactionDTO> getTransactionsByApplication(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear) {
		return getTransactionsByApplication(startMonth, startYear, endMonth, endYear, null, null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByApplication(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear, final List<String> apps,
			final List<String> orgs) {
		final String[] selectColumns = {
	        "t.aplicacion",
	        "SUM(CASE WHEN t.correcta = 1 THEN t.total ELSE 0 END) AS corrects",
	        "SUM(CASE WHEN t.correcta = 0 THEN 1 ELSE 0 END) AS incorrects",
	        "t.dir3_code"
	    };

	    final String[] groupByColumns = {
	        "t.aplicacion",
	        "t.dir3_code"
	    };

	    final List<TransactionDTO> raw = executeStatisticsQuery(
            selectColumns, groupByColumns,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
                final String application    = row[0] != null ? (String) row[0] : "No definido";
                final int corrects          = ((BigDecimal) row[1]).intValue();
                final int incorrects        = ((BigDecimal) row[2]).intValue();
                final String organization   = row[3] != null ? (String) row[3] : "No definido";
                return new TransactionDTO(
                    application,
                    corrects,
                    incorrects,
                    corrects + incorrects,
                    application,
                    organization
                );
            }
        );

	    final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	@Override
	public List<TransactionDTO> getTransactionsByProvider(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear) {
		return getTransactionsByProvider(startMonth, startYear, endMonth, endYear, null, null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByProvider(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear, final List<String> apps,
			final List<String> orgs) {
		final String[] selectColumns = {
	        "t.proveedor",
	        "SUM(CASE WHEN t.correcta = 1 THEN t.total ELSE 0 END) AS corrects",
	        "SUM(CASE WHEN t.correcta = 0 THEN 1 ELSE 0 END) AS incorrects",
	        "t.aplicacion",
	        "t.dir3_code"
	    };

	    final String[] groupByColumns = {
	        "t.proveedor",
	        "t.aplicacion",
	        "t.dir3_code"
	    };

	    final List<TransactionDTO> raw = executeStatisticsQuery(
            selectColumns, groupByColumns,
            startMonth, startYear, endMonth, endYear,
            apps, orgs,
            row -> {
            	if (row[0] != null && !((String) row[0]).equalsIgnoreCase("indefinido")) {
            		final String proveedor    = row[0] != null ? (String) row[0] : "No definido";
                    final int corrects        = ((BigDecimal) row[1]).intValue();
                    final int incorrects      = ((BigDecimal) row[2]).intValue();
                    final String aplicacion   = row[3] != null ? (String) row[3] : "No definido";
                    final String dir3 		= row[4] != null ? (String) row[4] : "No definido";
                    return new TransactionDTO(
                        proveedor,
                        corrects,
                        incorrects,
                        corrects + incorrects,
                        aplicacion,
                        dir3
                    );
            	}
				return null;
            }
        );

	    final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	@Override
	public List<TransactionDTO> getTransactionsByDatesSizeApp(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear) {
		return getTransactionsByDatesSizeApp(startMonth, startYear, endMonth, endYear, null, null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByDatesSizeApp(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear, final List<String> apps,
			final List<String> orgs) {
		final String[] selectColumns = {
	        "t.aplicacion",
	        "SUM(t.tamanno) AS sizeBytes",
	        "t.dir3_code"
	    };

	    final String[] groupByColumns = {
	        "t.aplicacion",
	        "t.dir3_code"
	    };

	    final List<TransactionDTO> raw = executeStatisticsQuery(
	        selectColumns, groupByColumns,
	        startMonth, startYear, endMonth, endYear,
	        apps, orgs,
	        row -> {
	            final String aplicacion   = row[0] != null ? (String) row[0] : "No definido";
	            final double sizeMb       = Math.floor(((BigDecimal) row[1]).intValue() / (1024 * 1024.0) * 100) / 100;
	            final String dir3 		= row[2] != null ? (String) row[2] : "No definido";
	            return new TransactionDTO(
	                aplicacion,
	                sizeMb,
	                aplicacion,
	                dir3
	            );
	        }
	    );

	    final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	@Override
	public List<TransactionDTO> getTransactionsByOperation(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear) {
		return getTransactionsByOperation(startMonth, startYear, endMonth, endYear, null, null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByOperation(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear, final List<String> apps,
			final List<String> orgs) {
		final String[] selectColumns = {
	        "t.aplicacion",
	        "SUM(CASE WHEN t.operacion = 'SIGN' AND t.correcta = 1 THEN t.total ELSE 0 END) AS correctSimpleSignatures",
	        "SUM(CASE WHEN t.operacion = 'SIGN' AND t.correcta = 0 THEN t.total ELSE 0 END) AS incorrectSimpleSignatures",
	        "SUM(CASE WHEN t.operacion = 'SIGN' THEN t.total ELSE 0 END) AS totalSimple",
	        "SUM(CASE WHEN t.operacion = 'BATCH' AND t.correcta = 1 THEN t.total ELSE 0 END) AS correctBatchSignatures",
	        "SUM(CASE WHEN t.operacion = 'BATCH' AND t.correcta = 0 THEN t.total ELSE 0 END) AS incorrectBatchSignatures",
	        "SUM(CASE WHEN t.operacion = 'BATCH' THEN t.total ELSE 0 END) AS totalBatch",
	        "t.dir3_code"
	    };

	    final String[] groupByColumns = {
	        "t.aplicacion",
	        "t.dir3_code"
	    };

	    final List<TransactionDTO> raw = executeStatisticsQuery(
	        selectColumns, groupByColumns,
	        startMonth, startYear, endMonth, endYear,
	        apps, orgs,
	        row -> {
	            final String aplicacion               = row[0] != null ? (String) row[0] : "No definido";
	            final Integer correctSimpleSignatures = ((BigDecimal) row[1]).intValue();
	            final Integer incorrectSimpleSignatures = ((BigDecimal) row[2]).intValue();
	            final Integer totalSimple             = ((BigDecimal) row[3]).intValue();
	            final Integer correctBatchSignatures  = ((BigDecimal) row[4]).intValue();
	            final Integer incorrectBatchSignatures = ((BigDecimal) row[5]).intValue();
	            final Integer totalBatch              = ((BigDecimal) row[6]).intValue();
	            final String dir3             		= row[7] != null ? (String) row[7] : "No definido";

	            return new TransactionDTO(
	                aplicacion,
	                correctSimpleSignatures,
	                incorrectSimpleSignatures,
	                totalSimple,
	                correctBatchSignatures,
	                incorrectBatchSignatures,
	                totalBatch,
	                aplicacion,
	                dir3
	            );
	        }
	    );

	    final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	@Override
	public List<TransactionDTO> getTransactionsByOrganism(final Integer month, final Integer year) {
		return getTransactionsByOrganism(month, year, (List<String>) null, (List<String>) null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByOrganism(final Integer month, final Integer year,
			final List<String> apps, final List<String> orgs) {
		final String[] selectColumns = {
	        "t.dir3_code",
	        "SUM(CASE WHEN t.correcta = 1 THEN t.total ELSE 0 END) AS corrects",
	        "SUM(CASE WHEN t.correcta = 0 THEN 1 ELSE 0 END) AS incorrects"
	    };

	    final String[] groupByColumns = {
	        "t.dir3_code"
	    };

	    final List<TransactionDTO> raw = executeStatisticsQuery(
	        selectColumns, groupByColumns,
	        month, year, null, null,
	        apps, orgs,
	        row -> {
	            final String dir3         = row[0] != null ? (String) row[0] : "No definido";
	            final int corrects        = ((BigDecimal) row[1]).intValue();
	            final int incorrects      = ((BigDecimal) row[2]).intValue();
	            return new TransactionDTO(
	            	dir3,
	                corrects,
	                incorrects,
	                corrects + incorrects,
	                null,
	                dir3
	            );
	        }
	    );

	    final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
	}

	@Override
	public List<TransactionDTO> getTransactionsByOrganism(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear) {
		return getTransactionsByOrganism(startMonth, startYear, endMonth, endYear, null, null);
	}

	@Override
	public List<TransactionDTO> getTransactionsByOrganism(final Integer startMonth, final Integer startYear,
			final Integer endMonth, final Integer endYear, final List<String> apps,
			final List<String> orgs) {
		final String[] selectColumns = {
	        "t.dir3_code",
	        "SUM(CASE WHEN t.correcta = 1 THEN t.total ELSE 0 END) AS corrects",
	        "SUM(CASE WHEN t.correcta = 0 THEN 1 ELSE 0 END) AS incorrects"
	    };
	    final String[] groupByColumns = {
	        "t.dir3_code"
	    };

	    final List<TransactionDTO> raw =executeStatisticsQuery(
	        selectColumns, groupByColumns,
	        startMonth, startYear, endMonth, endYear,
	        apps, orgs,
	        row -> {
	            final String dir3       = row[0] != null ? (String) row[0] : "No definido";
	            final int corrects      = ((BigDecimal) row[1]).intValue();
	            final int incorrects    = ((BigDecimal) row[2]).intValue();
	            return new TransactionDTO(
	                dir3,
	                corrects,
	                incorrects,
	                corrects + incorrects,
	                null,
	                dir3
	            );
	        }
	    );

	    final List<TransactionDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
        	final TransactionDTO dto = raw.get(i);
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