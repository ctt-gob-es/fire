package es.gob.fire.server.services.statistics;

import es.gob.fire.signature.ConfigManager;

/**
 * Configuraci&oacute;n necesaria para al generaci&oacute;n y gesti&oacute;n de
 * los datos estad&iacute;sticos.
 */
public class AuditConfig {

	private Policy policy = Policy.DISABLE;
	private String dataDirPath = null;
	private String deleteTime = null;

	/**
	 * Carga la configuraci&oacute;n para la generaci&oacute;n de los datos estad&iacute;sticos de auditoria.
	 * @return Configuraci&oacute;n para la generaci&oacute;n de los datos estad&iacute;sticos de auditoria.
	 * @throws IllegalArgumentException Cuando se indica una politica de firma no valida.
	 */
	public static AuditConfig load() throws IllegalArgumentException {

		final int policyId = ConfigManager.getAuditPolicy();
		final Policy policy = Policy.valueOf(policyId);
		if (policy == null) {
			throw new IllegalArgumentException("No se ha proporcionado una politica valida para la generacion de estadisticas de auditoria"); //$NON-NLS-1$
		}

		final AuditConfig config = new AuditConfig();
		config.setPolicy(policy);
		config.setDataDirPath(ConfigManager.getAuditDir());
		config.setDeleteTime(ConfigManager.getAuditDeleteTime());

		return config;
	}

	private void setPolicy(final Policy policy) {
		this.policy = policy;
	}

	private void setDataDirPath(final String dataDirPath) {
		this.dataDirPath = dataDirPath;
	}

	private void setDeleteTime(final String deleteTime) {
		this.deleteTime = deleteTime;
	}

	public Policy getPolicy() {
		return this.policy;
	}

	/**
	 * Comprueba que la pol&iacute;tica establecida permita la generaci&oacute;n de datos
	 * estad&iacute;sticos de auditor&iacute;a.
	 * @return {@code true} si la generaci&oacute;n no est&aacute; desactivada.
	 * {@code false} si s&iacute; se deben generar los datos.
	 */
	public boolean isEnabled() {
		return this.policy != Policy.DISABLE;
	}

	/**
	 * Indica a nivel general si se deben guardar los registros de auditor&iacute;a.
	 * @return {@code true} si la generaci&oacute;n no est&aacute; desactivada.
	 * {@code false} si s&iacute; se deben generar los datos.
	 */
	public static boolean needRegistry() {
		final int policyId = ConfigManager.getAuditPolicy();
		final Policy policy = Policy.valueOf(policyId);
		return policy != null && policy != Policy.DISABLE;
	}

	/**
	 * Comprueba que la pol&iacute;tica establecida permita el guardado de datos
	 * estad&iacute;sticos de auditor&iacute;a en base de datos.
	 * @return {@code true} si el guardado no est&aacute; desactivada.
	 * {@code false} si s&iacute; se deben generar los datos.
	 */
	public boolean isSavingToDB() {
		return getPolicy() == Policy.AUTOMATIC;
	}

	public String getDataDirPath() {
		return this.dataDirPath;
	}

	public String getDeleteTime() {
		return this.deleteTime;
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

		Policy(final int id) {
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
