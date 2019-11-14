package es.gob.fire.server.services.internal;

import es.gob.fire.upgrade.GracePeriodInfo;

/**
 * Resultado del postproceso de una firma (validaci&oacute;n o actualizaci&oacute;n).
 */
public class PostProcessResult {

	private final State state;

	private final byte[] result;

	private final GracePeriodInfo gracePeriodInfo;

	private String upgradeFormat = null;

	/**
	 * Construye el resultado del postproceso de la firma en donde se proporciona
	 * la firma obtenida.
	 * @param result Firma resultante.
	 * @param format Identificador del formato longevo.
	 */
	public PostProcessResult(final byte[] result) {
		this.result = result;
		this.gracePeriodInfo = null;
		this.state = State.OK;
	}

	/**
	 * Construye el resultado del postproceso de la firma, en donde se indica que
	 * debe esperarse un periodo de gracia para obtener la firma.
	 * @param gracePeriod Periodo de gracia.
	 */
	public PostProcessResult(final GracePeriodInfo gracePeriod) {
		this.result = null;
		this.gracePeriodInfo = gracePeriod;
		this.state = State.PENDING;
	}

	/**
	 * Recupera la firma resultante de la actualizaci&oacute;n.
	 * @return Firma electr&oacute;nica.
	 */
	public byte[] getResult() {
		return this.result;
	}

	/**
	 * Recupera la informaci&oacute;n sobre el periodo de gracia que se debe conceder antes de
	 * poder recuperar el resultado de la firma.
	 * @return Informaci&oacute;n del periodo de gracia o {@code null} si no lo hay.
	 */
	public GracePeriodInfo getGracePeriodInfo() {
		return this.gracePeriodInfo;
	}

	/**
	 * Recupera el estado de la actualizaci&oacute;n de firma, que puede haber terminado (se
	 * proporciona la firma en la respuesta) o estar pendiente (se debe esperar un periodo de
	 * gracia).
	 * @return Estado del proceso de actualizaci&oacute;n.
	 */
	public State getState() {
		return this.state;
	}

	/**
	 * Recupera el formato al que se ha actualizado la firma.
	 * @return Formato actualizado.
	 */
	public String getUpgradeFormat() {
		return this.upgradeFormat;
	}

	/**
	 * Establece el formato al que se ha actualizado la firma.
	 * @param upgradeFormat Formato actualizado.
	 */
	public void setUpgradeFormat(final String upgradeFormat) {
		this.upgradeFormat = upgradeFormat;
	}

	/**
	 * Estado del proceso de actualizaci&oacute;n.
	 */
	public enum State {
		OK,
		PENDING
	}
}
