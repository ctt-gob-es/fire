package es.gob.log.consumer;

/**
 * Part&iacute;culas que determinan distintos datos en
 * un patr&oacute;n.
 */
public enum ParticlePattern {

	/** Fecha */
	DATE("[DATE]"), //$NON-NLS-1$
	/** Nivel de log */
	LEVEL("[LEVEL]"), //$NON-NLS-1$
	/** Salto de l&iacute;nea */
	RETURN_CARRIAGE("\n"), //$NON-NLS-1$
	/** Cualquier cadena */
	UNDEFINED_STRING("*"), //$NON-NLS-1$
	/** Cadena concreta */
	STRING(null);

	private String particle;

	private ParticlePattern(final String particle) {
		this.particle = particle;
	}

	/**
	 * Obtiene la particula concreta que define a un elemento dentro
	 * de un patr&oacute;n.
	 * @return Part&iacute;cula o {@code null} si no se
	 */
	public String getParticle() {
		return this.particle;
	}

	/**
	 * Obtiene la longitud de la part&iacute;cula.
	 * @return Longitud de la particula o, si no hay particula definida el entero
	 * m&aacute;ximo - 1, para simbolizar que esa partiocula puede tener cualquier longitud.
	 */
	public int getPatternLength() {
		if (this.particle == null) {
			return Integer.MAX_VALUE - 1;
		}
		return this.particle.length();
	}

	/**
	 * Identifica si de la cadena proporcionada se puede extraer
	 * la particula.
	 * @param text Texto en el que buscar la part&iacute;cula.
	 * @return Indice en el que se encuentra
	 */
	public int indexOf(final String text) {

		if (text == null || text.length() == 0) {
			return -1;
		}
		// Si no existe particula concreta (como para el patron de cadena),
		// se da como que todo el texto es la particula
		if (this.particle == null) {
			return 0;
		}
		return text.indexOf(this.particle);
	}
}
