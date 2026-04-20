package es.gob.fire.server.services.internal.sessions;

import java.io.File;
import java.io.IOException;

public class FileSystemUtils {


    /**
     * Comprueba que el nombre de fichero indicado sea v&aacute;lido.
     * @param baseDir Directorio base en el que debe encontrarse el fichero.
     * @param filename Nombre de fichero.
     * @return Fichero v&aacute;lidado.
     * @throws IOException Cuando no se ha indicado un nombre de fichero v&aacute;lido
     * o si no se ha podido validar.
     */
    static File checkFile(final File baseDir, final String filename) throws IOException {
    	if (filename == null || filename.isEmpty()) {
            throw new IOException(
                    "El nombre del fichero a recuperar no puede ser nulo" //$NON-NLS-1$
            );
        }
        final File f = new File(baseDir, filename);
        try {
        	if (!f.getCanonicalPath().startsWith(baseDir.getCanonicalPath())) {
        		throw new IOException("Se ha intentado acceder a una ruta fuera del directorio de logs: " + f.getAbsolutePath()); //$NON-NLS-1$
        	}
        }
        catch (final Exception e) {
        	throw new IOException("No se ha podido validar la ruta del fichero: " + f.getAbsolutePath(), e); //$NON-NLS-1$
        }

        return f;
    }
}
