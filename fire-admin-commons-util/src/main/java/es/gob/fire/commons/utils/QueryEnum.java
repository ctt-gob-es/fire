/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espana
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
 * <b>File:</b><p>es.gob.fire.core.util.QueryEnum.java.</p>
 * <b>Description:</b><p>Enum that represents the query types.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>31/07/2018.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.1, 24/05/2021.
 */
package es.gob.fire.commons.utils;

/**
 * <p>Enum that represents the query types.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.1, 24/05/2021.
 */
public enum QueryEnum {

	TRANSACTIONS_ENDED_BY_APP (1L, "Transacciones finalizadas por cada aplicacion"),
	TRANSACTIONS_ENDED_BY_PROVIDER (2L, "Transacciones finalizadas  por cada origen de certificados/proveedor"),
	TRANSACTIONS_BY_DATES_SIZE_APP(3L, "Transacciones segun el tama\u00F3no de los datos de cada aplicacion"),
	TRANSACTIONS_BY_TYPE_TRANSACTION(4L, "Transacciones realizadas segun el tipo de transaccion (simple o lote)"),
	DOCUMENTS_SIGNED_BY_APP(5L, "Documentos firmados por cada aplicacion"),
	DOCUMENTS_SIGNED_BY_PROVIDER(6L, "Documentos firmados por cada origen de certificados/proveedor"),
	DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT(7L, "Documentos firmados en cada formato de firma"),
	DOCUMENTS_USED_IN_SIGNATURE_FORMAT(8L, "Documentos firmados que utilizan cada formato de firma longevo");

	/**
	 * Attribute that represents the enumerate id.
	 */
	private final Long id;

	/**
	 * Attribute that represents the enumerate name.
	 */
	private final String name;

	/**
	 * Constructor method for the class QueryEnum.java.
	 * @param id enumerate id
	 * @param name enumerate name
	 */
	private QueryEnum(final Long id, final String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Get id.
	 * @return id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * Get name.
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

}
