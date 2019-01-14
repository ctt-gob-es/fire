package es.gob.fire.services.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.fire.services.statistics.config.ConfigFilesException;
import es.gob.fire.services.statistics.config.ConfigManager;
import es.gob.fire.services.statistics.config.DBConnectionException;
import es.gob.fire.services.statistics.config.DbManager;
import es.gob.fire.services.statistics.dao.SignaturesDAO;
import es.gob.fire.services.statistics.dao.TransactionsDAO;
import es.gob.fire.services.statistics.entity.ApplicationSize;
import es.gob.fire.services.statistics.entity.SignatureCube;
import es.gob.fire.services.statistics.entity.TransactionCube;


public class FireStatistics {

	static Logger LOGGER = Logger.getLogger(FireStatistics.class.getName());
	private String startTime = null;
	private static final long SEG_DIA = 24L * 60L *60L;
	private static final long SEG_HORA = 60L * 60L;
	private static final long SEG_MIN = 60L;
	private static TimeUnit timeUnit = TimeUnit.SECONDS;
	private static final String FILE_ST_NAME = "FIReSTATISTICS.log"; //$NON-NLS-1$
	private static final String FILE_SIGN = "FIReSIGNATURE";//$NON-NLS-1$
	private static final String FILE_TRANS = "FIReTRANSACTION";//$NON-NLS-1$
	private static final String FILE_EXT_LCK = ".lck"; //$NON-NLS-1$
	private static final String FILE_EXT_LOG = ".log"; //$NON-NLS-1$
	/** Nombre del fichero de configuraci&oacute;n. */
	private static final String CONFIG_FILE = "config.properties"; //$NON-NLS-1$
	final static SimpleDateFormat formater =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
	private static  String logPath = null;

	private static final HashMap<Integer , SignatureCube> hashSign = new  HashMap<>();
	private static final HashMap<String , TransactionCube> hashTrans = new  HashMap<>();
	private static final HashMap<String, ApplicationSize> hashAppSize =  new HashMap<>();


	/**
	 * Constructor del Objeto de estad&iacute;sticas se le indica la ruta donde se encuentran los ficheros.
	 * @param path
	 */
	public FireStatistics(final String path) {
		super();
		FireStatistics.setLogPath(path);
	}

	/**
	 * Lanza la ejecuci&oacute;n de la carga de datos de los fichero de estad&iacute;sticas a la base de datos, a la
	 * hora indicada por par&aacute;metro.
	 * @param startTime
	 */
	public final void init(final String startTime) {
		this.setStartTime(startTime);



		 //Se crea un hilo que se ejecuta periodicamente segun el parametro indicado en el fichero de configuracion (S)
        final ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
        final Runnable loadStatisticsData = new Runnable(){

            @Override
            public void run() {

            	 Date lastLoadDate = null;
        		 boolean loaded = false;
        		 String [] result = null;
        		 final String startProcess = formater.format(new Date());
	            try {
	            	ConfigManager.checkConfiguration(CONFIG_FILE);

					 //Obtenemos la ultima fecha de carga de estadisticas y su estado:
	            	//1 true  cargado, 0 false No se cargo
	            	// del fichero FIReSTATISTICS.log
	            	final String[] lastLoad = getLastLoadStatistic();
	            	if(lastLoad != null && lastLoad.length == 2) {
	            		if(!"".equals(lastLoad[0])) { //$NON-NLS-1$
	            			lastLoadDate = formater.parse(lastLoad[0]);
	            		}
	            		if(lastLoad[1].equals("1")) { //$NON-NLS-1$
	            			loaded = true;
	            		}
	            	}
	            	else {
	            		//lastLoadDate =
	            	}
	            	result = exeLoadStatistics(lastLoadDate);
	            	final boolean resultProcess = writeStatisticFile(result);
	            	if(resultProcess) {
	            		System.out.println("Proceso de carga de estadisticas lanzado ["+ startProcess + "] =  Correcto.");  //$NON-NLS-1$//$NON-NLS-2$
	            		LOGGER.info("Proceso de carga de estadisticas lanzado ["+ startProcess + "] =  Correcto."); //$NON-NLS-1$ //$NON-NLS-2$
	            	}
	            	else {
	            		System.out.println("Proceso de carga de estadisticas lanzado ["+ startProcess + "] =  Erroneo.");  //$NON-NLS-1$//$NON-NLS-2$
	            		LOGGER.warning("Proceso de carga de estadisticas lanzado ["+ startProcess + "] =  Erroneo."); //$NON-NLS-1$ //$NON-NLS-2$
	            	}
	            } catch (final SecurityException e) {
	                LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
	            } catch (final IOException e) {
	                LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
	            } catch (final ParseException e) {
					LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (final SQLException e) {
					LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (final DBConnectionException e) {
					LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (final ConfigFilesException e) {
					LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
				}
            }

        };
        sch.scheduleAtFixedRate(loadStatisticsData, getSecondsInitialDelay(), SEG_DIA, timeUnit);

	}

	/**
	 * Lanza la ejecuci&oacute;n de la carga de datos de los fichero de estadisticas a la base de datos.
	 */
	public final void init() {

		Date lastLoadDate = null;
		boolean loaded = false;
        String [] result = null;
        final String startProcess = formater.format(new Date());
        try {
        	ConfigManager.checkConfiguration(CONFIG_FILE);
        	//Obtenemos la ultima fecha de carga de estadisticas y su estado:
	        //1 true  cargado, 0 false No se cargo
	        // del fichero FIReSTATISTICS.log
        	final String[] lastLoad = getLastLoadStatistic();
	        if(lastLoad != null && lastLoad.length == 2) {
	            if(!"".equals(lastLoad[0])) { //$NON-NLS-1$
	            	lastLoadDate = formater.parse(lastLoad[0]);
	            }
	            if(lastLoad[1].equals("1")) { //$NON-NLS-1$
	            	loaded = true;
	            }
	        }
	        result = exeLoadStatistics(lastLoadDate);
	        final boolean resultProcess = writeStatisticFile(result);

	        if(resultProcess) {
	        	System.out.println("Proceso de carga de estadisticas lanzado ["+ startProcess + "] =  Correcto.");  //$NON-NLS-1$//$NON-NLS-2$
	            LOGGER.info("Proceso de carga de estadisticas lanzado ["+ startProcess + "] =  Correcto."); //$NON-NLS-1$ //$NON-NLS-2$
	        }
	        else {
	        	System.out.println("Proceso de carga de estadisticas lanzado ["+ startProcess + "] =  Erroneo.");  //$NON-NLS-1$//$NON-NLS-2$
	        	LOGGER.warning("Proceso de carga de estadisticas lanzado ["+ startProcess + "] =  Erroneo."); //$NON-NLS-1$ //$NON-NLS-2$
	        }
        } catch (final SecurityException e) {
        	LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
	    } catch (final IOException e) {
	    	LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
	    } catch (final ParseException e) {
	    	LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
	    } catch (final SQLException e) {
	    	LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
	    } catch (final DBConnectionException e) {
	    	LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (final ConfigFilesException e) {
			LOGGER.severe("Error en hilo del proceso de carga de estadisticas lanzado ["+ startProcess + "]  :".concat(e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	/**
	 * Obtiene los segundos que faltan para iniciar una nueva carga de datos estad&iacute;sticos,
	 * respecto al par&aacute;metro indicado en el fichero de configuraci&oacute;n
	 * @return
	 */
	 private final long getSecondsInitialDelay() {
		 	final String [] start = getStartTime().split(":"); //$NON-NLS-1$
		 	final int hour = start[0] != null ?  Integer.parseInt(start[0]) : 0;
		 	final int minute = start[1] != null ?  Integer.parseInt(start[1]) : 0;
		 	final int second = start[2] != null ?  Integer.parseInt(start[2]) : 0;

			final Calendar c = Calendar.getInstance();
		    final long now = c.getTimeInMillis();
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.SECOND,second);
			c.set(Calendar.MILLISECOND, 0);
			final long passed = now - c.getTimeInMillis();
			final long secondsPassedToday = passed / 1000;
			if(secondsPassedToday < 0) {
				return - secondsPassedToday;
			}
			return SEG_DIA - secondsPassedToday;

	 }

	 /**
	  * Lee el fichero de resultados de ejecuci&oacute;n de carga de estadisticas en donde obtiene
	  * la &uacute;ltima fecha de ejecuci&oacute;n correcta, si no existe devuelve un null,
	  * @return
	  */
	  static final String[] getLastLoadStatistic() throws IOException {
		  File f;
		  String lastLine[] = null;
			try {
				f = new File(getLogPath().concat(File.separator).concat(FILE_ST_NAME)).getCanonicalFile();//es.gob.fire.services.ConfigManager.getLogsDir()
				if(f.exists()) {
					final BufferedReader br = new BufferedReader(new FileReader(f));
					String last = br.readLine();
					while (last != null && !"".equals(last) && last.contains(";")) { //$NON-NLS-1$ //$NON-NLS-2$
						lastLine = last.split(";"); //$NON-NLS-1$
						last = br.readLine();
					}
					br.close();
				}
				else {
					throw new IOException();
				}
			} catch (final IOException e1) {
				LOGGER.log(Level.SEVERE, "Error al leer el fichero".concat(FILE_ST_NAME).concat(" en la ruta ").concat(getLogPath())); //$NON-NLS-1$ //$NON-NLS-2$
				throw new IOException();
			}

		 return lastLine;
	 }

	  /**
	   * Ejecuta la carga de los ficheros log de estad&iacute;sticas
	   * a la base de datos
	   * @param date &Uacute;ltima fecha de carga correcta (puede tener valor nulo).
	   * @return Array de String  con el formato yyyy-MM-dd HH:mm:ss;N (1 ó 0) pudiendo ser nulo.
	 * @throws ParseException
	 * @throws IOException
	 * @throws DBConnectionException
	 * @throws SQLException
	 * @throws ConfigFilesException
	 * @throws NumberFormatException
	   */
	  //TODO Revisar metodo de lectura de listado de ficheros, para que pueda cargar primero el FIReSIGNATURE y despues el FIReTRANSACTION de una fecha y continuar con los otros ficheros por fecha
	  // actualmente 14/12/2018 los carga todos seguidos
	  static final String [] exeLoadStatistics(final Date date) throws ParseException, IOException, SQLException, DBConnectionException, NumberFormatException, ConfigFilesException {
		  String [] result = null;
		  //Obtiene la lista de ficheros para ejecutar la carga.
		  final String [] listFiles = getStatisticsFiles(date);
		  if(listFiles != null && listFiles.length > 0) {
			  result = new String[listFiles.length];
			  for (int i = 0; i < listFiles.length; i++) {
				  final int totalReg = countFileReg(listFiles[i]);
				  result[i] = prepareStatisticDB(listFiles[i],totalReg);
				  //TODO ojo revisar esta funcion ya que ahora solo cargaria un fichero de transaction en caso de haber varios ficheros de transactions y signature.
				  if(!hashSign.isEmpty() && !hashTrans.isEmpty() && !hashAppSize.isEmpty()) {
					  insertStatisticDB();
					  hashSign.clear();
					  hashTrans.clear();
					  hashAppSize.clear();
				  }

			  }
		  }

		  return result;
	  }

	  /**
	   * Apartir de dicha fecha se cargan los ficheros log cuya fecha sea superior a la indicada e inferior a la fecha de
	   * ejecucion (fecha actual)
	   * No tiene datos registrados el fichero FIReSTATISTICS.log se carga el fichero log cuya fecha sea la
	   * anterior a la fecha de ejecucion (fecha actual)
	   * @return
	 * @throws ParseException
	   */
	  static final String [] getStatisticsFiles(final Date lastDate ) throws ParseException {

		  	final SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		  	String [] result = null;
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);
		    c.set(Calendar.MINUTE, 0);
		    c.set(Calendar.SECOND, 0);
		    c.set(Calendar.MILLISECOND, 0);
		  	final Date today = c.getTime();
			final File f = new File(getLogPath());
			if(f.exists()) {
				final File[] files = f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						if(name.lastIndexOf(".") > 0) { //$NON-NLS-1$
							final int dot = name.lastIndexOf('.');
			                final String ext = name.substring(dot);
			                final String sDateFile = name.substring(name.lastIndexOf("_") + 1 , dot); //$NON-NLS-1$
			                Date dateFile;
							try {
								dateFile = format.parse(sDateFile);
								if(lastDate != null) {
									final Date lastLoadDate = format.parse(format.format(lastDate));
				                	if(dateFile.before(today) && dateFile.after(lastLoadDate)
				                		&& ext.equalsIgnoreCase(FILE_EXT_LOG)
				                		&& !ext.equalsIgnoreCase(FILE_EXT_LCK) && ext.length() == 4 && (name.contains(FILE_SIGN) || name.contains(FILE_TRANS))) {
										return true;
									}
				                }
				                else {
				                	final Calendar cDateFile =  Calendar.getInstance();
				                	final Calendar cToday =  Calendar.getInstance();
				                	cDateFile.setTime(dateFile);
				                	cToday.setTime(today);
				                	if(cDateFile.get(Calendar.DAY_OF_YEAR) == cToday.get(Calendar.DAY_OF_YEAR -1)
				                		&& ext.equalsIgnoreCase(FILE_EXT_LOG)
				                		&& !ext.equalsIgnoreCase(FILE_EXT_LCK) && ext.length() == 4 && (name.contains(FILE_SIGN) || name.contains(FILE_TRANS))) {
				                		return true;
				                	}
				                }

							} catch (final ParseException e) {
								 return false;
							}
			                return false;
						}
						return false;
					}
				});
				if( files.length > 0) {
					result = new String[files.length];
					for (int i = 0; i < files.length; i++){
						result[i] = files[i].getName();
					}
				}
			}
			return result;
	  }

	  /**
	   * Cuenta los registros (lineas de texto) del fichero log indicado como par&aacute;metro
	   * @param fileName
	   * @return
	 * @throws IOException
	   */
	  static final int countFileReg(final String fileName) throws IOException {
		  File f;
		  int totalReg = 0;
			try {
				f = new File(getLogPath().concat(File.separator).concat(fileName)).getCanonicalFile();
				if(f.exists()) {
					final BufferedReader br = new BufferedReader(new FileReader(f));
					String registry;
					while ((registry = br.readLine()) != null && !registry.isEmpty()) {
						totalReg ++;
					}
					br.close();
				}
				else {
					throw new IOException();
				}
			} catch (final IOException e1) {
				LOGGER.log(Level.SEVERE, "Error al leer el fichero".concat(fileName).concat(" en la ruta indicada ").concat(getLogPath())); //$NON-NLS-1$ //$NON-NLS-2$
				throw new IOException();
			}
		  return totalReg;
	  }

	  /**
	   * Funci&oacute;n que prepara los registros de los ficheros log pasados por par&aacute;metro en mapas hash
	   * para ser insertados en la BBDD.
	   * @param fileName
	   * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws DBConnectionException
	 * @throws ParseException
	 * @throws es.gob.fire.services.statistics.config.DBConnectionException
	 * @throws NumberFormatException
	 * @throws ConfigFilesException
	   */
	  //TODO: Revisar el reellenado de los mapas hash de tamanno por si se puede quedar algun dato nulo
	static final String prepareStatisticDB(final String fileName, final int totalReg) throws SQLException, IOException, DBConnectionException, ParseException, NumberFormatException, es.gob.fire.services.statistics.config.DBConnectionException, ConfigFilesException {

		 final File f;
		 String result = null;

		 if(fileName != null && !"".equals(fileName) ) {  //$NON-NLS-1$
			f = new File(getLogPath().concat(File.separator).concat(fileName)).getCanonicalFile();
			if(f.exists()) {
				final BufferedReader br = new BufferedReader(new FileReader(f));
				String registry;
				if(fileName.contains(FILE_SIGN)) {
					int key = 1;
					//Se lee registro de FIReSIGNATURE
					while ((registry = br.readLine()) != null) {
						if(!registry.isEmpty()) {
							//se obtiene el objeto SignatureCube del registro
							final SignatureCube sign =  SignatureCube.parse(registry);
							final ApplicationSize appSize = new ApplicationSize();
							if(sign != null) {
								//Se rellena el mapa de las firmas
								if(hashSign.isEmpty()) {
									//Si es la primera vez se introduce el primer objeto
									hashSign.put(new Integer(key), sign);
								}
								else {
									//Buscamos si existe otro objeto firma con los mismos datos anteriormente
									final Integer k =  searchSignature(hashSign,sign);
									if(k != null) {
										final SignatureCube sign2 = hashSign.get(k);
										final long total1 = sign.getTotal().longValue();
										final long total2 = sign2.getTotal().longValue();
										sign.setTotal(new Long (total1 + total2));
										hashSign.remove(k);
										hashSign.put(k, sign);
									}
									else {
										hashSign.put(new Integer(key), sign);
									}
								}
								//Rellenamos el mapa del size por aplicacion de cada transaccion
								appSize.setId_Transaction(sign.getId_transaccion());
								appSize.setSize(sign.getSize());

								if(hashAppSize.isEmpty()) {
									hashAppSize.put(appSize.getId_Transaction(), appSize);
								}
								else if(hashAppSize.get(appSize.getId_Transaction()) != null) {
									final ApplicationSize app = hashAppSize.get(appSize.getId_Transaction());
									final long tam1 = app.getSize().longValue();
									final long tam2 = appSize.getSize().longValue();
									appSize.setSize(new Long (tam1 + tam2));
									hashAppSize.remove(appSize.getId_Transaction());
									hashAppSize.put(appSize.getId_Transaction(), appSize);
								}
								else {
									hashAppSize.put(appSize.getId_Transaction(), appSize);
								}
							}
						}
						key ++;
					}

				}
				else if(fileName.contains(FILE_TRANS)) {
					while ((registry = br.readLine()) != null) {
						if(!registry.isEmpty()) {
							final TransactionCube trans =  TransactionCube.parse(registry);
							if(trans != null) {

								//Rellenamos el mapa de Transacciones
								if(hashTrans.isEmpty()) {
									//Si es la primera vez se introduce el primer objeto
									final ApplicationSize appSize = hashAppSize.get(trans.getId_transaccion());
									if(appSize != null && appSize.getSize() != null) {
										trans.setSize(appSize.getSize());
									}
									hashTrans.put(trans.getId_transaccion(), trans);
								}
								else {
									//Buscamos si existe otro objeto transaccion con los mismos datos anteriormente
									final String k =  searchTransaction(hashTrans, trans);
									if(k != null) {
										final TransactionCube trans2 = hashTrans.get(k);
										final long total1 = trans.getTotal().longValue();
										final long total2 = trans2.getTotal().longValue();
										final ApplicationSize appSize = hashAppSize.get(trans.getId_transaccion());
										final ApplicationSize appSize2 = hashAppSize.get(k);
										if(appSize != null && appSize.getSize() != null && appSize2 != null && appSize2.getSize() != null) {
											final long size1 = appSize.getSize().longValue();
											final long size2 = appSize2.getSize().longValue();
											trans.setSize(new Long(size1 + size2));
										}
										trans.setTotal(new Long (total1 + total2));
										hashTrans.remove(k);
										hashTrans.put(k, trans);
									}
									else {
										final ApplicationSize appSize = hashAppSize.get(trans.getId_transaccion());
										if(appSize != null && appSize.getSize() != null) {
											trans.setSize(appSize.getSize());
										}
										hashTrans.put(trans.getId_transaccion(), trans);
									}
								}
								//Se actualiza el nombre de la aplicacion en el mapa hash que tiene los tamannos de las transacciones
								// ya que antes de esto el valor del nombre de la aplicacion en el objeto ApplicationSize es nulo.
								if(hashAppSize.get(trans.getId_transaccion()) != null) {
									final ApplicationSize appSize = hashAppSize.get(trans.getId_transaccion());
									appSize.setApplication(trans.getAplicacion());
									hashAppSize.remove(trans.getId_transaccion());
									hashAppSize.put(trans.getId_transaccion(), appSize);
								}

							}
						}
					}
				}
				br.close();
			}
		}

		result = formater.format(new Date()).concat(";1"); //$NON-NLS-1$
		return result;
	}

	/**
	 *	Lee los mapas y se procede a realizar las inserciones en la BBDD
	 * @param totalReg
	 * @return
	 * @throws SQLException
	 * @throws DBConnectionException
	 * @throws NumberFormatException
	 * @throws es.gob.fire.services.statistics.config.DBConnectionException
	 * @throws ConfigFilesException
	 */
	static final int insertStatisticDB() throws SQLException, DBConnectionException, NumberFormatException, es.gob.fire.services.statistics.config.DBConnectionException, ConfigFilesException {

		 int regInserted = 0;
		 for (final Map.Entry<Integer, SignatureCube> objSign : hashSign.entrySet()) {
			 final SignatureCube sign = objSign.getValue();
			 if(sign != null) {
				 //Se asigna el nombre de la aplicacion a la firma antes de insertar el dato en la tabla t_firmas
				 final ApplicationSize appSize = hashAppSize.get(sign.getId_transaccion());
				 sign.setAplicacion(appSize.getApplication());
				 regInserted = regInserted +  SignaturesDAO.insertSignature(sign);
			 }
		 }

		 for (final Map.Entry<String, TransactionCube> objTrans : hashTrans.entrySet()) {
			final TransactionCube trans = objTrans.getValue();
			if(trans != null) {
				if( trans.getSize() == null ) {
					trans.setSize(new Long (0L));
				}
				regInserted = regInserted +  TransactionsDAO.insertTransaction(trans);
			}
		 }

		DbManager.runCommit();
		return regInserted;
	}


	/**
	 * Escribe el resultado de la carga en el fichero FIReSTATISTICS.log
	 * @param result true si se ha realizado correctamente la escritura en el fichero, false si ha habido alg&uacute;n fallo
	 * @return
	 * @throws IOException
	 */
	static final boolean writeStatisticFile(final String [] result) throws IOException {
		boolean res = false;
		int count = 0;
		if(result != null && result.length > 0) {
			final FileWriter fw = new FileWriter(getLogPath().concat(File.separator).concat(FILE_ST_NAME),true);
			final PrintWriter pw = new PrintWriter(fw);
			for (int i = 0; i < result.length; i++) {
				pw.println(result[i]);
				count ++;
			}
			if(count == result.length) {
				res = true;
			}
			if (fw != null ) {
				fw.close();
			}
		}
		return res;
	}

	/**
	 * Compara dos objetos firma
	 * @param sign1
	 * @param sign2
	 * @return true si son iguales , false si son distintos
	 */
	static final boolean compareSignature(final SignatureCube sign1, final SignatureCube sign2) {
		boolean result = false;
		if(sign1 != null && sign2 != null) {
			//Comparar algoritmo
			if(sign1.getAlgorithm()!= null && sign2.getAlgorithm() != null &&
					!sign1.getAlgorithm().isEmpty() && !sign2.getAlgorithm().isEmpty() &&
					sign1.getAlgorithm().equals(sign2.getAlgorithm())) {
				result = true;
			}
			else {
				return  false;
			}
			//Comparar formato
			if(sign1.getFormat()!= null && sign2.getFormat() != null &&
					!sign1.getFormat().isEmpty() && !sign2.getFormat().isEmpty() &&
					sign1.getFormat().equals(sign2.getFormat())) {
				result = true;
			}
			else {
				return  false;
			}
			//Comparar formato mejorado
			if(sign1.getImprovedFormat() != null && sign2.getImprovedFormat() != null &&
					!sign1.getImprovedFormat().isEmpty() && !sign2.getImprovedFormat().isEmpty() &&
					sign1.getImprovedFormat().equals(sign2.getImprovedFormat())) {
				result = true;
			}
			//Comparar proveedor
			if(sign1.getProveedor() != null && sign2.getProveedor() != null &&
					!sign1.getProveedor().isEmpty() && !sign2.getProveedor().isEmpty() &&
					sign1.getProveedor().equals(sign2.getProveedor())) {
				result = true;
			}
			else {
				return  false;
			}
			//Comparar navegador
			if(sign1.getNavegador() != null && sign1.getNavegador().getName() != null &&
					sign2.getNavegador() != null && sign2.getNavegador().getName() != null &&
					!sign1.getNavegador().getName().isEmpty() && !sign2.getNavegador().getName().isEmpty() &&
					sign1.getNavegador().getName().equals(sign2.getNavegador().getName())) {

				result = true;
			}
			else {
				return  false;
			}
			//Comparar resultados
			/*EXNOR:NOT OR EXCLUSIVE
			 * true ^ true = true
			 * false ^ false = true
			 * */
			if(!(sign1.isResultSign() ^ sign2.isResultSign())) {
				result = true;
			}
			else {
				return  false;
			}

		}

		return result;
	}

	/**
	 * Compara dos objetos transacci&oacute;n
	 * @param trans1
	 * @param trans2
	 * @return true si son iguales , false si son distintos
	 */
	static final boolean compareTransaction(final TransactionCube trans1, final TransactionCube trans2) {
		boolean result = false;
		if(trans1 != null && trans2 != null) {
			//Comparar aplicacion
			if(trans1.getAplicacion() != null && trans2.getAplicacion() != null &&
					!trans1.getAplicacion().isEmpty() && !trans2.getAplicacion().isEmpty() &&
					trans1.getAplicacion().equals(trans2.getAplicacion())) {
				result = true;
			}
			else {
				return  false;
			}
			//Comparar operacion
			if(trans1.getOperacion() != null && trans2.getOperacion() != null &&
					!trans1.getOperacion().isEmpty() && !trans2.getOperacion().isEmpty() &&
					trans1.getOperacion().equals(trans2.getOperacion())) {
				result = true;
			}
			else {
				return  false;
			}
			//Comparar proveedor
			if(trans1.getProveedor() != null && trans2.getProveedor() != null &&
					!trans1.getProveedor().isEmpty() && !trans2.getProveedor().isEmpty() &&
					trans1.getProveedor().equals(trans2.getProveedor())) {
				result = true;
			}
			else {
				return  false;
			}

			//Comparar proveedor forzado
			/*EXNOR:NOT OR EXCLUSIVE
			 * true ^ true = true
			 * false ^ false = true
			 * */
			if(!(trans1.isProveedorForzado() ^ trans2.isProveedorForzado())) {
				result = true;
			}
			else {
				return  false;
			}
			//Comparar resultado
			/*EXNOR:NOT OR EXCLUSIVE
			 * true ^ true = true
			 * false ^ false = true
			 * */
			if(!(trans1.isResultTransaction() ^ trans2.isResultTransaction())) {
				result = true;
			}
			else {
				return  false;
			}
		}

		return result;
	}

	/**
	 * Busca un objeto SignatureCube en el mapa que se le pasa por par&aacute;metro, que sea igual al
	 * objeto SignatureCube que se pasa por par&aacute;metro.
	 * @param hash
	 * @param sign
	 * @return Devuelve la clave del objeto si se ha encontrado, si no devuelve un nulo.
	 */
	static final Integer searchSignature(final HashMap<Integer, SignatureCube> hash, final SignatureCube sign) {
		for (final Map.Entry<Integer, SignatureCube> objSign : hash.entrySet()) {
			final SignatureCube sign2 = objSign.getValue();
			if(compareSignature(sign2,sign)) {
				return objSign.getKey();
			}
		}
		return null;
	}

	/**
	 *Busca un objeto TransactionCube en el mapa que se le pasa por par&aacute;metro, que sea igual al
	 * objeto TransactionCube que se pasa por par&aacute;metro.
	 * @param hash
	 * @param trans
	 * @return Devuelve la clave del objeto si se ha encontrado, si no devuelve un nulo.
	 */
	static final String  searchTransaction(final HashMap<String, TransactionCube> hash, final TransactionCube trans) {
		for (final Map.Entry<String, TransactionCube> objTrans : hash.entrySet()) {
			final TransactionCube trans2 = objTrans.getValue();
			if(compareTransaction(trans2, trans)) {
				return objTrans.getKey();
			}
		}
		return null;
	}


	private final  String getStartTime() {
		return this.startTime;
	}
	private final void setStartTime(final String startTime) {
		this.startTime = startTime;
	}

	public  final static String getLogPath() {
		return FireStatistics.logPath;
	}

	private final static void setLogPath(final String logPath) {
		FireStatistics.logPath = logPath;
	}




}
