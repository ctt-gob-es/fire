package es.gob.fire;

import es.gob.fire.services.statistics.FireStatistics;

public class Statistics {


	/**
	 *
	 * @param args
	 */
	public static void main(final String[] args) {

		if (args != null && args.length > 0) {

			final String path = args[0];
			final FireStatistics fstatistics = new FireStatistics(path);
			if(args.length > 1) {
				final String startTime = args[1];
				fstatistics.init(startTime);
			}
			else {
				fstatistics.init();
			}
		}
		else {
			System.out.println("Error en la ejecución.\n Es necesario introducir al menos el primer parámetro " //$NON-NLS-1$
					+ "correspondiente a la ruta en donde se encuentran los ficheros logs"); //$NON-NLS-1$
		}

	}

}
