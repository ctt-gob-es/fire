package es.gob.log.consumer;

import java.io.IOException;

/**
 * Analizador de una particula de texto dentro de un patr&oacute;n.
 */
public abstract class ParticleParser {

	String nextLimit = null;

	/**
	 * Analiza la linea indicada de un registro de log para comprobar que se
	 * ajusta con esta part&iacute;cula concreta.
	 * @param reader Objeto para la lectura de logs.
	 * @param registry Registro en el que debe almacenarse la informaci&oacute;n
	 * de la part&iacute;cula si aplica. Si no se indica, no se almacenara nada.
	 * @throws IOException Cuando ocurre un error durante el an&aacute;lisis.
	 * @throws InvalidRegistryFormatException Cuando no se encuentra la part&iacute;cula
	 * en el texto indicado.
	 */
	abstract void parse(LogReader reader, LogRegistry registry)
			throws IOException, InvalidRegistryFormatException;

	/**
	 * Establece el limitador de fin de particula, que vendra determinado
	 * por el limite de la siguiente particula
	 * @param limit Delimitador del fin de la part&iacute;cula.
	 */
	void setNextLimit(final String limit) {
		this.nextLimit = limit;
	}

	/**
	 * Indica el valor que debe de ejercer de l&iacute;mite frente a una
	 * part&iacute;cula anterior. Si no se puede determinar o no hay limite,
	 * se devuelve {@code null}.
	 * @return Valor l&iacute;mite o {@code null} si no hay.
	 */
	abstract String getLimit();
}
