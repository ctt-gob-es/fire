/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.gob.fire.logs.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.ErrorManager;
import java.util.logging.LogRecord;

/**
 * Manejador de logs con guardado a fichero diario.
 */
public class DailyFileHandler extends FileHandler {

	private static final String SUFFIX = "_yyyy-MM-dd"; //$NON-NLS-1$

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(SUFFIX);
    private static final TimeZone TIMEZONE = TimeZone.getDefault();

    static {
    	DATE_FORMATTER.setTimeZone(TIMEZONE);
    }

    private final String originalFilename;

    private long nextRollover;

    /**
     * Construct a new instance with the given output file.
     *
     * @param fileName the file name
     *
     * @throws java.io.FileNotFoundException if the file could not be found on open
     */
    public DailyFileHandler(final String fileName) throws FileNotFoundException {
        super(fileName, true);
        this.originalFilename = fileName;
        this.nextRollover = 0;
    }

    @Override
    public void setFile(final File file) throws FileNotFoundException {
        synchronized (this.outputLock) {

        	File newFile = null;
        	if (file != null) {
        		final String suffix = calcNextSuffix();
        		newFile = new File(file.getParentFile(), addSuffix(file.getName(), suffix));
        	}

        	super.setFile(newFile);
        	if (newFile != null && newFile.lastModified() > 0) {
        		calcNextRollover(newFile.lastModified());
        	}
        }
    }

    /** {@inheritDoc}  This implementation checks to see if the scheduled rollover time has yet occurred. */
    @Override
	protected void preWrite(final LogRecord record) {
        final long recordMillis = record.getMillis();
        if (recordMillis >= this.nextRollover) {
            rollOver();
            calcNextRollover(recordMillis);
        }
    }

    /**
     * Returns the suffix to be used.
     *
     * @return the suffix to be used
     */
    private final static String calcNextSuffix() {
        return DATE_FORMATTER.format(new Date());
    }

    private void rollOver() {
        try {
            // close the current file
            setFile(null);
            // start new file
            setFile(new File(this.originalFilename));
        } catch (final IOException e) {
            reportError("Unable to rotate log file", e, ErrorManager.OPEN_FAILURE); //$NON-NLS-1$
        }
    }

    private void calcNextRollover(final long fromTime) {

    	final Calendar calendar = Calendar.getInstance(TIMEZONE);
        calendar.setTimeInMillis(fromTime);

        calendar.set(Calendar.HOUR_OF_DAY, 0);

        //This should ensure the hour is truly zeroed out
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // increment the relevant field
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        this.nextRollover = calendar.getTimeInMillis();
    }

    /**
     * Agrega un sufijo a un nombre de fichero, coloc&aacute;ndolo entre el nombre del fichero
     * y su extensi&oacute;n. Si no se localiza la extensi&oacute;n, se agreg&aacute;n al final
     * del nombre. Si no se indica un sufijo, se agrega la fecha.
     * @param filename Ruta del fichero.
     * @param suffix Sufijo a agregar.
     * @return Ruta del fichero con el sufijo agregado.
     */
    private static String addSuffix(final String filename, final String suffix) {

    	final String newSuffix = suffix != null ?
    			suffix :
    			DATE_FORMATTER.format(new Date());

    	String currentFilename;
    	final int extDotPos = filename.indexOf('.');
    	if (extDotPos > 0) {
    		currentFilename =
    				filename.substring(0,  extDotPos) +
    				newSuffix +
    				filename.substring(extDotPos);
    	}
    	else {
    		currentFilename = filename + newSuffix;
    	}

    	return currentFilename;
    }
}
