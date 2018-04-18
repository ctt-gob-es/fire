package es.gob.log.consumer;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogCompress {

	private  final int PART_SIZE = 1024;
	private String PATH_ZIP_FILE = "C:\\temp\\"; //$NON-NLS-1$
	private final String EXT_FILE = ".zip"; //$NON-NLS-1$
	private final Path path;
	private final String logFileName;

	/**
	 * Constructor
	 * @param path
	 */
	public LogCompress(final String path) {
		this.path = Paths.get(path);
		this.logFileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")) ;  //$NON-NLS-1$//$NON-NLS-2$
		this.PATH_ZIP_FILE = this.PATH_ZIP_FILE.concat(this.logFileName).concat(this.EXT_FILE);
	}

	public final boolean compress() {

		  byte[]  data = null;
		  try (SeekableByteChannel channel = Files.newByteChannel(this.path, StandardOpenOption.READ)) {

			try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(this.PATH_ZIP_FILE))){
				final String fileName = this.path.toString().substring(this.path.toString().lastIndexOf("\\") + 1); //$NON-NLS-1$
				final ZipEntry zipName = new ZipEntry(fileName);
				zos.putNextEntry(zipName);
				final ByteBuffer buf =  ByteBuffer.allocate(this.PART_SIZE);
				int i = 0;
				while((i = channel.read(buf)) > 0){
					 buf.flip();
					 data = new byte[buf.limit()];
					 buf.get(data);
					 zos.write(data);
					 buf.clear();
				 }

				 zos.closeEntry();
				 zos.close();
			}

			final long ZipSize = Files.size(Paths.get(this.PATH_ZIP_FILE));
			if(ZipSize > 0L) {
				return true;
			}

		}
		catch (final Exception e) {

		}
		return false;
	}



}
