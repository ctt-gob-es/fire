/*
 * Este fichero forma parte de la plataforma de @firma.
 * La plataforma de @firma es de libre distribuci&oacute;n cuyo c&oacute;digo fuente puede ser consultado
 * y descargado desde http://administracionelectronica.gob.es
 *
 * Copyright 2005-2019 Gobierno de España
 * Este fichero se distribuye bajo las licencias EUPL versi&oacute;n 1.1, seg&uacute;n las
 * condiciones que figuran en el fichero 'LICENSE.txt' que se acompaña.  Si se   distribuyera este
 * fichero individualmente, deben incluirse aqu&iacute; las condiciones expresadas all&iacute;.
 */

/**
 * <b>File:</b><p>es.gob.fire.persistence.entity.Scheduler.java.</p>
 * <b>Description:</b><p>Class that represents the representation of the <i>SCHEDULER</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.1, 12/02/2025.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that represents the representation of the <i>SCHEDULER</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.1, 12/02/2025.
 */
@Entity
@Table(name = "TB_PROGRAMADOR")
public class Scheduler implements Serializable {

    /**
     * Versi&oacute;n serial de la clase.
     */
    private static final long serialVersionUID = -6177818765623010960L;

    /**
     * Atributo que representa el ID del objeto.
     */
    private Long idScheduler;

    /**
     * Atributo que representa el nombre del token con la descripci&oacute;n almacenada en el archivo de propiedades para internacionalizaci&oacute;n.
     */
    private String tokenName;

    /**
     * Atributo que representa el nombre de la clase que implementa el programador.
     */
    private String className;

    /**
     * Atributo que indica si el programador est&aacute; activo (true) o no (false).
     */
    private Boolean active;

    /**
     * Atributo que representa el planificador asociado.
     */
    private Planner planner;

    /**
     * Atributo que representa el n&uacute;mero de hilos en paralelo que se lanzar&aacute;n en cada proceso, en milisegundos.
     */
    private Long numThreads;

    /**
     * Atributo que representa el n&uacute;mero de procesos asincr&oacute;nicos que se evaluar&aacute;n por hilo, en milisegundos.
     */
    private Long numProcess;

    /**
     * Atributo que representa el periodo en el que una solicitud asincr&oacute;nica se considera expirada, en milisegundos.
     */
    private Long expiredPeriod;

    /**
     * Atributo que representa el periodo en el que se considera que un nodo no est&aacute; activo, en milisegundos.
     */
    private Long reassingTime;

    /**
     * Atributo que representa el tiempo en el cual deber&iacute;a haber terminado un proceso asincr&oacute;nico, en milisegundos.
     */
    private Long reactiveTime;

    /**
     * Atributo que representa el periodo en el que se considera que una solicitud consultada debe ser eliminada, en milisegundos.
     */
    private Long checkedTime;

    /**
     * Atributo que representa el nombre del programador.
     */
    private String schedulerName;

    /**
	 * Attribute that represents the day of the advance notice.
	 */
	private Long advanceNotice;

	/**
	 * Attribute that represents the day of the period communication.
	 */
	private Long periodCommunication;

    /**
     * Obtiene el valor del atributo {@link #idScheduler}.
     * @return el valor del atributo {@link #idScheduler}.
     */
    @Id
    @Column(name = "ID_PROGRAMADOR", unique = true, nullable = false, precision = NumberConstants.NUM19)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tb_programador_seq")
    @TableGenerator( name = "tb_programador_seq", table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "next_val", pkColumnValue = "programador_gen", allocationSize = 1 )
    public Long getIdScheduler() {
        return this.idScheduler;
    }

    /**
     * Establece el valor del atributo {@link #idScheduler}.
     * @param idSchedulerParam El valor para el atributo {@link #idScheduler}.
     */
    public void setIdScheduler(final Long idSchedulerParam) {
        this.idScheduler = idSchedulerParam;
    }

    /**
     * Obtiene el valor del atributo {@link #tokenName}.
     * @return el valor del atributo {@link #tokenName}.
     */
    @Column(name = "NOMBRE_TOKEN", nullable = false, length = NumberConstants.NUM30)
    public String getTokenName() {
        return this.tokenName;
    }

    /**
     * Establece el valor del atributo {@link #tokenName}.
     * @param tokenNameParam El valor para el atributo {@link #tokenName}.
     */
    public void setTokenName(final String tokenNameParam) {
        this.tokenName = tokenNameParam;
    }

    /**
     * Obtiene el valor del atributo {@link #className}.
     * @return el valor del atributo {@link #className}.
     */
    @Column(name = "NOMBRE_CLASE", nullable = false, length = NumberConstants.NUM255)
    public String getClassName() {
        return this.className;
    }

    /**
     * Establece el valor del atributo {@link #className}.
     * @param classNameParam El valor para el atributo {@link #className}.
     */
    public void setClassName(final String classNameParam) {
        this.className = classNameParam;
    }

    /**
     * Obtiene el valor del atributo {@link #active}.
     * @return el valor del atributo {@link #active}.
     */
    @Column(name = "ESTA_ACTIVO", nullable = false)
    public boolean isActive() {
        return this.active;
    }

    /**
     * Establece el valor del atributo {@link #active}.
     * @param active El valor para el atributo {@link #active}.
     */
    public void setActive(final Boolean active) {
        this.active = active;
    }

    /**
     * Obtiene el valor del atributo {@link #planner}.
     * @return el valor del atributo {@link #planner}.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PLANIFICADOR", nullable = false)
    public Planner getPlanner() {
        return this.planner;
    }

    /**
     * Establece el valor del atributo {@link #planner}.
     * @param plannerParam El valor para el atributo {@link #planner}.
     */
    public void setPlanner(final Planner plannerParam) {
        this.planner = plannerParam;
    }

    /**
     * Obtiene el valor del atributo {@link #numThreads}.
     * @return el valor del atributo {@link #numThreads}.
     */
    @Column(name = "NUM_HILOS", precision = NumberConstants.NUM19)
    public Long getNumThreads() {
        return this.numThreads;
    }

    /**
     * Establece el valor del atributo {@link #numThreads}.
     * @param numThreadsParam El valor para el atributo {@link #numThreads}.
     */
    public void setNumThreads(final Long numThreadsParam) {
        this.numThreads = numThreadsParam;
    }

    /**
     * Obtiene el valor del atributo {@link #numProcess}.
     * @return el valor del atributo {@link #numProcess}.
     */
    @Column(name = "NUM_PROCESOS", precision = NumberConstants.NUM19)
    public Long getNumProcess() {
        return this.numProcess;
    }

    /**
     * Establece el valor del atributo {@link #numProcess}.
     * @param numProcessParam El valor para el atributo {@link #numProcess}.
     */
    public void setNumProcess(final Long numProcessParam) {
        this.numProcess = numProcessParam;
    }

    /**
     * Obtiene el valor del atributo {@link #expiredPeriod}.
     * @return el valor del atributo {@link #expiredPeriod}.
     */
    @Column(name = "PERIODO_EXPIRADO", precision = NumberConstants.NUM19)
    public Long getExpiredPeriod() {
        return this.expiredPeriod;
    }

    /**
     * Establece el valor del atributo {@link #expiredPeriod}.
     * @param expiredPeriodParam El valor para el atributo {@link #expiredPeriod}.
     */
    public void setExpiredPeriod(final Long expiredPeriodParam) {
        this.expiredPeriod = expiredPeriodParam;
    }

    /**
     * Obtiene el valor del atributo {@link #reassingTime}.
     * @return el valor del atributo {@link #reassingTime}.
     */
    @Column(name = "TIEMPO_REASIGNACION", precision = NumberConstants.NUM19)
    public Long getReassingTime() {
        return this.reassingTime;
    }

    /**
     * Establece el valor del atributo {@link #reassingTime}.
     * @param reassingTimeParam El valor para el atributo {@link #reassingTime}.
     */
    public void setReassingTime(final Long reassingTimeParam) {
        this.reassingTime = reassingTimeParam;
    }

    /**
     * Obtiene el valor del atributo {@link #reactiveTime}.
     * @return el valor del atributo {@link #reactiveTime}.
     */
    @Column(name = "TIEMPO_REACTIVACION", precision = NumberConstants.NUM19)
    public Long getReactiveTime() {
        return this.reactiveTime;
    }

    /**
     * Establece el valor del atributo {@link #reactiveTime}.
     * @param reactiveTimeParam El valor para el atributo {@link #reactiveTime}.
     */
    public void setReactiveTime(final Long reactiveTimeParam) {
        this.reactiveTime = reactiveTimeParam;
    }

    /**
     * Obtiene el valor del atributo {@link #checkedTime}.
     * @return el valor del atributo {@link #checkedTime}.
     */
    @Column(name = "TIEMPO_COMPROBACION", precision = NumberConstants.NUM19)
    public Long getCheckedTime() {
        return this.checkedTime;
    }

    /**
     * Establece el valor del atributo {@link #checkedTime}.
     * @param checkedTimeParam El valor para el atributo {@link #checkedTime}.
     */
    public void setCheckedTime(final Long checkedTimeParam) {
        this.checkedTime = checkedTimeParam;
    }

    /**
     * Obtiene el valor del atributo {@link #schedulerName}.
     * @return el valor del atributo {@link #schedulerName}.
     */
    @Column(name = "NOMBRE_PROGRAMADOR", nullable = false, length = NumberConstants.NUM50)
    public String getSchedulerName() {
        return this.schedulerName;
    }

    /**
     * Establece el valor del atributo {@link #schedulerName}.
     * @param schedulerNameParam El valor para el atributo {@link #schedulerName}.
     */
    public void setSchedulerName(final String schedulerNameParam) {
        this.schedulerName = schedulerNameParam;
    }

    /**
	 * Gets the value of the attribute {@link #advanceNotice}.
	 * @return the value of the attribute {@link #advanceNotice}.
	 */
	@Column(name = "DIAS_PREAVISO", nullable = true, length = NumberConstants.NUM19)
	public Long getAdvanceNotice() {
		return this.advanceNotice;
	}

	/**
	 * Sets the value of the attribute {@link #advanceNotice}.
	 * @param taskParam The value for the attribute {@link #advanceNotice}.
	 */
	public void setAdvanceNotice(final Long advanceNotice) {
		this.advanceNotice = advanceNotice;
	}

	/**
	 * Gets the value of the attribute {@link #periodCommunication}.
	 * @return the value of the attribute {@link #periodCommunication}.
	 */
	@Column(name = "PERIODO_COMUNICACION", nullable = true, length = NumberConstants.NUM19)
	public Long getPeriodCommunication() {
		return this.periodCommunication;
	}

	/**
	 * Sets the value of the attribute {@link #periodCommunication}.
	 * @param periodCommunication The value for the attribute {@link #periodCommunication}.
	 */
	public void setPeriodCommunication(final Long periodCommunication) {
		this.periodCommunication = periodCommunication;
	}

}
