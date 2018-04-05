package es.gob.log.consumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.logging.Logger;

public class pruebaMain {


	public static void main(final String[] args) {

		 Logger.getLogger(LogFilter.class.getName()).info("Este es mi log");

			LogInfo info;
			try (FileInputStream fis = new FileInputStream("C:/LOGS/fire/logback_api.loginfo")) {

				info = new LogInfo();
				info.load(fis);

				//getTail
				 final LogTail lTail= new LogTail(info,"C:/LOGS/fire/logging_api.log"); //$NON-NLS-1$
				 final String resultado = lTail.getLogTail(6);
				 if(resultado!=null) {
					 System.out.println(resultado);
				 }
				 else {
					 System.out.println("ERROR"); //$NON-NLS-1$
				 }

				 String entradaTeclado = "";
			     final Scanner entradaEscaner = new Scanner (System.in); //Creación de un objeto Scanner
			     entradaTeclado = entradaEscaner.nextLine ();

			   //getMore
				final File logFile = new File("C:/LOGS/fire/logging_api.log");

				try (final AsynchronousFileChannel channel =
						AsynchronousFileChannel.open(
								logFile.toPath(),
								StandardOpenOption.READ);) {


					final LogReader reader = new FragmentedFileReader(channel, info.getCharset());

					final LogMore logMore = new LogMore(info);
					System.out.println("Listado de logs");
					reader.load(lTail.getFilePosition());
					System.out.println(new String(logMore.getLogMore(10,reader)));



				}

				 catch (final IOException | InvalidPatternException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


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
