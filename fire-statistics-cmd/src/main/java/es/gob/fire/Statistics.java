package es.gob.fire;

import es.gob.fire.services.statistics.FireStatistics;


public class Statistics {



	/**
	 *
	 * @param args
	 */
	public static void main(final String[] args) {

//		try {
//	    	ConfigManager.checkConfiguration();
//	    	System.out.println(ConfigManager.getStatisticsDir());
//		}
//    	catch (final Exception e) {
//    		System.out.println("Error al cargar la configuracion"); //$NON-NLS-1$
//    		return;
//    	}


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
