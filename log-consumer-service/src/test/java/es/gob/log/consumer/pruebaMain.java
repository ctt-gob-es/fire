package es.gob.log.consumer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class pruebaMain {


	public static void main(final String[] args) throws ParseException {

		 Logger.getLogger(LogFilter.class.getName()).info("Este es mi log");

			LogInfo info;
			try (FileInputStream fis = new FileInputStream("C:/LOGS/fire/logging_api.loginfo")) {

				info = new LogInfo();
				info.load(fis);

//				//getTail
//				 final LogTail lTail= new LogTail(info,"C:/LOGS/fire/logging_api.log"); //$NON-NLS-1$
//				 final String resultado = new String(lTail.getLogTail(600));
//				 if(resultado != null) {
//					 System.out.println(resultado);
//				 }
//				 else {
//					 System.out.println("ERROR"); //$NON-NLS-1$
//				 }

//				 String entradaTeclado = "";
//			     final Scanner entradaEscaner = new Scanner (System.in); //Creación de un objeto Scanner
//			     entradaTeclado = entradaEscaner.nextLine ();

			     //searchText  Error indeterminado al recuperar los certificados del usuario 00001,
			     final LogSearchText lsearchText = new LogSearchText(info,"C:/LOGS/fire/logging_api.log"); //$NON-NLS-1$
			     final String result1 = new String(lsearchText.searchText(10, "Error indeterminado al recuperar los certificados del usuario 00001")); //$NON-NLS-1$
			     System.out.println(result1);


			     // Prueba 2


			     System.out.println("........................"); //$NON-NLS-1$
			     final String sdate = "mar 28, 2018 12:42:59 PM"; //$NON-NLS-1$ mar 28, 2018 12:41:12 PM mar 28, 2018 12:42:59 PM

				 final String logFormatDateTime ="MMM dd, yyyy hh:mm:ss a"; //info.getDateFormat();
				 final DateFormat formatter = new SimpleDateFormat(logFormatDateTime);
				 final Date fecha = formatter.parse(sdate);
//				 final Calendar	calendar = Calendar.getInstance();
//				 calendar.setTime(fecha);
				 final long timeMillis = fecha.getTime();
			     final String result2 = new String(lsearchText.searchText(10, "ChooseCertificateOriginService signWithProvider",timeMillis)); //$NON-NLS-1$
			     System.out.println(result2);
			   //getMore
//				final File logFile = new File("C:/LOGS/fire/logging_api.log");
//
//				try (final AsynchronousFileChannel channel =
//						AsynchronousFileChannel.open(
//								logFile.toPath(),
//								StandardOpenOption.READ);) {
//
//
//					final LogReader reader = new FragmentedFileReader(channel, info.getCharset());
//
//					final LogMore logMore = new LogMore(info);
//					System.out.println("Listado de logs");
//					reader.load(lTail.getFilePosition());
//					System.out.println(new String(logMore.getLogMore(10,reader)));
//
//
//
//				}
//
//				 catch (final IOException | InvalidPatternException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}


			} catch (final FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final InvalidPatternException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}



	}

}
