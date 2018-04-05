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

/**
 *
 * @author Adolfo.Navarro
 *
 */
public class LogTail {

	private  final int PART_SIZE = 1024;
	private  long filePosition = 0L;
	private  int totalBufferLines = 0;
	private final LogInfo logInfor;
	private final Path path;

	/**Constructor
	 * @throws InvalidPatternException */
	public LogTail(final LogInfo logInfo, final String path) throws InvalidPatternException {
		this.logInfor = logInfo;
		this.path = Paths.get(path);
	}

	/**
	 *Permite consultar las últimas líneas de un fichero de log.
	 *Se almacena la posición en la que se termina de leer (final del fichero) para que en una próxima llamada a getMoreLog(String log)
	 *se pueda continuar cargando desde ese punto.
	 * @param logFileName
	 * @param numLines
	 * @return
	 */
	public final String getLogTail(final int numLines) {
		 String salida = null;
		 byte[]  data = null;
		 /* Creamos el canal asociado al fichero , se podr&aacute; modificar suposi&oacute;n de lectura*/
		  try (SeekableByteChannel channel = Files.newByteChannel(this.path, StandardOpenOption.READ)) {
			  	/*Obtenemos el tama&ntilde;o del fichero,
			  	 * creamos un array con las posiciones obtenidas de la divisi&oacute;n del fichero en partes (PART_SIZE)*/
		        final long totalSize = Files.size(this.path);
		        setFilePosition(totalSize);
		        final int totalNext =  (int) (totalSize/this.PART_SIZE);
		        final int [] positions = new int [totalNext];
		        for(int i = 0; i < totalNext; i++) {
		        	positions[i] = i * this.PART_SIZE;
		        }
		        if( positions.length > 0) {
					int i = 1;
					/*Leemos el fichero n veces hasta obtener el n&uacute;mero de l&iacute;neas indicadas,
					 * obteniendo en cada lectura un nuevo bloque del fichero*/
					while (numLines > getTotalBufferLines()  && i <= positions.length) {
						final ByteBuffer buf =  ByteBuffer.allocate((int) totalSize - positions[positions.length - i]);
						channel.position(positions[positions.length - i]);
						channel.read(buf);
						buf.flip();
						data = new byte[buf.limit()];
						buf.get(data);
						salida = this.readLine(data,numLines);
						buf.clear() ;
						i++;
					}
		        }


		    } catch (final IOException e) {

				e.printStackTrace();
			}

			return salida;

	}

	/**
	 * Obtiene el m&aacute;ximo n&uacute;mero de l&iacute;neas del bloque de datos pasado como par&aacute;metro (data)
	 * hasta completar  el n&uacute;mero indicado en el par&aacute;metro (lines)
	 * @param data
	 * @param lines
	 * @return
	 */
	private  String readLine( final byte[] data, final int lines) {
		 int numLines = 0;
		 String linesDataRead = ""; //$NON-NLS-1$

		 String line = ""; //$NON-NLS-1$
		 try (final BufferedReader reader = new BufferedReader (new InputStreamReader(new ByteArrayInputStream(data), this.logInfor.getCharset()))){
			final int totalLines = getNumLines(data);
			numLines = 0;
			while ((line = reader.readLine()) != null)
			  {
				numLines++;
				if(numLines > totalLines - lines) {
					linesDataRead =linesDataRead.concat(line).concat("\n"); //$NON-NLS-1$
				}
			  }
			reader.close();
			setTotalBufferLines(numLines);
		  } catch (final IOException e) {

			e.printStackTrace();
		  }

		 return linesDataRead;
	}

	/**
	 * Obtiene el n&uacute;mero de l&iacute;neas del bloque de datos pasado como par&aacute;metro (data)
	 * @param data
	 * @return
	 */
	private static  int getNumLines(final byte[] data) {
		int numLines = 0;
		 try (final BufferedReader reader = new BufferedReader (new InputStreamReader(new ByteArrayInputStream(data)))){
				while (reader.readLine() != null){
					numLines++;
				  }
				reader.close();
			  } catch (final IOException e) {

				e.printStackTrace();
			  }
			 return numLines;
	}

	/**
	 * Establece la posici&oacute;n
	 * @param filePosition
	 */
	private final  void setFilePosition(final long filePosition) {
		this.filePosition = filePosition;
	}

	/**
	 * Obtiene la posici&oacute;n
	 * @return
	 */
	public final long getFilePosition() {
		return this.filePosition;
	}

	/**
	 * Obtiene el total de l&iacute;neas leidas
	 * @return
	 */
	private final int getTotalBufferLines() {
		return this.totalBufferLines;
	}

	/**
	 * Establece el total de l&iacute;neas leidas
	 * @param totalBufferLines
	 */
	private final  void setTotalBufferLines(final int totalBufferLines) {
		this.totalBufferLines = totalBufferLines;
	}




}
