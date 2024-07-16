package es.gob.fire.server.services.internal.sessions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.fire.server.services.internal.LogTransactionFormatter;
import es.gob.fire.signature.ConfigManager;

public class FileSystemTempDocumentsDAO implements TempDocumentsDAO {

	private static final Logger LOGGER = Logger.getLogger(FileSystemTempDocumentsDAO.class.getName());

    private static final String DEFAULT_PREFIX = "fire-"; //$NON-NLS-1$

    private static final int MAX_FILENAME_SIZE = 80;

    private static File TMPDIR;

    static {

        final String defaultDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$

        try {
            final String tmpDir = ConfigManager.getTempDir();
            final File f = tmpDir != null && tmpDir.trim().length() > 0 ? new File(tmpDir.trim()) : null;

            // Si no existe el directorio configurado, tratamos de crearlo
            if (f != null && !f.exists()) {
    			// Creamos el directorio con permisos para despues poder crear subdirectorios desde la propia aplicacion
    			try {
    				final Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rwxrwxrwx"); //$NON-NLS-1$
    				final FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(ownerWritable);
    				Files.createDirectories(f.toPath(), permissions);
    			}
    			catch (final Exception e) {
    				f.mkdirs();
    			}
    			LOGGER.warning("No se encontro el directorio de guardado de temporales y se tratara de crear: " + f.getAbsolutePath()); //$NON-NLS-1$
            }

            if (f == null || !f.isDirectory()) {
                LOGGER.severe(
                		"El directorio temporal configurado (" + //$NON-NLS-1$
                		(f != null ? f.getAbsolutePath() : null) +
                		") no es valido, se usara el por defecto: " + defaultDir); //$NON-NLS-1$
                TMPDIR = new File(defaultDir);
            } else if (!f.canRead() || !f.canWrite()) {
            	LOGGER.severe(
                		"El directorio temporal configurado (" + f.getAbsolutePath() + //$NON-NLS-1$
                		") no tiene permiso de lectura/escritura, se usara el por defecto: " + defaultDir); //$NON-NLS-1$
            	TMPDIR = new File(defaultDir);
            } else {
                LOGGER.info("Se usara el directorio temporal configurado: " + f.getAbsolutePath()); //$NON-NLS-1$
                TMPDIR = f;
            }
        }
        catch (final Exception e) {
        	LOGGER.log(Level.SEVERE, "No se ha podido cargar la configuracion del modulo", e); //$NON-NLS-1$
        	LOGGER.warning("Se usara el directorio temporal por defecto: " + defaultDir); //$NON-NLS-1$
        	TMPDIR = new File(defaultDir);
        }
    }


	@Override
	public boolean existDocument(final String filename) throws IOException {
		return checkFile(filename).isFile();
	}


    /**
     * Almacena datos en el directorio temporal.
     * @param filename Nombre del fichero en el que se almacenar&aacute;n los datos.
     * 			Si se indica null, se generar&aacute; un nombre aleatorio
     * @param data Datos a almacenar.
     * @return Nombre final del fichero almacenado.
     * @throws IOException Cuando ocurre un error durante el guardado.
     */
    @Override
    public String storeDocument(final String filename, final byte[] data, final boolean newDocument, final LogTransactionFormatter formt)
    		throws IOException {

        if (data == null || data.length < 1) {
            throw new IOException(
                    "Los datos a guardar no pueden ser nulos ni vacios" //$NON-NLS-1$
            );
        }

        final File f = filename != null
        	? checkFile(filename)
        	: File.createTempFile(DEFAULT_PREFIX, null, TMPDIR);

        try (final OutputStream fos = new FileOutputStream(f);
        		final OutputStream bos = new BufferedOutputStream(fos);) {
        	bos.write(data);
        }

        LOGGER.fine(formt.f("Almacenado temporal de datos en: " + f.getAbsolutePath())); //$NON-NLS-1$

        return f.getName();
    }

    /**
     * Lee el contenido de un documento guardado en un fichero situado en un directorio
     * concreto.
     * @param filename Nombre del fichero.
     * @return Contenido del fichero.
     * @throws IOException Cuando no se encuentra el fichero o no puede leerse.
     */
    @Override
	public byte[] retrieveDocument(final String filename) throws IOException {
        final File dataFile = checkFile(filename);
        return readFile(dataFile);
    }

    private static byte[] readFile(final File dataFile) throws IOException {

    	byte[] ret;
    	try (final InputStream fis = new FileInputStream(dataFile);
        	final InputStream bis = new BufferedInputStream(fis); ) {
        	ret = AOUtil.getDataFromInputStream(bis);
    	}

        return ret;
    }

    /**
     * Elimina un fichero situado en un directorio
     * concreto.
     * @param filename Nombre del fichero.
     * @throws IOException Cuando no se puede eliminar el fichero.
     */
    @Override
    public void deleteDocument(final String filename) throws IOException {

    	final File f = checkFile(filename);

    	Files.deleteIfExists(f.toPath());
    }


    /**
     * Lee el contenido de un documento guardado en un fichero situado en un directorio
     * concreto y despu&eacute;s lo elimina.
     * @param filename Nombre del fichero.
     * @return Contenido del fichero.
     * @throws IOException Cuando no se encuentra el fichero o no puede leerse.
     */
    @Override
	public byte[] retrieveAndDeleteDocument(final String filename) throws IOException {

    	final File dataFile = checkFile(filename);
    	final byte[] ret = readFile(dataFile);
    	Files.delete(dataFile.toPath());

        return ret;
    }

    /**
     * Recorre el directorio temporal eliminando los ficheros que hayan sobrepasado el tiempo
     * indicado sin haber sido modificados.
     * @param timeout Tiempo en milisegundos que debe haber transcurrido desde la &uacute;ltima
     * modificaci&oacute;n de un fichero para considerarse caducado.
     */
    @Override
	public void deleteExpiredDocuments(final long timeout) {

    	for (final File tempFile : TMPDIR.listFiles(new ExpiredFileFilter(timeout))) {
    		try {
    			Files.delete(tempFile.toPath());
    		}
    		catch (final Exception e) {
    			LOGGER.warning("No se pudo eliminar el fichero caducado " + tempFile.getAbsolutePath() + //$NON-NLS-1$
    					": " + e); //$NON-NLS-1$
    		}
    	}
    }

    /**
     * Comprueba que el nombre de fichero indicado sea v&aacute;lido.
     * @param filename Nombre de fichero.
     * @return Fichero v&aacute;lidado.
     * @throws IOException Cuando no se ha indicado un nombre de fichero v&aacute;lido
     * o si no se ha podido validar.
     */
    private static File checkFile(final String filename) throws IOException {
    	if (filename == null || filename.isEmpty()) {
            throw new IOException(
                    "El nombre del fichero a recuperar no puede ser nulo" //$NON-NLS-1$
            );
        }
        final File f = new File(TMPDIR, cleanFileName(filename));
        try {
        	if (!f.getCanonicalPath().startsWith(TMPDIR.getCanonicalPath())) {
        		throw new IOException("Se ha intentado acceder a una ruta fuera del directorio de logs: " + f.getAbsolutePath()); //$NON-NLS-1$
        	}
        }
        catch (final Exception e) {
        	throw new IOException("No se ha podido validar la ruta del fichero: " + f.getAbsolutePath(), e); //$NON-NLS-1$
        }

        return f;
    }

    /**
     * Limpia un nombre de fichero para asegurar que no haya caracteres con los que no puedan
     * guardarse los ficheros en disco y lo recorta a un tama&ntilde;o m&aacute;ximo.
     * @param filename Nombre de fichero.
     * @return Nombre de fichero limpio.
     */
    private static String cleanFileName(final String filename) {

    	// Componemos un nombre de hasta 64 caracters con caracteres validos para nombres de fichero
    	final StringBuilder builder = new StringBuilder();
    	for (final char c : filename.toCharArray()) {
    		if (Character.isLetterOrDigit(c) || c == '.' || c == '-') {
    			builder.append(c);
    		} else {
    			builder.append('_');
    		}
    		if (builder.length() >= MAX_FILENAME_SIZE) {
    			break;
    		}
    	}
    	return builder.toString();
    }
}
