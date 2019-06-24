package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import es.gob.log.consumer.LogDownload;


/**
 * Manejador para la gesti&oacute;n de las peticiones de descarga de fichero.
 */
public class LogDownloadServiceManager {

	private static final Logger LOGGER = Logger.getLogger(LogDownloadServiceManager.class.getName());

	/**
	 * Procesa una peticion de descarga de fichero.
	 * @param req Peticion HTTP realizada.
	 * @param pathLogs Ruta del directorio de logs.
	 * @return Fragmento de datos descargados.
	 * @throws IOException Ocurrio un error al acceder al contenido del fichero para su descarga.
	 */
	public final static DataFragment process(final HttpServletRequest req, final String pathLogs) throws IOException {

		final boolean reset = Boolean.parseBoolean(req.getParameter(ServiceParams.PARAM_RESET));
		if (reset) {
			removeDownloadSessions(req);
		}

		final HttpSession session = req.getSession(false);

		final String logFileName = req.getParameter(ServiceParams.LOG_FILE_NAME);
		final File dataFile = new File(pathLogs, logFileName);

		if (!dataFile.getCanonicalPath().startsWith(pathLogs)){
			throw new SecurityException("Se ha intentado acceder a una ruta fuera del directorio de logs: " + dataFile.getAbsolutePath()); //$NON-NLS-1$
        }

		Long fileDownloadPosition = new Long(0L);

		if ((Long) session.getAttribute("FileDownloadPos") != null) { //$NON-NLS-1$
			fileDownloadPosition = (Long)session.getAttribute("FileDownloadPos"); //$NON-NLS-1$
		}

		SeekableByteChannel channel;
		if ((SeekableByteChannel) session.getAttribute("ChannelDownload") != null) { //$NON-NLS-1$
			channel = (SeekableByteChannel) session.getAttribute("ChannelDownload"); //$NON-NLS-1$
		}
		else {
			try {
				channel = FileChannel.open(dataFile.toPath(), StandardOpenOption.READ);
			}
			catch (final Exception e) {
				removeDownloadSessions(req);
				throw new IOException("Ocurrio un error durante la apertura del fichero de log", e);
			}
			session.setAttribute("ChannelDownload", channel); //$NON-NLS-1$
		}

		LogDownload download;
		if ((LogDownload) session.getAttribute("Download") != null) { //$NON-NLS-1$
			download = (LogDownload) session.getAttribute("Download");//$NON-NLS-1$
		}
		else {
			download =  new LogDownload(logFileName, channel);
			session.setAttribute("Download", download); //$NON-NLS-1$
		}

		DataFragment result;

		try {
			channel.position(fileDownloadPosition.longValue());

			final byte[] data = download.download();
			final boolean partial = download.hasMore();
			result = new DataFragment(data, !partial);
		}
		catch (final Exception e) {
			removeDownloadSessions(req);
			throw new IOException("Ocurrio un error durante la lectura del fichero de log", e); //$NON-NLS-1$
		}

		if (download.getPosition() > 0L) {
			session.setAttribute("FileDownloadPos", new Long(download.getPosition())); //$NON-NLS-1$
		}

		if (result.isComplete()) {
			removeDownloadSessions(req);
		}

		return result;
	}

	private static final void removeDownloadSessions(final HttpServletRequest request) {
		final HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}

		final Object channelObject = session.getAttribute("ChannelDownload"); //$NON-NLS-1$
		if (channelObject != null && channelObject instanceof SeekableByteChannel) {
			final SeekableByteChannel channel = (SeekableByteChannel) channelObject;
			try {
				channel.close();
			} catch (final IOException e) {
				LOGGER.log(Level.WARNING, "No se pudo cerrar el canal del fichero de log", e); //$NON-NLS-1$
			}
		}

		session.removeAttribute("ChannelDownload"); //$NON-NLS-1$
		session.removeAttribute("Download"); //$NON-NLS-1$
		session.removeAttribute("FileDownloadPos"); //$NON-NLS-1$
	}
}
