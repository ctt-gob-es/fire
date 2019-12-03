package es.gob.log.consumer;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Factoria para la generaci&oacute;n de analizadores para cada una de las
 * part&iacute;culas que pueden aparecer en el patr&oacute;n.
 */
public class ParticleParserFactory {

	private static final String CR_STRING = "\n"; //$NON-NLS-1$

	private String dateFormat = null;

	private String[] levels = null;

	/**
	 * Obtiene una instancia de la factor&iacute;a configurada seg&uacute;n la
	 * informaci&oacute;n proporcionada.
	 * @param logInfo Informaci&oacute;n sobre el log.
	 * @return Factor&iacute;a para la creaci&oacute;n de analizadores.
	 */
	public static ParticleParserFactory getInstance(final LogInfo logInfo) {
		return new ParticleParserFactory(logInfo);
	}

	private ParticleParserFactory(final LogInfo logInfo) {
		if (logInfo != null) {
			this.dateFormat = logInfo.getDateFormat();
			this.levels = logInfo.getLevels();
		}
	}

	ParticleParser build(final ParticlePattern pp, final String text) {

		switch (pp) {
		case DATE:
			return new DateParticleParser(this.dateFormat);
		case LEVEL:
			return new LevelParticleParser(this.levels);
		case RETURN_CARRIAGE:
			return new ReturnCarriageParticleParser();
		case UNDEFINED_STRING:
			return new UndefinedStringParser();
		case STRING:
		default:
			return new StringParticleParser(text);
		}
	}


	private class StringParticleParser extends ParticleParser {
		private final String text;
		public StringParticleParser(final String text) {
			this.text = text;
		}

		@Override
		public void parse(final LogReader reader, final LogRegistry registry)
				throws IOException, InvalidRegistryFormatException {

			final CharBuffer line = reader.getCurrentLine();

				for (final char c : this.text.toCharArray()) {
					if (c != line.get()) {
						line.rewind();
						throw new InvalidRegistryFormatException("Se encontro un texto distinto al esperado: " + line.toString()); //$NON-NLS-1$
					}
				}


		}

		@Override
		public String getLimit() {
			return this.text;
		}
	}

	private class DateParticleParser extends ParticleParser {

		private final SimpleDateFormat formatter;

		/** Texto a partir del cual se construye el parser de fechas. */
		private final String dateFormatText;

		/** Numero de veces que el delimitador de este analizador aparece dentro del texto que
		 * debemos analizar. Esto sirve para saltar esas primeras entradas que tambi&acute;n forman
		 * parte de la fecha. Se calcula al parsear el valor por primera vez ya que hasta entonces
		 * no sabemos cu&aacute;l sera el delimitador. */
		private int numTimesLimit = -1;




		public DateParticleParser(final String dateFormat) {
			this.dateFormatText = dateFormat;
			this.formatter = this.dateFormatText != null ?
					new SimpleDateFormat(this.dateFormatText) : null;
		}

		@Override
		public void parse(final LogReader reader, final LogRegistry registry)
				throws IOException, InvalidRegistryFormatException {

			if (this.dateFormatText == null) {
				throw new InvalidRegistryFormatException("No se ha indicado el formato de fecha"); //$NON-NLS-1$
			}

			final CharBuffer line = reader.getCurrentLine();

			Date date;
			String dateText;
			if (this.nextLimit == null) {
				dateText = line.toString();
				final ParsePosition pos = new ParsePosition(0);
				date = this.formatter.parse(dateText, pos);
				line.position(line.position() + pos.getIndex());
			}
			else {
				if (this.nextLimit == CR_STRING) {
					dateText = line.toString();
					try {
						date = this.formatter.parse(dateText);
					} catch (final ParseException e) {
						date = null;
					}
				}
				else {

					// La primera vez, se calcula cuantas veces el limite de la fecha esta contenido en el propio formato
					if (this.numTimesLimit == -1) {
						this.numTimesLimit = calculateNumTimesLimit();
					}

					final int idx = calculateLimitPos(line);
					if (idx == -1) {
						throw new InvalidRegistryFormatException("No se ha encontrado el limite de la fecha"); //$NON-NLS-1$
					}
					final char[] dateTextChars = new char[idx];
					line.get(dateTextChars);
					dateText = new String(dateTextChars);
					try {
						date = this.formatter.parse(dateText);
					} catch (final ParseException e) {
						date = null;
					}
				}
			}

			if (date == null) {
				throw new InvalidRegistryFormatException("Fecha no valida: " + dateText); //$NON-NLS-1$
			}

			if (registry != null) {
				registry.setCurrentMillis(date.getTime());
			}
		}

		/**
		 * Calcula el n&uacute;mero de veces que el l&iacute;mite se encuentra de forma completa y
		 * exclusiva en el formato de fecha. Por ejemplo, un espacio puede determinar el fin de la
		 * fecha, pero la propia fecha puede contener un espacio o m&aacute;s.
		 * @param dateFormat Formato de fecha.
		 * @param limit L&iacute;mite que marca el final de la fecha.
		 * @return N&uacute;mero de veces que el l&iacute;mite est&aacute; dentro del propio formato.
		 */
		private int calculateNumTimesLimit() {
			int count = 0;
			int pos = 0;
			while((pos = this.dateFormatText.indexOf(this.nextLimit, pos)) > -1) {
				count++;
				pos += this.nextLimit.length();
			}
			return count;
		}

		/**
		 * Identifica donde termina la fecha en una l&iacute;nea cuadno se conoce la cadena
		 * delimitadora.
		 * @param line L&iacute;nea en el que busca la posici&oacute;n final de la fecha.
		 * @return Posici&oacute;n de la cadena que pone fin a la fecha o {@code -1} cuando no
		 * se localiza el fin.
		 */
		private int calculateLimitPos(final CharBuffer line) {

			int pos = 0;
			int count = 0;
			final String lineText = line.toString();
			do {
				pos = lineText.indexOf(this.nextLimit, pos);
				count++;
				if (pos != -1 && count <= this.numTimesLimit) {
					pos += this.nextLimit.length();
				}

			} while (pos != -1 && count <= this.numTimesLimit);

			// Si no la hemos encontrado todas las veces que debiamos, indicamos que no se ha encontrado un limite valido
			if (count <= this.numTimesLimit) {
				pos = -1;
			}
			return pos;
		}

		@Override
		public String getLimit() {
			return null;
		}
	}

	private class LevelParticleParser extends ParticleParser {

		private final String[] pLevels;
		public LevelParticleParser(final String[] levels) {
			this.pLevels = levels;
		}

		@Override
		public void parse(final LogReader reader, final LogRegistry registry)
				throws IOException, InvalidRegistryFormatException {

			if (this.pLevels == null) {
				throw new InvalidRegistryFormatException("No se han indicado los niveles de log"); //$NON-NLS-1$
			}

			int level = -1;
			final CharBuffer line = reader.getCurrentLine();

			if (this.nextLimit != null) {
				if (this.nextLimit == CR_STRING) {
					level = getLevel(line.toString());
				}
				else {
					final int idx = line.toString().indexOf(this.nextLimit);
					if (idx == -1) {
						throw new InvalidRegistryFormatException("No se ha encontrado el limite del nivel"); //$NON-NLS-1$
					}
					final char[] levelChars = new char[idx];
					line.get(levelChars);
					level = getLevel(new String(levelChars));
				}
			}

			// Si aun no se ha encontrado, buscamos activamente cada nivel aceptado
			if (level < 0) {
				// Marcamos la posicion para poder volver
				line.mark();
				char[] levelChars;
				for (int i = 0; i < this.pLevels.length; i++) {
					// Volvemos a la marca
					line.reset();
					levelChars = new char[this.pLevels[i].length()];
					try {
						line.get(levelChars);
					}
					catch (final BufferUnderflowException e) {
						continue;
					}
					if (this.pLevels[i].equals(new String(levelChars))) {
						level = i;
						break;
					}
				}
			}

			if (level < 0) {
				throw new InvalidRegistryFormatException("No se ha encontrado el nivel"); //$NON-NLS-1$
			}

			if (registry != null) {
				registry.setLevel(level);
			}

		}

		private int getLevel(final String levelText) throws InvalidRegistryFormatException {
			for (int i = 0; i < this.pLevels.length; i++) {
				if (this.pLevels[i].equals(levelText)) {
					return i;
				}
			}
			throw new InvalidRegistryFormatException("No se ha encontrado un nivel valido"); //$NON-NLS-1$
		}

		@Override
		public String getLimit() {
			return null;
		}
	}

	private class ReturnCarriageParticleParser extends ParticleParser {

		public ReturnCarriageParticleParser() { }

		@Override
		public void parse(final LogReader reader, final LogRegistry registry) throws IOException {

			// Leemos una nueva linea a traves del lector
			final CharBuffer line = reader.readLine();

			// Introducimos la nueva linea como parte del registro de log
			if(line != null) {
				registry.appendLogLine(line.toString());
			}
		}

		@Override
		public String getLimit() {
			return CR_STRING;
		}
	}

	private class UndefinedStringParser extends ParticleParserUndefined {

		private ParticleParser initialPParser = null;

		public UndefinedStringParser() {}

		@Override
		public void parse(final LogReader reader, final LogRegistry registry)
				throws IOException, InvalidRegistryFormatException {

			CharBuffer line = reader.getCurrentLine();

			// En caso de que haya un limite, avanzamos en la linea hasta encontrarlo
			if (this.nextLimit != null) {
				find(line, this.nextLimit);
			}
			// Si no lo hay, entendemos que estamos en el ultimo elemento del registro.
			// Vamos leyendo lineas hasta encontrar una que empiece como un nuevo registro
			// y agregando todas las que no como parte del registro actual
			else {
				line = reader.readLine();
				while (line != null && !isNewRegistry(reader)) {
					line.rewind();
					registry.appendLogLine(line.toString());
					line = reader.readLine();
				}
				if (line != null) {
					line.rewind();
				}
			}
		}

		private boolean isNewRegistry(final LogReader reader) {

			// Para evitar bucles, en el caso de que la siguiente particula
			// sea esta misma (el patron es un solo asterisco), devolvemos
			// true para que cada linea sea un nuevo registro
			if (this.initialPParser == this) {
				return true;
			}

			try {
				this.initialPParser.parse(reader, null);
			} catch (final Exception e) {
				return false;
			}
			return true;
		}

		/**
		 * Encuentra y avanza hasta el l&iacute;mite establecido.
		 * @param line L&iacute;nea en la que buscar el l&iacute;mite
		 * @param limit Cadena que define el limite de esta part&iacute;cula.
		 * @throws InvalidRegistryFormatException Cuando no se encuentra la
		 * particula de fin en la l&iacute;nea.
		 */
		private void find(final CharBuffer line, final String limit)
				throws InvalidRegistryFormatException {

			// Si el limite es un salto de linea, vamos directamente al final
			if (CR_STRING.equals(limit)) {
				line.position(line.limit());
				return;
			}

			// Si no, buscamos la cadena
			char c;
			boolean found = false;
			final char[] limitChars = limit.toCharArray();
			try {
				while (!found) {
					line.mark();
					c = line.get();
					if (c == limitChars[0]) {
						found = true;
						for (int i = 0; found && i < limitChars.length; i++) {
							if (c == limitChars[i]) {
								c = line.get();
							}
							else {
								found = false;
							}
						}
						if (!found) {
							line.reset();
						}
					}
				}
				line.reset();
			}
			catch (final Exception e) {
				throw new InvalidRegistryFormatException("No se encontro el limite establecido para la cadena indefinida", e); //$NON-NLS-1$
			}
		}

		@Override
		public String getLimit() {
			return null;
		}

		@Override
		public void setInitialParser(final ParticleParser pParser) {
			this.initialPParser = pParser;
		}
	}

	/**
	 * Comprueba si el patr&oacute;n cumple una serie de reglas que determinan si es
	 * v&aacute;lido o no.
	 * @param parsers Analizadores de las distintas part&iacute;culas encontradas dentro
	 * del patr&oacute;n.
	 * @throws InvalidPatternException Cuando el patr&oacute;n no es v&aacute;lido.
	 */
	public static void checkPattern(final ParticleParser[] parsers) throws InvalidPatternException {


		for (int i = 0; i < parsers.length; i++) {

			// La ultima particula del patron tiene consideraciones especiales, ya que no tiene a
			// ningun otro que lo limite y puede extenderse multiples lineas
			if (i == parsers.length - 1) {
				if (parsers[i] instanceof ReturnCarriageParticleParser) {
					throw new InvalidPatternException("La ultima particula del patron no puede ser retorno de carro"); //$NON-NLS-1$
				}
			}
			else {
				// Dentro del resto de particulas, el primero puede tener alguna comprobacion adicional
				if (i == 0) {
					if (parsers[i] instanceof ReturnCarriageParticleParser) {
						throw new InvalidPatternException("La primera particula del patron no puede ser retorno de carro"); //$NON-NLS-1$
					}
					else if (parsers[i] instanceof UndefinedStringParser) {
						if (parsers[i].nextLimit == null || parsers[i].nextLimit.equals(CR_STRING)) {
							throw new InvalidPatternException("La primera particula del patron no puede ser una cadena indefinida a la que no siga un delimitador"); //$NON-NLS-1$
						}
					}
				}

				// Comprobaciones generales

				// La cadena indefinida debe estar seguida de una cadena o un salto de linea
				if (parsers[i] instanceof UndefinedStringParser) {
					if (parsers[i].nextLimit == null) {
						throw new InvalidPatternException("La particula de texto indefinido debe ser la ultima del patron o estar seguida por un delimitador o un salto de linea"); //$NON-NLS-1$
					}
				}

				// No puede haber 2 particulas iguales seguidas
				if (parsers[i].getClass().isInstance(parsers[i + 1])) {
					throw new InvalidPatternException("No puede haber dos particulas iguales consecutivas"); //$NON-NLS-1$
				}
			}
		}
	}
}
