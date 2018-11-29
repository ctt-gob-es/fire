package es.gob.log.consumer;

/**
 * Analizador de una part&iacute;cula de texto que puede abarcar varias l&iacute;neas.
 * Esta interfaz se usa &uacute;nicamente para identificar a los analizadores que
 * pueden procesar mas de una l&iacute;nea. Esto es necesario para saber como actuar
 * al leer los registros del log.
 */
public abstract class ParticleParserUndefined extends ParticleParser {

	/**
	 * Establece cual es el analizador que identifica cual es la part&iacute;cula
	 * que debemos buscar para identificar que una l&iacute;nea se corresponde
	 * con el inicio de un nuevo registro.
	 * @param pParser Analizador de part&iacute;cula.
	 */
	abstract void setInitialParser(ParticleParser pParser);
}
