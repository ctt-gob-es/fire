/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
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
 * <b>File:</b><p>es.gob.fire.i18n.ICoreGeneralMessages.java.</p>
 * <b>Description:</b><p>Interface that defines all the token constants for the messages
 * in the core module of FIRe: general.</p>
 * <b>Project:</b><p>Application for signing documents of FIRe suite systems</p>
 * <b>Date:</b><p>20/10/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.3, 04/03/2025.
 */
package es.gob.fire.i18n;

/**
 * <p>Interface that defines all the token constants for the messages
 * in the core module of FIRe: general.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.3, 04/03/2025.
 */
public interface ICoreMessages {

	/***********************/
	/** Mensajes para errores para las tareas **/ 
	/***********************/
	
	/**
	 * Constant attribute that represents the property key <code>TASK_MNG_002</code> belonging to the
	 * file messages/core/task_xx.properties.
	 */
	String TASK_MNG_002 = "TASK_MNG_002";
	
	/**
	 * Constant attribute that represents the property key <code>TASK_MNG_003</code> belonging to the
	 * file messages/core/task_xx.properties.
	 */
	String TASK_MNG_003 = "TASK_MNG_003";
	
	/***********************/
	/** Mensajes para errores para los errores del core de fire **/ 
	/***********************/
	
	/**
	 * Constant attribute that represents the property key <code>CE001</code> belonging to the
	 * file messages/core/fire_xx.properties.
	 */
	String LOG_CE001 = "CE001";

}
