package es.gob.log.consumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Analizador de registros de log. Permite que el lector de logs ({@link LogRegistryReader})
 * identifique qu&eacute; es un log y cuales son sus componentes.
 */
public class LogRegistryParser {

	/** Indica si sera necesario leer activamente una nueva l&iacute;nea de
	 * log al terminar de leer el registro.
	 */
	private final boolean needActiveReadline;

	/** Listado de analizadores de particulas para identificar los registros. */
	private final ParticleParser[] pParsers;


	/**
	 * Establece la informaci&oacute;n para el an&aacute;lisis de los registros de log.
	 * @param logInfo Informaci&oacute;n acerca del formato del log.
	 * @throws InvalidPatternException Cuando en la informaci&oacute;n proporcionada se
	 * indique un patr&oacute;n de registro no v&aacute;lido.
	 */
	LogRegistryParser(final LogInfo logInfo) throws InvalidPatternException {

		if (logInfo != null) {
			final ParticleParserFactory parserFactory = ParticleParserFactory.getInstance(logInfo);
			this.pParsers = buildParsersList(logInfo.getLogPattern(), parserFactory);
		}
		else {
			this.pParsers = null;
		}

		// En caso de que el ultimo elemento del patron no admita registros de un numero indeterminado
		// de lineas, la lectura del patron siempre terminaria al final de una linea. En ese caso,
		// tendremos que avanzar activamente a la siguiente linea antes de leer un nuevo registro
		this.needActiveReadline = this.pParsers == null ||
				!(this.pParsers[this.pParsers.length - 1] instanceof ParticleParserUndefined);
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

	private static ParticleParser[] buildParsersList(final String pattern, final ParticleParserFactory factory) throws InvalidPatternException {
		final ParticleParser[] parsers = buildParsers(pattern, factory).toArray(new ParticleParser[0]);

		// A cada uno de los parsers, le asignamos como limite el establecido por el parser siguiente.
		// En el caso del ultimo, si este busca nuevas lineas, le asignaremos el propio parser, para que
		// pueda identificarlas
		for (int i = 0; i < parsers.length; i++) {

			if (i + 1 < parsers.length) {
				parsers[i].setNextLimit(parsers[i + 1].getLimit());
			}
			else if (parsers[parsers.length - 1] instanceof ParticleParserUndefined) {
				((ParticleParserUndefined) parsers[parsers.length - 1]).setInitialParser(parsers[0]);
			}
		}

		// Comprobamos que el patron sea correcto
		ParticleParserFactory.checkPattern(parsers);

		return parsers;
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
			final String pattern, final ParticlePattern pp) {

		final int idx = pp.indexOf(pattern);
		if (idx > -1) {
			if (idx > 0) {
				addList(list, buildParsers(pattern.substring(0, idx), factory));
			}
			addList(list, factory.build(pp, pattern));
			if (idx + pp.getPatternLength() < pattern.length()) {
				addList(list, buildParsers(pattern.substring(idx + pp.getPatternLength()), factory));
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





}
