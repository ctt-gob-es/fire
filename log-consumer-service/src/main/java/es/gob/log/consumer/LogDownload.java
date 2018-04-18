package es.gob.log.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogDownload {

	private  ByteArrayOutputStream bos;
	private  ZipOutputStream zipfile;
	private final ZipEntry zipentry;
	private final String fileName;
	private final Charset charset;

	/**
	 * Constructor
	 * @param path
	 * @throws IOException
	 */
	public LogDownload (final String fileName, final Charset charset)  {
		this.fileName = fileName;
		this.charset = charset;
		this.zipentry =  new ZipEntry(this.fileName);
	}

	public final  byte[] download(final SeekableByteChannel channel) throws IOException {

			final ByteBuffer buf =  ByteBuffer.allocate(1024);
			channel.read(buf);
			buf.flip();
			final byte[] data = new byte[buf.limit()];
			buf.get(data);
            try {
            	this.bos = new ByteArrayOutputStream();
    			this.zipfile = new ZipOutputStream(this.bos, this.charset);
            	this.zipfile.write(data);

            } catch(final IOException e) {
                      e.printStackTrace();
            }
			buf.clear();

			return this.bos.toByteArray();
	}


	public final void open() throws IOException {
		this.bos = new ByteArrayOutputStream();
		this.zipfile = new ZipOutputStream(this.bos, this.charset);
		this.zipfile.putNextEntry(this.zipentry);
	}


	public final void close() throws IOException {
		//this.zipfile.closeEntry();
		this.zipfile.close();
	}


	/*
   Deflater deflater = new Deflater();
   deflater.setInput(data);
   ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
   deflater.finish();
   byte[] buffer = new byte[1024];
   while (!deflater.finished()) {
    int count = deflater.deflate(buffer); // returns the generated code... index
    outputStream.write(buffer, 0, count);
   }
   outputStream.close();
   byte[] output = outputStream.toByteArray();
   LOG.debug("Original: " + data.length / 1024 + " Kb");
   LOG.debug("Compressed: " + output.length / 1024 + " Kb");
   return output;  */

	public final  byte[] download2(final SeekableByteChannel channel) throws IOException {

		final ByteBuffer buf =  ByteBuffer.allocate(1024);
		channel.read(buf);
		buf.flip();
		final byte[] data = new byte[buf.limit()];
		buf.get(data);
		final Deflater deflater = new Deflater();
		deflater.setInput(data);
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		deflater.finish();
		final byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
		   final int count = deflater.deflate(buffer); // returns the generated code... index
		   outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		final byte[] output = outputStream.toByteArray();

		return output;

	}
}
