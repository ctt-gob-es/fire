package es.gob.fire.server.services.statistics;

import es.gob.fire.signature.ConfigManager;

/**
 * Configuraci&oacute;n necesaria para al generaci&oacute;n y gesti&oacute;n de
 * los datos estad&iacute;sticos.
 */
public class StatisticsConfig {

	private Policy policy = Policy.DISABLE;
	private String dataDirPath = null;
	private String dumpTime = null;

	/**
	 * Carga la configuraci&oacute;n para la generaci&oacute;n de los datos estad&iacute;sticos.
	 * @return Configuraci&oacute;n para la generaci&oacute;n de los datos estad&iacute;sticos.
	 * @throws IllegalArgumentException Cuando se indica una politica de firma no valida.
	 */
	public static StatisticsConfig load() throws IllegalArgumentException {

		final int policyId = ConfigManager.getStatisticsPolicy();
		final Policy policy = Policy.valueOf(policyId);
		if (policy == null) {
			throw new IllegalArgumentException("No se ha proporcionado una politica valida para la generacion de estadisticas"); //$NON-NLS-1$
		}

		final StatisticsConfig config = new StatisticsConfig();
		config.setPolicy(policy);
		config.setDataDirPath(ConfigManager.getStatisticsDir());
		config.setDumpTime(ConfigManager.getStatisticsDumpTime());

		return config;
	}

	private void setPolicy(final Policy policy) {
		this.policy = policy;
	}

	private void setDataDirPath(final String dataDirPath) {
		this.dataDirPath = dataDirPath;
	}

	private void setDumpTime(final String dumpTime) {
		this.dumpTime = dumpTime;
	}

	public Policy getPolicy() {
		return this.policy;
	}

	/**
	 * Comprueba que la pol&iacute;tica establecida permita la generaci&oacute;n de datos
	 * estad&iacute;sticos.
	 * @return {@code true} si la generaci&oacute;n no est&aacute; desactivada.
	 * {@code false} si s&iacute; se deben generar los datos.
	 */
	public boolean isEnabled() {
		return this.policy != Policy.DISABLE;
	}

	public String getDataDirPath() {
		return this.dataDirPath;
	}

	public String getDumpTime() {
		return this.dumpTime;
	}

	/**
	 * Pol&iacute;ticas de generaci&oacute;n de datos estad&iacute;sticos.
	 */
	enum Policy {
		/** No se generan datos estad&iacute;sticos, */
		DISABLE (0),
		/** S&iacute; se generan datos estad&iacute;sticos. */
		GENERATE (1),
		/** Se generan datos estad&iacute;sticos y se vuelcan autom&aacute;ticamente a base de datos. */
		AUTOMATIC (2);

		int id;

		private Policy(final int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}

		public static Policy valueOf(final int policyId) {
			for (final Policy policy : values()) {
				if (policy.getId() == policyId) {
					return policy;
				}
			}
			return null;
		}
	}
}
