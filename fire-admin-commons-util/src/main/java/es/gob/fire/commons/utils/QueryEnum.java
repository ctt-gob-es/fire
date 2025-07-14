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
 * @version 1.2, 30/05/2025.
 */
package es.gob.fire.commons.utils;

/**
 * <p>Enum que representa los tipos de consultas.</p>
 * <b>Proyecto:</b><p>Aplicación para la firma de documentos de la suite @firma.</p>
 * @version 1.1, 24/05/2021.
 */
public enum QueryEnum {

	TRANSACTIONS_ENDED_BY_APP(1L, "Transacciones finalizadas por cada aplicación"),
	TRANSACTIONS_ENDED_BY_PROVIDER(2L, "Transacciones finalizadas por cada origen de certificados/proveedor"),
	TRANSACTIONS_BY_DATES_SIZE_APP(3L, "Transacciones según el tamaño de los datos de cada aplicación"),
	TRANSACTIONS_BY_TYPE_TRANSACTION(4L, "Transacciones realizadas según el tipo de transacción (simple o lote)"),
	TRANSACTIONS_ENDED_BY_ORGANISM(5L, "Transacciones finalizadas por cada organismo"),
	DOCUMENTS_SIGNED_BY_APP(6L, "Documentos firmados por cada aplicación"),
	DOCUMENTS_SIGNED_BY_PROVIDER(7L, "Documentos firmados por cada origen de certificados/proveedor"),
	DOCUMENTS_SIGNED_BY_SIGNATURE_FORMAT(8L, "Documentos firmados en cada formato de firma"),
	DOCUMENTS_USED_IN_SIGNATURE_FORMAT(9L, "Documentos firmados que utilizan cada formato de firma longevo"),
	DOCUMENTS_SIGNED_BY_ORGANISM(10L, "Documentos firmados por cada organismo");

	private final Long id;
	private final String name;

	private QueryEnum(final Long id, final String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
}