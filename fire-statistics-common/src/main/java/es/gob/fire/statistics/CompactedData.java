package es.gob.fire.statistics;

import java.util.HashMap;
import java.util.Map;

import es.gob.fire.statistics.entity.SignatureCube;
import es.gob.fire.statistics.entity.TransactionCube;
import es.gob.fire.statistics.entity.TransactionTotal;

/** Conjunto consistente de datos con la informaci&oacute;n de las firmas y transacciones realizadas
 * por FIRe para la obtenci&oacute;n de estad&iacute;sticas. */
public final class CompactedData {

	private final Map<SignatureCube, Long> signatureData;

	private final Map<TransactionCube, TransactionTotal> transactionData;

	private final Map<String, Long> transactionSizeData;

	/**
	 * Construye un conjunto de datos vac&iacute;o.
	 */
	public CompactedData() {

		this.signatureData = new HashMap<>();

		this.transactionSizeData = new HashMap<>();

		this.transactionData = new HashMap<>();
	}

	/**
	 * Agrega un nuevo registro del cubo de firmas al conjunto de datos.
	 * @param signatureCube Registro del cubo de firmas.
	 */
	public void addSignatureData(final SignatureCube signatureCube) {

		Long totalInstances = this.signatureData.get(signatureCube);

		totalInstances = totalInstances != null ?
				new Long(totalInstances.longValue() + 1) : new Long(1);

		this.signatureData.put(signatureCube, totalInstances);

		// Almacenamos en otro map el tamano acumulado de los datos firmados en esa transaccion
		// para despues poder completar la informacion de las transacciones con el

		Long currentTrSize = this.transactionSizeData.get(signatureCube.getIdTransaction());
		if (currentTrSize == null) {
			currentTrSize = new Long(0);
		}
		currentTrSize = new Long(currentTrSize.longValue() + signatureCube.getDataSize());

		this.transactionSizeData.put(signatureCube.getIdTransaction(), currentTrSize);
	}

	/** Agrega un nuevo registro del cubo de transacciones al conjunto de datos.
	 * @param transactionCube Registro del cubo de transacciones. */
	public void addTransactionData(final TransactionCube transactionCube) {

		if (this.signatureData.isEmpty()) {
			throw new java.lang.IllegalStateException("Se deben cargar los datos de las firmas antes que los de transaccion"); //$NON-NLS-1$
		}

		// Obtenemos el tamano de los datos de la transaccion de la informacion almacenada al
		// registrar las firmas
		Long trSize = this.transactionSizeData.get(transactionCube.getIdTransaction());
		if (trSize == null) {
			trSize = new Long(0);
		}

		// Obtenemos el acumulado de todas las transacciones con la misma configuracion.
		TransactionTotal total = this.transactionData.get(transactionCube);
		if (total == null) {
			total = new TransactionTotal(0, 0);
		}

		// Sumamos al acumulado, esta nueva transaccion y el tamano de los datos que procesa
		total.setTotal(total.getTotal() + 1);
		total.setDataSize(total.getDataSize() + trSize.longValue());

		// Agregamos/actualizamos el acumulado para esta configuracion de transaccion
		this.transactionData.put(transactionCube, total);
	}

	public Map<SignatureCube, Long> getSignatureData() {
		return this.signatureData;
	}

	public Map<TransactionCube, TransactionTotal> getTransactionData() {
		return this.transactionData;
	}
}
