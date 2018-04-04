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

public class LogTail {

	private static final int SIZE_OF_REPORT_ENTRY = 1024;
	private static long filePosition = 0L;
	private static int totalBufferLines = 0;

	public static String getLogTail(final String logFileName, final int numLines) {
		 String salida = null;
		 byte[]  data = null;
		 final Path path = Paths.get(logFileName);
		  try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
		        final long totalSize = Files.size(path);
		        setFilePosition(totalSize);
		        final int totalNext =  (int) (totalSize/SIZE_OF_REPORT_ENTRY);
		        final int [] positions = new int [totalNext];
		        for(int i = 0; i < totalNext; i++) {
		        	positions[i] = i * SIZE_OF_REPORT_ENTRY;
		        }
		        if( positions.length > 0) {
					int i = 1;
					while (numLines > getTotalBufferLines() ) {
						final ByteBuffer buf =  ByteBuffer.allocate((int) (totalSize-positions[positions.length - i]));
						channel.position(positions[positions.length - i]);
						channel.read(buf);
						buf.flip();
						data = new byte[buf.limit()];
						buf.get(data);
						salida = readLine(data,numLines);
						buf.clear() ;
						i++;
					}

		        }


		    } catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return salida;

	}

	private static String readLine( final byte[] data, final int lines) {
		 int numLineas = 0;
		 String linesDataRead = ""; //$NON-NLS-1$

		 String line = ""; //$NON-NLS-1$
		 try (final BufferedReader reader = new BufferedReader (new InputStreamReader(new ByteArrayInputStream(data)))){
			while ((line = reader.readLine()) != null)
			  {
				numLineas++;
				if(numLineas <= lines) {
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



	public static void main(final String[] args) {

		 final String resultado = getLogTail("C:/LOGS/fire/2018-03-14-18-18.log",40); //$NON-NLS-1$
		 if(resultado!=null) {
			 System.out.println(resultado);
		 }
		 else {
			 System.out.println("ERROR"); //$NON-NLS-1$
		 }

	}



	private final static void setFilePosition(final long filePosition) {
		LogTail.filePosition = filePosition;
	}

	public final long getFilePosition() {
		return LogTail.filePosition;
	}

	private final static int getTotalBufferLines() {
		return totalBufferLines;
	}

	private final static void setTotalBufferLines(final int totalBufferLines) {
		LogTail.totalBufferLines = totalBufferLines;
	}




}
