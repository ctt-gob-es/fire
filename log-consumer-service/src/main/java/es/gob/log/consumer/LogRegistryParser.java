package es.gob.log.consumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Analizador de registros de log. Permite que el lector de logs ({@link LogRegistryReader})
 * identifique qu&eacute; es un log y cuales son sus componentes.
 */
public class LogRegistryParser {

	private final ParticleParser[] pParsers;

	private final ParticleParserFactory parserFactory;

	/** Indica si sera necesario leer activamente una nueva l&iacute;nea de
	 * log al terminar de leer el registro.
	 */
	private final boolean needActiveReadline;

	/**
	 * Analizador de registros de log.
	 * @param logInfo Informaci&oacute;n acerca del formato del log.
	 */
	public LogRegistryParser(final LogInfo logInfo) {
		this.parserFactory = ParticleParserFactory.getInstance(logInfo);
		if (logInfo != null) {
			this.pParsers = buildParsersList(logInfo.getLogPattern(), this.parserFactory);

			// Si el ultimo analizador busca activamente nuevas lineas, le tenemos que indicar
			// como idenficiar que se ha encontrado el inicio de un nuevo registro.
			// En caso contrario, cuando terminemos de leer un registro tendremos que leer una
			// linea (que ya sera del siguiente) para que pueda procesarse el nuevo registro
			if (this.pParsers[this.pParsers.length - 1] instanceof ParticleParserUndefined) {
				this.needActiveReadline = false;
				((ParticleParserUndefined) this.pParsers[this.pParsers.length - 1])
					.setInitialParser(
							this.pParsers[0],
							this.pParsers.length > 2 ? this.pParsers[1].getLimit() : null);
			}
			else {
				this.needActiveReadline = true;
			}
		}
		else {
			this.pParsers = null;
			this.needActiveReadline = true;
		}
	}

	/**
	 * Identifica un nuevo registro analizando las l&iacute;neas del fichero.
	 * @param reader Lector de logs.
	 * @return Registro de logs.
	 * @throws IOException Si se produce alg&uacute;n error durante la lectura.
	 * @throws InvalidRegistryFormatException Si se encuentra una entrada que no
	 * cumple los requisitos de un registro.
	 */
	LogRegistry parse(final LogReader reader) throws IOException, InvalidRegistryFormatException {

		final LogRegistry registry = new LogRegistry(reader.getCurrentLine().toString());
		if (this.pParsers != null) {
			try {
				for (int i = 0; i < this.pParsers.length; i++) {
					this.pParsers[i].parse(
							reader,
							getLimit(i, this.pParsers),
							registry);
				}
			}
			catch (final InvalidRegistryFormatException e) {
				// Si encontramos un registro sin el formato adecuado,
				// lo comprendemos como un registro sin datos
				e.setRegistry(registry);

				// Cargamos la siguiente linea para que se empiece a tratar
				// como un registro nuevo
				reader.readLine();

				throw e;
			}
		}

		// Si el ultimo parse no avanza la linea, lo hacemos nosotros
		if (this.needActiveReadline) {
			reader.readLine();
		}

		return registry;
	}

	private static ParticleParser[] buildParsersList(final String pattern, final ParticleParserFactory factory) {
		return buildParsers(pattern, factory).toArray(new ParticleParser[0]);
	}

	private static List<ParticleParser> buildParsers(final String pattern, final ParticleParserFactory factory) {

		final List<ParticleParser> list = new ArrayList<>();

		// Dividiremos el patron en fragmentos, agregando uno cada vez a la lista
		for (final ParticlePattern particle : ParticlePattern.values()) {
			if (particle.indexOf(pattern) != -1) {
				parseParticle(list, factory, pattern, particle);
				break;
			}
		}
		return list;
	}

	private static void parseParticle(final List<ParticleParser> list, final ParticleParserFactory factory,
			final String text, final ParticlePattern pp) {

		final int idx = pp.indexOf(text);
		if (idx > -1) {
			if (idx > 0) {
				addList(list, buildParsers(text.substring(0, idx), factory));
			}
			addList(list, factory.build(pp, text));
			if (idx + pp.getPatternLength() < text.length()) {
				addList(list, buildParsers(text.substring(idx + pp.getPatternLength()), factory));
			}
		}
	}

	private static List<ParticleParser> addList(final List<ParticleParser> list, final List<ParticleParser> internalList) {
		for (final ParticleParser pp : internalList) {
			list.add(pp);
		}
		return list;
	}

	private static List<ParticleParser> addList(final List<ParticleParser> list, final ParticleParser particle) {
		list.add(particle);
		return list;
	}


	/** Determina cual es la cadena que debe hacer de l&iacute;mite de
	 * la part&iacute;cula actual.
	 * @param i Indice de la particula actual.
	 * @param pParsers Listado de analizadores.
	 * @return Cadena a usar como limite o {@code null} si no se ha definido.
	 */
	private static String getLimit(final int i, final ParticleParser[] pParsers) {
		String limit = null;
		if (i + 1 < pParsers.length) {
			limit = pParsers[i + 1].getLimit();
		}
		return limit;
	}
}
