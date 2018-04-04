package es.gob.log.consumer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LogMore {

	private static final int SIZE_OF_REPORT_ENTRY = 1024;
	private static long filePosition = 0L;
	private static int totalBufferLines = 0;


	public static String  getLogMore(final String logFileName, final int numLines, final long actPosition ) {
		 String salida = null;
		 final Path path = Paths.get(logFileName);
		 int nLines = numLines;
		 byte[] data = null;
		 long positionResult = 0L;
		  try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {

		        final long totalSize = Files.size(path);
		         int totalNext = 0;
		        if(totalSize % SIZE_OF_REPORT_ENTRY!=0) {
		        	totalNext = (int) (totalSize / SIZE_OF_REPORT_ENTRY) + 1;
		        }
		        else {
		        	totalNext = (int) (totalSize / SIZE_OF_REPORT_ENTRY);
		        }
		        // (int) (totalSize % SIZE_OF_REPORT_ENTRY) + 1;
		        final int [] positions = new int [totalNext];
		        long nextPosition = 0L;
		        int j = 0;
		        /*Se inicializa el array de posiciones del fichero log completo*/
		        for(int i = 0; i < totalNext; i++) {
		        	if(i != totalNext -1) {
		        		positions[i] = i * SIZE_OF_REPORT_ENTRY;
		        	}
		        	else {
		        		positions[i] = (int) totalSize;
		        	}

			        if(totalSize > actPosition && positions[i] <= actPosition && actPosition > 0L) {
			        	/*Actualizamos la siguiente posici&oacute;n respecto a la posici&oacute;n actual indicada*/
			        	nextPosition = positions[i] + SIZE_OF_REPORT_ENTRY;
			        	j = i + 1;
			        }
			    }

		        /*Actualizamos la siguiente posici&oacute;n en caso de ser 0 (inicio del fichero) coincidir&aacute; con el tamano indicado de
		         * partici&oacute;n*/
		        if(actPosition <= 0L) {
		        	nextPosition = SIZE_OF_REPORT_ENTRY;
		        	j = 2;
		        }
		        /*Actualizamos la siguiente posici&oacute;n en caso de ser 0 (inicio del fichero) y n&uacute;mero de l&iacute;neas 0
		         *  coincidir&aacute; con el tamano total del fichero para leer desde la posici&oacute;n actual hasta el final*/
		        if(nLines <= 0 && actPosition <= 0L|| nLines <= 0 && actPosition > 0L) {
		        	nextPosition = totalSize;
		        	final ByteBuffer buf =  ByteBuffer.allocate((int) nextPosition);
					channel.position(actPosition);
					channel.read(buf);
					buf.flip();
					data = new byte[buf.limit()];
					buf.get(data);
					nLines = getNumLines(data);
					salida = readLine(data, nLines);
					buf.clear();
					setFilePosition(nextPosition);
		        }

		        if(positions.length > 0 && salida == null) {
		        	//positionResult = nextPosition  - actPosition;
					while (nLines > getTotalBufferLines() && j < positions.length) {
						positionResult = positions[j] - actPosition;
						final ByteBuffer buf =  ByteBuffer.allocate((int) positionResult);
						channel.position(actPosition);
						channel.read(buf);
						buf.flip();
						data = new byte[buf.limit()];
						buf.get(data);
						salida = readLine(data, nLines);
						buf.clear();
						j++;
					}

					if(salida != null) {
						final long position = channel.position() - (channel.position() - (salida.getBytes().length + actPosition));
						setFilePosition(position);
					}
					else {
						setFilePosition(channel.position());
					}

					setTotalBufferLines(0);
		        }
		    	if(channel.isOpen()) {
					channel.close();
				}

		    } catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return salida;
	}

	private static String readLine( final byte[] data, final int lines) {
		 int numLineas = 0;
		 int linesRead = lines;
		 if(linesRead <= 0) {
			 linesRead = getTotalBufferLines();
		}
		 String linesDataRead = ""; //$NON-NLS-1$
	     String line = ""; //$NON-NLS-1$

		try(final BufferedReader reader = new BufferedReader (new InputStreamReader(new ByteArrayInputStream(data)))) {

			while ((line = reader.readLine()) != null)
			{
				numLineas++;
				if(numLineas <= linesRead) {
					linesDataRead = linesDataRead.concat(line).concat("\n"); //$NON-NLS-1$
				}
				else {
					break;
				}
			}
			reader.close();
			setTotalBufferLines(numLineas);
		} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}

		return linesDataRead;

	}


	public static int getNumLines(final  byte[] data)
	{
	 int numLineas = 0;
	final BufferedReader reader = new BufferedReader (new InputStreamReader(new ByteArrayInputStream(data)));
	  try {
		while (reader.readLine() != null)
		  {
			numLineas ++;
		  }
	  } catch (final IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }

	  return numLineas;
	}



	public static void main(final String[] args) {

		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",40,0L)); //$NON-NLS-1$
		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",40,getFilePosition())); //$NON-NLS-1$
		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",40,getFilePosition())); //$NON-NLS-1$
		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",20,getFilePosition())); //$NON-NLS-1$
//		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",10,getFilePosition())); //$NON-NLS-1$
//		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",10,getFilePosition())); //$NON-NLS-1$
//		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",10,getFilePosition())); //$NON-NLS-1$
//		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",60,getFilePosition())); //$NON-NLS-1$
//		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",10,getFilePosition())); //$NON-NLS-1$
//		System.out.println(getLogMore("C:/LOGS/fire/2018-03-14-18-18.log",10,getFilePosition())); //$NON-NLS-1$

	}



	private final static void setFilePosition(final long filePosition) {
		LogMore.filePosition = filePosition;
	}

	public final static long getFilePosition() {
		return LogMore.filePosition;
	}

	private final static int getTotalBufferLines() {
		return totalBufferLines;
	}

	private final static void setTotalBufferLines(final int totalBufferLines) {
		LogMore.totalBufferLines = totalBufferLines;
	}



}
