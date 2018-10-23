package es.gob.fire;

import es.gob.fire.services.statistics.FireStatistics;
import es.gob.fire.services.statistics.config.ConfigManager;


public class Statistics {



	/**
	 *
	 * @param args
	 */
	public static void main(final String[] args) {

		try {
	    	ConfigManager.checkConfiguration("config.properties"); //$NON-NLS-1$
	    	System.out.println(ConfigManager.getDataBaseConnectionString());
		}
    	catch (final Exception e) {
    		System.out.println("Error al cargar la configuracion"); //$NON-NLS-1$
    		return;
    	}


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

}
