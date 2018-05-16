package es.gob.log.consumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.Scanner;
import java.util.logging.Logger;

public class pruebaMain {


	public static void main(final String[] args) throws ParseException {

		 Logger.getLogger(LogFilter.class.getName()).info("Este es mi log");

			LogInfo info;
			try (FileInputStream fis = new FileInputStream("C:/LOGS/fire/logging_api.loginfo")) {

				info = new LogInfo();
				info.load(fis);

//				//getTail
				 final LogTail lTail= new LogTail(info,"C:/LOGS/fire/logging_api.log"); //$NON-NLS-1$
				 final String resultado = new String(lTail.getLogTail(6));
				 if(resultado != null) {
					 System.out.println(resultado);
				 }
				 else {
					 System.out.println("ERROR"); //$NON-NLS-1$
				 }

				 String entradaTeclado = "";
			     final Scanner entradaEscaner = new Scanner (System.in); //Creación de un objeto Scanner
			     entradaTeclado = entradaEscaner.nextLine ();

			     //searchText  Error indeterminado al recuperar los certificados del usuario 00001,
//			     final LogSearchText lsearchText = new LogSearchText(info,"C:/LOGS/fire/logging_api.log"); //$NON-NLS-1$
//			     final String result1 = new String(lsearchText.searchText(10, "Error indeterminado al recuperar los certificados del usuario 00001")); //$NON-NLS-1$
//			     System.out.println(result1);


			     // Prueba 2


//			     System.out.println("........................"); //$NON-NLS-1$
//			     final String sdate = "mar 28, 2018 12:42:59 PM"; //$NON-NLS-1$ mar 28, 2018 12:41:12 PM mar 28, 2018 12:42:59 PM
//				 final String logFormatDateTime ="MMM dd, yyyy hh:mm:ss a"; //info.getDateFormat();
//				 final DateFormat formatter = new SimpleDateFormat(logFormatDateTime);
//				 final Date fecha = formatter.parse(sdate);
//				 final long timeMillis = fecha.getTime();
//			     final String result2 = new String(lsearchText.searchText(10, "ChooseCertificateOriginService signWithProvider",timeMillis)); //$NON-NLS-1$
//			     System.out.println(result2);

			   //getMore
				final File logFile = new File("C:/LOGS/fire/logging_api.log");

				try (final AsynchronousFileChannel channel =
						AsynchronousFileChannel.open(
								logFile.toPath(),
								StandardOpenOption.READ);) {
//
//
					final LogReader reader = new FragmentedFileReader(channel, info.getCharset());
					reader.load();
					final LogMore logMore = new LogMore(info);
					System.out.println("Listado de logs");
					reader.load(lTail.getFilePosition());
//					reader.load();
					System.out.println(new String(logMore.getLogMore(10,reader)));
					entradaTeclado = entradaEscaner.nextLine ();
					System.out.println(new String(logMore.getLogMore(5,reader)));
//
					entradaTeclado = entradaEscaner.nextLine ();
					System.out.println(new String(logMore.getLogMore(5,reader)));
//				}
//
//				 catch (final IOException | InvalidPatternException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				final LogCompress logcomp = new LogCompress("C:/LOGS/fire/logging_api.log"); //$NON-NLS-1$
//				final boolean result = logcomp.compress();
//				if(result) {
//					System.out.println("fichero comprimido"); //$NON-NLS-1$
//				}
//				else{
//					System.out.println("NO SE HA COMPRIMIDO"); //$NON-NLS-1$
//				}

				//Prueba Download
//				final Path path = Paths.get("C:/LOGS/fire/logging_api.log"); //$NON-NLS-1$
				 //$NON-NLS-1$

//				final HashMap<Integer, byte[]> map = new HashMap<>();
//
//				try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
//
//					final File f = new File("C:\\temp\\logging_api.zip"); //$NON-NLS-1$
//					if (!f.exists()) {
//						f.createNewFile();
//					}
//					final OutputStream fzip = new FileOutputStream(f,true);
//					final LogDownload logdown = new LogDownload("logging_api.log", info.getCharset()); //$NON-NLS-1$
//					logdown.open();
//					final long totalSize = Files.size(path);
//					final byte[] resul = logdown.download(channel);
//					logdown.close();
//					map.put(Integer.valueOf(resul.length), resul);
//
//					int i = 2;
//
//
//					while (channel.position() < totalSize) {
//						logdown.open();
//						final byte[] finalResult = logdown.download(channel);
//						logdown.close();
//						map.put(Integer.valueOf(finalResult.length), finalResult);
//						i++;
//					}
//					int size = 0;
//					 for(final Map.Entry m:map.entrySet()){
//						 size +=((Integer) m.getKey()).intValue();
//					}
//					final ByteBuffer buf = ByteBuffer.allocate(size);
//					 for(final Map.Entry m:map.entrySet()){
//						final byte[] resultado = (byte[]) m.getValue();
//						buf.put(resultado);
//					}
//					buf.flip();
//					final byte[] data = new byte[buf.limit()];
//					buf.get(data);
//
//
//					fzip.write(data);
//					fzip.close();

//					final ZipOutputStream zout = new ZipOutputStream(fzip , info.getCharset());
//					final ZipEntry zipName = new ZipEntry("logging_api.log"); //$NON-NLS-1$
//
//					zout.putNextEntry(zipName);
//
//					zout.write(data);
//					zout.closeEntry();
//					zout.close();


//


				}
				 catch (final Exception e) {
					 e.printStackTrace();
				}

			} catch (final FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}


