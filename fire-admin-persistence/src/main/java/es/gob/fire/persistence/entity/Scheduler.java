/*
 * Este fichero forma parte de la plataforma de @firma.
 * La plataforma de @firma es de libre distribución cuyo código fuente puede ser consultado
 * y descargado desde http://administracionelectronica.gob.es
 *
 * Copyright 2005-2019 Gobierno de España
 * Este fichero se distribuye bajo las licencias EUPL versión 1.1, según las
 * condiciones que figuran en el fichero 'LICENSE.txt' que se acompaña.  Si se   distribuyera este
 * fichero individualmente, deben incluirse aquí las condiciones expresadas allí.
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
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

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
     * Versión serial de la clase.
     */
    private static final long serialVersionUID = -6177818765623010960L;

    /**
     * Atributo que representa el ID del objeto.
     */
    private Long idScheduler;

    /**
     * Atributo que representa el nombre del token con la descripción almacenada en el archivo de propiedades para internacionalización.
     */
    private String tokenName;

    /**
     * Atributo que representa el nombre de la clase que implementa el programador.
     */
    private String className;

    /**
     * Atributo que indica si el programador está activo (true) o no (false).
     */
    private Boolean isActive;

    /**
     * Atributo que representa el planificador asociado.
     */
    private Planner planner;

    /**
     * Atributo que representa el número de hilos en paralelo que se lanzarán en cada proceso, en milisegundos.
     */
    private Long numThreads;

    /**
     * Atributo que representa el número de procesos asincrónicos que se evaluarán por hilo, en milisegundos.
     */
    private Long numProcess;

    /**
     * Atributo que representa el período en el que una solicitud asincrónica se considera expirada, en milisegundos.
     */
    private Long expiredPeriod;

    /**
     * Atributo que representa el período en el que se considera que un nodo no está activo, en milisegundos.
     */
    private Long reassingTime;

    /**
     * Atributo que representa el tiempo en el cual debería haber terminado un proceso asincrónico, en milisegundos.
     */
    private Long reactiveTime;

    /**
     * Atributo que representa el período en el que se considera que una solicitud consultada debe ser eliminada, en milisegundos.
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
    @GeneratedValue(generator = "tb_programador_seq")
	@GenericGenerator(name = "tb_programador_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "TB_PROGRAMADOR_SEQ"), @Parameter(name = "initial_value", value = "1"), @Parameter(name = "increment_size", value = "1") })
    public Long getIdScheduler() {
        return idScheduler;
    }

    /**
     * Establece el valor del atributo {@link #idScheduler}.
     * @param idSchedulerParam El valor para el atributo {@link #idScheduler}.
     */
    public void setIdScheduler(Long idSchedulerParam) {
        this.idScheduler = idSchedulerParam;
    }

    /**
     * Obtiene el valor del atributo {@link #tokenName}.
     * @return el valor del atributo {@link #tokenName}.
     */
    @Column(name = "NOMBRE_TOKEN", nullable = false, length = NumberConstants.NUM30)
    public String getTokenName() {
        return tokenName;
    }

    /**
     * Establece el valor del atributo {@link #tokenName}.
     * @param tokenNameParam El valor para el atributo {@link #tokenName}.
     */
    public void setTokenName(String tokenNameParam) {
        this.tokenName = tokenNameParam;
    }

    /**
     * Obtiene el valor del atributo {@link #className}.
     * @return el valor del atributo {@link #className}.
     */
    @Column(name = "NOMBRE_CLASE", nullable = false, length = NumberConstants.NUM255)
    public String getClassName() {
        return className;
    }

    /**
     * Establece el valor del atributo {@link #className}.
     * @param classNameParam El valor para el atributo {@link #className}.
     */
    public void setClassName(String classNameParam) {
        this.className = classNameParam;
    }

    /**
     * Obtiene el valor del atributo {@link #isActive}.
     * @return el valor del atributo {@link #isActive}.
     */
    @Column(name = "ESTA_ACTIVO", nullable = false, precision = 1)
    @Type(type = "yes_no")
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * Establece el valor del atributo {@link #isActive}.
     * @param isActiveParam El valor para el atributo {@link #isActive}.
     */
    public void setIsActive(Boolean isActiveParam) {
        this.isActive = isActiveParam;
    }

    /**
     * Obtiene el valor del atributo {@link #planner}.
     * @return el valor del atributo {@link #planner}.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PLANIFICADOR", nullable = false)
    public Planner getPlanner() {
        return planner;
    }

    /**
     * Establece el valor del atributo {@link #planner}.
     * @param plannerParam El valor para el atributo {@link #planner}.
     */
    public void setPlanner(Planner plannerParam) {
        this.planner = plannerParam;
    }

    /**
     * Obtiene el valor del atributo {@link #numThreads}.
     * @return el valor del atributo {@link #numThreads}.
     */
    @Column(name = "NUM_HILOS", precision = NumberConstants.NUM19)
    public Long getNumThreads() {
        return numThreads;
    }

    /**
     * Establece el valor del atributo {@link #numThreads}.
     * @param numThreadsParam El valor para el atributo {@link #numThreads}.
     */
    public void setNumThreads(Long numThreadsParam) {
        this.numThreads = numThreadsParam;
    }

    /**
     * Obtiene el valor del atributo {@link #numProcess}.
     * @return el valor del atributo {@link #numProcess}.
     */
    @Column(name = "NUM_PROCESOS", precision = NumberConstants.NUM19)
    public Long getNumProcess() {
        return numProcess;
    }

    /**
     * Establece el valor del atributo {@link #numProcess}.
     * @param numProcessParam El valor para el atributo {@link #numProcess}.
     */
    public void setNumProcess(Long numProcessParam) {
        this.numProcess = numProcessParam;
    }

    /**
     * Obtiene el valor del atributo {@link #expiredPeriod}.
     * @return el valor del atributo {@link #expiredPeriod}.
     */
    @Column(name = "PERIODO_EXPIRADO", precision = NumberConstants.NUM19)
    public Long getExpiredPeriod() {
        return expiredPeriod;
    }

    /**
     * Establece el valor del atributo {@link #expiredPeriod}.
     * @param expiredPeriodParam El valor para el atributo {@link #expiredPeriod}.
     */
    public void setExpiredPeriod(Long expiredPeriodParam) {
        this.expiredPeriod = expiredPeriodParam;
    }

    /**
     * Obtiene el valor del atributo {@link #reassingTime}.
     * @return el valor del atributo {@link #reassingTime}.
     */
    @Column(name = "TIEMPO_REASIGNACION", precision = NumberConstants.NUM19)
    public Long getReassingTime() {
        return reassingTime;
    }

    /**
     * Establece el valor del atributo {@link #reassingTime}.
     * @param reassingTimeParam El valor para el atributo {@link #reassingTime}.
     */
    public void setReassingTime(Long reassingTimeParam) {
        this.reassingTime = reassingTimeParam;
    }

    /**
     * Obtiene el valor del atributo {@link #reactiveTime}.
     * @return el valor del atributo {@link #reactiveTime}.
     */
    @Column(name = "TIEMPO_REACTIVACION", precision = NumberConstants.NUM19)
    public Long getReactiveTime() {
        return reactiveTime;
    }

    /**
     * Establece el valor del atributo {@link #reactiveTime}.
     * @param reactiveTimeParam El valor para el atributo {@link #reactiveTime}.
     */
    public void setReactiveTime(Long reactiveTimeParam) {
        this.reactiveTime = reactiveTimeParam;
    }

    /**
     * Obtiene el valor del atributo {@link #checkedTime}.
     * @return el valor del atributo {@link #checkedTime}.
     */
    @Column(name = "TIEMPO_COMPROBACION", precision = NumberConstants.NUM19)
    public Long getCheckedTime() {
        return checkedTime;
    }

    /**
     * Establece el valor del atributo {@link #checkedTime}.
     * @param checkedTimeParam El valor para el atributo {@link #checkedTime}.
     */
    public void setCheckedTime(Long checkedTimeParam) {
        this.checkedTime = checkedTimeParam;
    }

    /**
     * Obtiene el valor del atributo {@link #schedulerName}.
     * @return el valor del atributo {@link #schedulerName}.
     */
    @Column(name = "NOMBRE_PROGRAMADOR", nullable = false, length = NumberConstants.NUM50)
    public String getSchedulerName() {
        return schedulerName;
    }

    /**
     * Establece el valor del atributo {@link #schedulerName}.
     * @param schedulerNameParam El valor para el atributo {@link #schedulerName}.
     */
    public void setSchedulerName(String schedulerNameParam) {
        this.schedulerName = schedulerNameParam;
    }

    /**
	 * Gets the value of the attribute {@link #advanceNotice}.
	 * @return the value of the attribute {@link #advanceNotice}.
	 */
	@Column(name = "DIAS_PREAVISO", nullable = true, length = NumberConstants.NUM19)
	public Long getAdvanceNotice() {
		return advanceNotice;
	}

	/**
	 * Sets the value of the attribute {@link #advanceNotice}.
	 * @param taskParam The value for the attribute {@link #advanceNotice}.
	 */
	public void setAdvanceNotice(Long advanceNotice) {
		this.advanceNotice = advanceNotice;
	}

	/**
	 * Gets the value of the attribute {@link #periodCommunication}.
	 * @return the value of the attribute {@link #periodCommunication}.
	 */
	@Column(name = "PERIODO_COMUNICACION", nullable = true, length = NumberConstants.NUM19)
	public Long getPeriodCommunication() {
		return periodCommunication;
	}

	/**
	 * Sets the value of the attribute {@link #periodCommunication}.
	 * @param periodCommunication The value for the attribute {@link #periodCommunication}.
	 */
	public void setPeriodCommunication(Long periodCommunication) {
		this.periodCommunication = periodCommunication;
	}
	
}
