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
 * <b>File:</b><p>es.gob.fire.persistence.entity.CPlannerType.java.</p>
 * <b>Description:</b><p> Class that represents the representation of the <i>C_PLANNER_TYPE</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/05/2020.
 */
package es.gob.fire.persistence.entity;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import es.gob.fire.commons.utils.NumberConstants;

/**
 * <p>Class that represents the representation of the <i>C_PLANNER_TYPE</i> database table as a Plain Old Java Object.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.0, 15/05/2020.
 */
@Cacheable
@Entity
@Table(name = "TB_TIPO_PLANIFICADOR")
public class CPlannerType implements Serializable {

    /**
     * Atributo constante que representa el serial version UID.
     */
    private static final long serialVersionUID = 3710288918662199270L;

    /**
     * Atributo que representa el ID del objeto.
     */
    private Long idPlannerType;

    /**
     * Atributo que representa el nombre del token con la descripción almacenada en el archivo de propiedades para internacionalización.
     */
    private String tokenName;

    /**
     * Obtiene el valor del atributo {@link #idPlannerType}.
     * @return el valor del atributo {@link #idPlannerType}.
     */
    @Id
    @Column(name = "ID_TIPO_PLANIFICADOR", unique = true, nullable = false, precision = NumberConstants.NUM19)
    public Long getIdPlannerType() {
        return idPlannerType;
    }

    /**
     * Establece el valor del atributo {@link #idPlannerType}.
     * @param idPlannerTypeParam El valor para el atributo {@link #idPlannerType}.
     */
    public void setIdPlannerType(Long idPlannerTypeParam) {
        this.idPlannerType = idPlannerTypeParam;
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

}
