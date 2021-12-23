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
 * <b>File:</b><p>es.gob.fire.core.dto.TransactionDTO.java.</p>
 * <b>Description:</b><p>Class that represents the transfer object and backing form for a log server.</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>14/04/2020.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 14/04/2020.
 */
package es.gob.fire.persistence.dto;

/**
 * <p>Class that represents the transfer object and backing form for a log server.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 14/04/2020.
 */
public class TransactionDTO {

	/**
	 * Attribute that represents the value of the application or provider transaction.
	 */
	private String name;

	/**
	 * Attribute that represents the value of the correct transactions.
	 */
	private Integer corrects;

	/**
	 * Attribute that represents the value of the incorrect transactions.
	 */
	private Integer incorrects;

	/**
	 * Attribute that represents the value of the total transactions.
	 */
	private Integer total;

	/**
	 * Attribute that represents the value of the size bytes transactions.
	 */
	private double sizeBytes;

	/**
	 * Attribute that represents the value of the correct simple signatures transactions.
	 */
	private Integer correctSimpleSignatures;

	/**
	 * Attribute that represents the value of the incorrect simple signatures transactions.
	 */
	private Integer incorrectSimpleSignatures;

	/**
	 * Attribute that represents the value of the total simple signatures transactions.
	 */
	private Integer totalSimple;

	/**
	 * Attribute that represents the value of the correct batch signatures transactions.
	 */
	private Integer correctBatchSignatures;

	/**
	 * Attribute that represents the value of the incorrect batch signatures transactions.
	 */
	private Integer incorrectBatchSignatures;

	/**
	 * Attribute that represents the value of the total batch signatures transactions.
	 */
	private Integer totalBatch;

	public TransactionDTO(final String name, final double sizeBytes) {
		this.name = name;
		this.sizeBytes = sizeBytes;
	}

	public TransactionDTO(final String name, final Integer corrects, final Integer incorrects, final Integer total) {
		this.name = name;
		this.corrects = corrects;
		this.incorrects = incorrects;
		this.total = total;
	}

	public TransactionDTO(final String name, final Integer correctSimpleSignatures, final Integer incorrectSimpleSignatures, final Integer totalSimple,
			final Integer correctBatchSignatures, final Integer incorrectBatchSignatures, final Integer totalBatch) {
		this.name = name;
		this.correctSimpleSignatures = correctSimpleSignatures;
		this.incorrectSimpleSignatures = incorrectSimpleSignatures;
		this.totalSimple = totalSimple;
		this.correctBatchSignatures = correctBatchSignatures;
		this.incorrectBatchSignatures = incorrectBatchSignatures;
		this.totalBatch = totalBatch;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String nameP) {
		this.name = nameP;
	}

	public Integer getCorrects() {
		return this.corrects;
	}

	public void setCorrects(final Integer correctsP) {
		this.corrects = correctsP;
	}

	public Integer getIncorrects() {
		return this.incorrects;
	}

	public void setIncorrects(final Integer incorrectsP) {
		this.incorrects = incorrectsP;
	}

	public Integer getTotal() {
		return this.total;
	}

	public void setTotal(final Integer totalP) {
		this.total = totalP;
	}

	public double getSizeBytes() {
		return this.sizeBytes;
	}

	public void setSizeBytes(final double sizeBytesP) {
		this.sizeBytes = sizeBytesP;
	}

	public Integer getCorrectSimpleSignatures() {
		return this.correctSimpleSignatures;
	}

	public void setCorrectSimpleSignatures(final Integer correctSimpleSignaturesP) {
		this.correctSimpleSignatures = correctSimpleSignaturesP;
	}

	public Integer getIncorrectSimpleSignatures() {
		return this.incorrectSimpleSignatures;
	}

	public void setIncorrectSimpleSignatures(final Integer incorrectSimpleSignaturesP) {
		this.incorrectSimpleSignatures = incorrectSimpleSignaturesP;
	}

	public Integer getTotalSimple() {
		return this.totalSimple;
	}

	public void setTotalSimple(final Integer totalSimpleP) {
		this.totalSimple = totalSimpleP;
	}

	public Integer getCorrectBatchSignatures() {
		return this.correctBatchSignatures;
	}

	public void setCorrectBatchSignatures(final Integer correctBatchSignaturesP) {
		this.correctBatchSignatures = correctBatchSignaturesP;
	}

	public Integer getIncorrectBatchSignatures() {
		return this.incorrectBatchSignatures;
	}

	public void setIncorrectBatchSignatures(final Integer incorrectBatchSignaturesP) {
		this.incorrectBatchSignatures = incorrectBatchSignaturesP;
	}

	public Integer getTotalBatch() {
		return this.totalBatch;
	}

	public void setTotalBatch(final Integer totalBatchP) {
		this.totalBatch = totalBatchP;
	}


}
