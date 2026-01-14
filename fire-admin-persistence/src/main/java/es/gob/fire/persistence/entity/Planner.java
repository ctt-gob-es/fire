/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de España
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/**
 * <b>File:</b><p>es.gob.fire.persistence.entity.Planner.java.</p>
 * <b>Description:</b><p>Class that represents the representation of the <i>PLANNER</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.2, 12/06/2023.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonView;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that represents the representation of the <i>PLANNER</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.2, 12/06/2023.
 */
@Entity
@Table(name = "TB_PLANIFICADOR")
public class Planner implements Serializable {

    /**
     * Atributo constante que representa el serial version UID.
     */
    private static final long serialVersionUID = 1731493626842035877L;

    /**
     * Atributo que representa el ID del objeto.
     */
    private Long idPlanner;

    /**
     * Atributo que representa las horas asociadas a un período.
     */
    private Long hourPeriod;

    /**
     * Atributo que representa la fecha de inicio del planificador.
     */
    private Date initDay;

    /**
     * Atributo que representa los minutos asociados a un período.
     */
    private Long minutePeriod;

    /**
     * Atributo que representa los segundos asociados a un período.
     */
    private Long secondPeriod;

    /**
     * Atributo que representa el tipo del planificador.
     */
    private CPlannerType plannerType;

    /**
     * Atributo que representa el día del aviso anticipado.
     */
    private Integer advanceNotice;

    /**
     * Obtiene el valor del atributo {@link #idPlanner}.
     * @return el valor del atributo {@link #idPlanner}.
     */
    @Id
    @Column(name = "ID_PLANIFICADOR", unique = true, nullable = false, precision = NumberConstants.NUM19)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tb_planificador_seq")
    @TableGenerator( name = "tb_planificador_seq", table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "next_val", pkColumnValue = "planificador_gen", allocationSize = 1 )
    @JsonView(DataTablesOutput.View.class)

    public Long getIdPlanner() {
        return this.idPlanner;
    }

    /**
     * Establece el valor del atributo {@link #idPlanner}.
     * @param idPlannerParam El valor para el atributo {@link #idPlanner}.
     */
    public void setIdPlanner(final Long idPlannerParam) {
        this.idPlanner = idPlannerParam;
    }

    /**
     * Obtiene el valor del atributo {@link #hourPeriod}.
     * @return el valor del atributo {@link #hourPeriod}.
     */
    @Column(name = "HORA_PERIODO")
    public Long getHourPeriod() {
        return this.hourPeriod;
    }

    /**
     * Establece el valor del atributo {@link #hourPeriod}.
     * @param hourPeriodParam El valor para el atributo {@link #hourPeriod}.
     */
    public void setHourPeriod(final Long hourPeriodParam) {
        this.hourPeriod = hourPeriodParam;
    }

    /**
     * Obtiene el valor del atributo {@link #initDay}.
     * @return el valor del atributo {@link #initDay}.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DIA_INICIO")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    public Date getInitDay() {
        return this.initDay;
    }

    /**
     * Establece el valor del atributo {@link #initDay}.
     * @param initDayParam El valor para el atributo {@link #initDay}.
     */
    public void setInitDay(final Date initDayParam) {
        this.initDay = initDayParam;
    }

    /**
     * Obtiene el valor del atributo {@link #minutePeriod}.
     * @return el valor del atributo {@link #minutePeriod}.
     */
    @Column(name = "MINUTO_PERIODO")
    public Long getMinutePeriod() {
        return this.minutePeriod;
    }

    /**
     * Establece el valor del atributo {@link #minutePeriod}.
     * @param minutePeriodParam El valor para el atributo {@link #minutePeriod}.
     */
    public void setMinutePeriod(final Long minutePeriodParam) {
        this.minutePeriod = minutePeriodParam;
    }

    /**
     * Obtiene el valor del atributo {@link #secondPeriod}.
     * @return el valor del atributo {@link #secondPeriod}.
     */
    @Column(name = "SEGUNDO_PERIODO")
    public Long getSecondPeriod() {
        return this.secondPeriod;
    }

    /**
     * Establece el valor del atributo {@link #secondPeriod}.
     * @param secondPeriodParam El valor para el atributo {@link #secondPeriod}.
     */
    public void setSecondPeriod(final Long secondPeriodParam) {
        this.secondPeriod = secondPeriodParam;
    }

    /**
     * Obtiene el valor del atributo {@link #plannerType}.
     * @return el valor del atributo {@link #plannerType}.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TIPO_PLANIFICADOR", nullable = false)
    public CPlannerType getPlannerType() {
        return this.plannerType;
    }

    /**
     * Establece el valor del atributo {@link #plannerType}.
     * @param plannerTypeParam El valor para el atributo {@link #plannerType}.
     */
    public void setPlannerType(final CPlannerType plannerTypeParam) {
        this.plannerType = plannerTypeParam;
    }

    /**
     * Obtiene el valor del atributo {@link #advanceNotice}.
     * @return el valor del atributo {@link #advanceNotice}.
     */
    @Column(name = "AVISO_ANTICIPADO")
    public Integer getAdvanceNotice() {
        return this.advanceNotice;
    }

    /**
     * Establece el valor del atributo {@link #advanceNotice}.
     * @param advanceNoticeParam El valor para el atributo {@link #advanceNotice}.
     */
    public void setAdvanceNotice(final Integer advanceNoticeParam) {
        this.advanceNotice = advanceNoticeParam;
    }

}
