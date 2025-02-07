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
 * <b>File:</b><p>es.gob.fire.persistence.dto.SchedulerEditDTO.java.</p>
 * <b>Description:</b><p> Class that represents the backing form for editing a Task.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * <b>Date:</b><p>15/05/2020.</p>
 * @author Gobierno de España.
 * @version 1.0, 15/05/2020.
 */
package es.gob.fire.persistence.dto;

import java.util.List;

/** 
 * <p>Class that represents the backing form for editing a Task.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI
 * certificates and electronic signature.</p>
 * @version 1.0, 15/05/2020.
 */
public class SchedulerEditDTO {
	
	/**
	 * Attribute that represents the object ID.
	 */
	private Long idSchedulerEdit;
	
	/**
	 * Attribute that represents the name of the scheduler.
	 */
	private String nameEdit;
	
	/**
	 * Attribute that represents the name of the class which implements the scheduler.
	 */
	private String implementationClassEdit;
	
	/**
	 * Attribute that represents the list of planner types.
	 */
	private List<ConstantsDTO> listPlannerTypeEdit;
	
	/**
	 * Attribute that represents type of planners.
	 */
	private String plannerTypeEdit;
	
	/**
	 * Attribute that represents the ID of planner type.
	 */
	private Long idPlannerTypeEdit;
	
	/**
	 * Attribute that represents the ID of planner.
	 */
	private Long idPlannerEdit;
	
	/**
	 * Attribute that represents the hours associated to a period.
	 */
	private Long hourPeriodEdit;
	/**
	 * Attribute that represents the minutes associated to a period.
	 */
	private Long minutePeriodEdit;
	/**
	 * Attribute that represents the seconds associated to a period.
	 */
	private Long secondPeriodEdit;
	
	/**
	 * Attribute that defines if the task is enabled (true) or not (false).
	 */
	private Boolean isEnabledEdit = false;
	
	/**
	 * Attribute the init date of the planner String.
	 */
	private String initDayStringEdit;
	
	/**
	 * Attribute that represents the number of threads in parallel that will be thrown in every process, in milliseconds.
	 */
	private Long numThreads;

	/**
	 * Attribute that represents the number of asynchronous processes that will be evaluated by thread, in milliseconds.
	 */
	private Long numProcess;

	/**
	 * Attribute that represents the period in the one that is considered to be that a consulted request must be eliminated, in milliseconds.
	 */
	private Long checkedTime;
	
	/**
	 * Attribute that represents the period in which an asynchronous request is considered to be expired, in milliseconds.
	 */
	private Long expiredPeriod;
	
	/**
	 * Attribute that represents the period in which it thinks that a node is not active, in milliseconds.
	 */
	private Long reassingTime;

	/**
	 * Attribute that represents the time in the one that is supposed should have finished an asynchronous process, in milliseconds.
	 */
	private Long reactiveTime;
	
	/**
	 * Attribute that represents the variable where the ok messages will be stored.
	 */
	private String msgOkEdit;

	/**
	 * Attribute that represents the variable where the error messages will be stored.
	 */
	private String errorEdit;

	/**
	 * Attribute that represents the description for task of the system or task SPIE.
	 */
	private String descriptionNameEdit;
	
	/**
	 * Gets the value of the attribute {@link #idSchedulerEdit}.
	 * @return the value of the attribute {@link #idSchedulerEdit}.
	 */
	public Long getIdSchedulerEdit() {
		return idSchedulerEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #idSchedulerEdit}.
	 * @param idSchedulerParam The value for the attribute {@link #idSchedulerEdit}.
	 */
	public void setIdSchedulerEdit(Long idSchedulerEditParam) {
		this.idSchedulerEdit = idSchedulerEditParam;
	}


	/**
	 * Gets the value of the attribute {@link #nameEdit}.
	 * @return the value of the attribute {@link #nameEdit}.
	 */
	public String getNameEdit() {
		return nameEdit;
	}

	/**
	 * Sets the value of the attribute {@link #nameEdit}.
	 * @param nameParam The value for the attribute {@link #nameEdit}.
	 */
	public void setNameEdit(String nameEditParam) {
		this.nameEdit = nameEditParam;
	}


	/**
	 * Gets the value of the attribute {@link #implementationClassEdit}.
	 * @return the value of the attribute {@link #implementationClassEdit}.
	 */
	public String getImplementationClassEdit() {
		return implementationClassEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #implementationClassEdit}.
	 * @param implementationClassEditParam The value for the attribute {@link #implementationClassEdit}.
	 */
	public void setImplementationClassEdit(String implementationClassEditParam) {
		this.implementationClassEdit = implementationClassEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #listPlannerTypeEdit}.
	 * @return the value of the attribute {@link #listPlannerTypeEdit}.
	 */

	public List<ConstantsDTO> getListPlannerTypeEdit() {
		return listPlannerTypeEdit;
	}

	/**
	 * Sets the value of the attribute {@link #listPlannerTypeEdit}.
	 * @param listPlannerTypeEditParam The value for the attribute {@link #listPlannerTypeEdit}.
	 */
	public void setListPlannerTypeEdit(List<ConstantsDTO> listPlannerTypeEditParam) {
		this.listPlannerTypeEdit = listPlannerTypeEditParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #plannerTypeEdit}.
	 * @return the value of the attribute {@link #plannerTypeEdit}.
	 */
	
	public String getPlannerTypeEdit() {
		return plannerTypeEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #plannerTypeEdit}.
	 * @param plannerTypeParam The value for the attribute {@link #plannerTypeEdit}.
	 */
	public void setPlannerTypeEdit(String plannerTypeEditParam) {
		this.plannerTypeEdit = plannerTypeEditParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #idPlannerTypeEdit}.
	 * @return the value of the attribute {@link #idPlannerTypeEdit}.
	 */

	public Long getIdPlannerTypeEdit() {
		return idPlannerTypeEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #idPlannerTypeEdit}.
	 * @param idPlannerTypeEditParam The value for the attribute {@link #idPlannerTypeEdit}.
	 */
	public void setIdPlannerTypeEdit(Long idPlannerTypeEditParam) {
		this.idPlannerTypeEdit = idPlannerTypeEditParam;
	}


	
	/**
	 * Gets the value of the attribute {@link #idPlannerEdit}.
	 * @return the value of the attribute {@link #idPlannerEdit}.
	 */
	public Long getIdPlannerEdit() {
		return idPlannerEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #idPlannerEdit}.
	 * @param idPlannerEditParam The value for the attribute {@link #idPlannerEdit}.
	 */
	public void setIdPlannerEdit(Long idPlannerEditParam) {
		this.idPlannerEdit = idPlannerEditParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #hourPeriodEdit}.
	 * @return the value of the attribute {@link #hourPeriodEdit}.
	 */

	public Long getHourPeriodEdit() {
		return hourPeriodEdit;
	}

	/**
	 * Sets the value of the attribute {@link #hourPeriodEdit}.
	 * @param hourPeriodEditParam The value for the attribute {@link #hourPeriodEdit}.
	 */
	public void setHourPeriodEdit(Long hourPeriodEditParam) {
		this.hourPeriodEdit = hourPeriodEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #minutePeriodEdit}.
	 * @return the value of the attribute {@link #minutePeriodEdit}.
	 */
	public Long getMinutePeriodEdit() {
		return minutePeriodEdit;
	}

	/**
	 * Sets the value of the attribute {@link #minutePeriodEdit}.
	 * @param minutePeriodParam The value for the attribute {@link #minutePeriodEdit}.
	 */
	public void setMinutePeriodEdit(Long minutePeriodEditParam) {
		this.minutePeriodEdit = minutePeriodEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #secondPeriodEdit}.
	 * @return the value of the attribute {@link #secondPeriodEdit}.
	 */
	public Long getSecondPeriodEdit() {
		return secondPeriodEdit;
	}

	/**
	 * Sets the value of the attribute {@link #secondPeriodEdit}.
	 * @param secondPeriodParam The value for the attribute {@link #secondPeriodEdit}.
	 */
	public void setSecondPeriodEdit(Long secondPeriodEditParam) {
		this.secondPeriodEdit = secondPeriodEditParam;
	}

	
	/**
	 * Gets the value of the attribute {@link #isEnabledEdit}.
	 * @return the value of the attribute {@link #isEnabledEdit}.
	 */
	public Boolean getIsEnabledEdit() {
		return isEnabledEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #isEnabledEdit}.
	 * @param isEnabledParam The value for the attribute {@link #isEnabledEdit}.
	 */
	public void setIsEnabledEdit(Boolean isEnabledEditParam) {
		this.isEnabledEdit = isEnabledEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #numThreads}.
	 * @return the value of the attribute {@link #numThreads}.
	 */
	public Long getNumThreads() {
		return numThreads;
	}

	/**
	 * Sets the value of the attribute {@link #numThreads}.
	 * @param numThreadsParam The value for the attribute {@link #numThreads}.
	 */
	public void setNumThreads(Long numThreadsParam) {
		this.numThreads = numThreadsParam;
	}

	/**
	 * Gets the value of the attribute {@link #numProcess}.
	 * @return the value of the attribute {@link #numProcess}.
	 */
	public Long getNumProcess() {
		return numProcess;
	}

	/**
	 * Sets the value of the attribute {@link #numProcess}.
	 * @param numProcessParam The value for the attribute {@link #numProcess}.
	 */
	public void setNumProcess(Long numProcessParam) {
		this.numProcess = numProcessParam;
	}

	/**
	 * Gets the value of the attribute {@link #expiredPeriod}.
	 * @return the value of the attribute {@link #expiredPeriod}.
	 */
	public Long getExpiredPeriod() {
		return expiredPeriod;
	}

	/**
	 * Sets the value of the attribute {@link #expiredPeriod}.
	 * @param expiredPeriodParam The value for the attribute {@link #expiredPeriod}.
	 */
	public void setExpiredPeriod(Long expiredPeriodParam) {
		this.expiredPeriod = expiredPeriodParam;
	}

	/**
	 * Gets the value of the attribute {@link #reassingTime}.
	 * @return the value of the attribute {@link #reassingTime}.
	 */
	public Long getReassingTime() {
		return reassingTime;
	}

	/**
	 * Sets the value of the attribute {@link #reassingTime}.
	 * @param reassingTimeParam The value for the attribute {@link #reassingTime}.
	 */
	public void setReassingTime(Long reassingTimeParam) {
		this.reassingTime = reassingTimeParam;
	}

	/**
	 * Gets the value of the attribute {@link #reactiveTime}.
	 * @return the value of the attribute {@link #reactiveTime}.
	 */
	public Long getReactiveTime() {
		return reactiveTime;
	}

	/**
	 * Sets the value of the attribute {@link #reactiveTime}.
	 * @param reactiveTimeParam The value for the attribute {@link #reactiveTime}.
	 */
	public void setReactiveTime(Long reactiveTimeParam) {
		this.reactiveTime = reactiveTimeParam;
	}

	/**
	 * Gets the value of the attribute {@link #checkedTime}.
	 * @return the value of the attribute {@link #checkedTime}.
	 */
	public Long getCheckedTime() {
		return checkedTime;
	}

	/**
	 * Sets the value of the attribute {@link #checkedTime}.
	 * @param checkedTimeParam The value for the attribute {@link #checkedTime}.
	 */
	public void setCheckedTime(Long checkedTimeParam) {
		this.checkedTime = checkedTimeParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #msgOkEdit}.
	 * @return the value of the attribute {@link #msgOkEdit}.
	 */
	public String getMsgOkEdit() {
		return msgOkEdit;
	}

	/**
	 * Sets the value of the attribute {@link #msgOkEdit}.
	 * @param msgOkEditParam The value for the attribute {@link #msgOkEdit}.
	 */
	public void setMsgOkEdit(String msgOkEditParam) {
		this.msgOkEdit = msgOkEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #errorEdit}.
	 * @return the value of the attribute {@link #errorEdit}.
	 */
	public String getErrorEdit() {
		return errorEdit;
	}
	
	/**
	 * Sets the value of the attribute {@link #errorEdit}.
	 * @param errorEditParam The value for the attribute {@link #errorEdit}.
	 */
	public void setErrorEdit(String errorEditParam) {
		this.errorEdit = errorEditParam;
	}
	
	/**
	 * Gets the value of the attribute {@link #initDayStringEdit}.
	 * @return the value of the attribute {@link #initDayStringEdit}.
	 */
	public String getInitDayStringEdit() {
		return initDayStringEdit;
	}

	
	/**
	 * Sets the value of the attribute {@link #initDayStringEdit}.
	 * @param initDayStringEditParam The value for the attribute {@link #initDayStringEdit}.
	 */
	public void setInitDayStringEdit(String initDayStringEditParam) {
		this.initDayStringEdit = initDayStringEditParam;
	}

	/**
	 * Gets the value of the attribute {@link #descriptionNameEdit}.
	 * @return the value of the attribute {@link #descriptionNameEdit}.
	 */
	public String getDescriptionNameEdit() {
		return descriptionNameEdit;
	}

	/**
	 * Sets the value of the attribute {@link #descriptionNameEdit}.
	 * @param descriptionNameEdit The value for the attribute {@link #descriptionNameEdit}.
	 */
	public void setDescriptionNameEdit(String descriptionNameEdit) {
		this.descriptionNameEdit = descriptionNameEdit;
	}

}
