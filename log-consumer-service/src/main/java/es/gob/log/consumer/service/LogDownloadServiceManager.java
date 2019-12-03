package es.gob.log.consumer.service;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.StandardOpenOption;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.gob.log.consumer.LogDownload;


/**
 * Manejador para la gesti&oacute;n de las peticiones de descarga de fichero.
 */
public class LogDownloadServiceManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogDownloadServiceManager.class);

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

		if ((Long) session.getAttribute(SessionParams.DOWNLOAD_FILE_POS) != null) {
			fileDownloadPosition = (Long) session.getAttribute(SessionParams.DOWNLOAD_FILE_POS);
		}

		SeekableByteChannel channel;
		if ((SeekableByteChannel) session.getAttribute(SessionParams.DOWNLOAD_CHANNEL) != null) {
			channel = (SeekableByteChannel) session.getAttribute(SessionParams.DOWNLOAD_CHANNEL);
		}
		else {
			try {
				channel = FileChannel.open(dataFile.toPath(), StandardOpenOption.READ);
			}
			catch (final Exception e) {
				removeDownloadSessions(req);
				throw new IOException("Ocurrio un error durante la apertura del fichero de log", e);
			}
			session.setAttribute(SessionParams.DOWNLOAD_CHANNEL, channel);
		}

		LogDownload download;
		if ((LogDownload) session.getAttribute(SessionParams.DOWNLOAD_FILE) != null) {
			download = (LogDownload) session.getAttribute(SessionParams.DOWNLOAD_FILE);
		}
		else {
			download =  new LogDownload(logFileName, channel);
			session.setAttribute(SessionParams.DOWNLOAD_FILE, download);
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
			session.setAttribute(SessionParams.DOWNLOAD_FILE_POS, new Long(download.getPosition()));
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

		final Object channelObject = session.getAttribute(SessionParams.DOWNLOAD_CHANNEL);
		if (channelObject != null && channelObject instanceof SeekableByteChannel) {
			final SeekableByteChannel channel = (SeekableByteChannel) channelObject;
			try {
				channel.close();
			} catch (final IOException e) {
				LOGGER.warn("No se pudo cerrar el canal del fichero de log", e); //$NON-NLS-1$
			}
		}

		session.removeAttribute(SessionParams.DOWNLOAD_CHANNEL);
		session.removeAttribute(SessionParams.DOWNLOAD_FILE);
		session.removeAttribute(SessionParams.DOWNLOAD_FILE_POS);
	}
}
