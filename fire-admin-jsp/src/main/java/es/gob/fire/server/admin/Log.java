/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.admin;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log de la base de datos
 * @author mario
 */
public class Log {

    private int id;

    private long fecha;

    private String levelError;

    private String timeStam;

    private String sourceClassName;

    private String sourceMethod;

    private String message;

    private String throwable;

    /**
     * Devuelve el Id del log.
     * @return Id de log
     */
    public int getId() {
        return this.id;
    }

    /**
     * Establece el identificador del log.
     * @param id Identificador del log.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Devuelve la fecha en milisegundos.
     * @return La fecha en milisegundos.
     */
    public long getFecha() {
        return this.fecha;
    }

    /**
     * Establece la fecha en milisegundos.
     * @param fecha La fecha en milisegundos.
     */
    public void setFecha(final long fecha) {
        this.fecha = fecha;
    }

    /**
     * Devuelve el nivel de error de un log.
     * @return El nivel de error de un log.
     */
    public String getLevelError() {
        return this.levelError;
    }

    /**
     * Establece el nivel de error de un log.
     * @param levelError El nivel de error de un log.
     */
    public void setLevelError(final String levelError) {
        this.levelError = levelError;
    }

    /**
     * Devuelve el timestam de un log.
     * @return El timestam de un log.
     */
    public String getTimeStam() {
        return this.timeStam;
    }

    /**
     * Establece el timestam de un log.
     * @param timeStam El timestam de un log.
     */
    public void setTimeStam(final String timeStam) {
        this.timeStam = timeStam;
    }

    /**
     * Devuelve cual es la clase fuente.
     * @return Clase fuente.
     */
    public String getSourceClassName() {
        return this.sourceClassName;
    }

    /**
     * Asigna la clase fuente.
     * @param sourceClassName Clase fuente.
     */
    public void setSourceClassName(final String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    /**
     * Devuelve el metodo fuente.
     * @return El metodo fuente.
     */
    public String getSourceMethod() {
        return this.sourceMethod;
    }

    /**
     * Establece el metodo fuente.
     * @param socurceMethod El metodo fuente.
     */
    public void setSourceMethod(final String socurceMethod) {
        this.sourceMethod = socurceMethod;
    }

    /**
     * Devuelve si es throwable.
     * @return Si es throwable.
     */
    public String getThrowable() {
        return this.throwable;
    }

    /**
     * Establece si es throwable.
     * @param throwable Si es throwable.
     */
    public void setThrowable(final String throwable) {
        this.throwable = throwable;
    }

    /**
     * Devuelve el mensaje del log.
     * @return El mensaje del log.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Asigna el mensaje del log
     * @param message El mensaje del log
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Devuelve un formato de fecha "dd-MM-yyyy HH:mm:ss" dado una fecha en milisegundos
     * @param milisegundos Milisegundos que determinan la fecha.
     * @return Fecha en format "dd-MM-yyyy HH:mm:ss"
     */
    public static String getFechaS(final long milisegundos){
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(milisegundos)); //$NON-NLS-1$
    }




}
