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
 * <b>File:</b><p>es.gob.afirma.quartz.planner.PlannerPeriod.java.</p>
 * <b>Description:</b><p>Class that defines the information of a daily/periodic planner.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>20/10/2021.</p>
 * @author Gobierno de España.
 * @version 1.0, 20/10/2021.
 */
package es.gob.fire.quartz.planner;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.commons.utils.UtilsDate;
import es.gob.fire.persistence.entity.CPlannerType;
import es.gob.fire.persistence.entity.Planner;
import es.gob.fire.service.ICPlannerTypeService;
import es.gob.fire.service.impl.CPlannerTypeService;
import es.gob.fire.spring.config.ApplicationContextProvider;

/**
 * <p>Class that defines the information of a diary/periodic planner.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.0, 20/10/2021.
 */
public class PlannerPeriod implements IPlanner, Serializable {

	/**
	 * Class serial version.
	 */
	private static final long serialVersionUID = -4656113774614331791L;
	
	/**
	 * Attribute that represents the services for the configuration persistence: Constants Planner Types.
	 */
	@Autowired
	private ICPlannerTypeService cplannerTypeService;

	/**
	 * Attribute that represents the period planner identifier.
	 */
	public static final String PERIOD_PLANNER = "periodPlanner";

	/**
	 * Attribute that represents the daily planner identifier.
	 */
	public static final String DAILY_PLANNER = "dailyPlanner";

	/**
	 * Attribute that represents the object POJO for a planner.
	 */
	private Planner planner;

	/**
	 * Attribute that represents the default planner identifier.
	 */
	private transient String plannerTypeId = PERIOD_PLANNER;

	/**
	 * Constructor method for the class PlannerPeriod.java.
	 */
	private PlannerPeriod() {
		super();
	}

	/**
	 * Constructor method for the class PlannerPeriod.java.
	 * @param plannerParam Object POJO that represents the planner. If it is null,
	 * initializes a diary planner that starts at 03:00 (24H).
	 */
	public PlannerPeriod(final Planner plannerParam) {
		this();
		planner = plannerParam;
		if (planner == null) {

			planner = new Planner();
			planner.setHourPeriod(Integer.valueOf(NumberConstants.NUM24).longValue());
			planner.setMinutePeriod(0L);
			planner.setSecondPeriod(0L);
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, NumberConstants.NUM3);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			planner.setInitDay(calendar.getTime());

			CPlannerType plannerType = ApplicationContextProvider.getApplicationContext().getBean(CPlannerTypeService.class).getCPlannerTypeById(IPlanner.PLANNER_TYPE_DIARY);
			
			planner.setPlannerType(plannerType);

		}
	}

	/**
	 * Constructor method for the class PlannerPeriod.java.
	 * @param period Period in milliseconds.
	 * @param startDate Date of initialization.
	 * @param isDaily Execute daily
	 */
	public PlannerPeriod(final long period, final Date startDate, final Boolean isDaily) {

		this();
		createPeriodPlanner(period, startDate, isDaily);

	}

	/**
	 * Private method to construct the class PlannerPeriod.java.
	 * @param period Period in milliseconds.
	 * @param startDate Date of initialization.
	 * @param isDaily Execute daily
	 */
	private void createPeriodPlanner(final long period, final Date startDate, final Boolean isDaily) {

		planner = new Planner();

		// Si es diario o no se ha establecido correctamente el periodo...
		if (period < 0 || isDaily != null && isDaily) {

			planner.setHourPeriod(Integer.valueOf(NumberConstants.NUM24).longValue());
			planner.setMinutePeriod(0L);
			planner.setSecondPeriod(0L);
			CPlannerType plannerType = cplannerTypeService.getCPlannerTypeById(IPlanner.PLANNER_TYPE_DIARY);
			planner.setPlannerType(plannerType);

		}
		// Si es periódico...
		else {

			long periodSeconds = period / NumberConstants.NUM1000;
			long hours = periodSeconds / NumberConstants.NUM3600;
			long seconds = periodSeconds % NumberConstants.NUM3600;
			long mins = seconds / NumberConstants.NUM60;
			seconds = seconds % NumberConstants.NUM60;
			planner.setHourPeriod(hours);
			planner.setMinutePeriod(mins);
			planner.setSecondPeriod(seconds);
			CPlannerType plannerType = cplannerTypeService.getCPlannerTypeById(IPlanner.PLANNER_TYPE_DIARY);
			planner.setPlannerType(plannerType);

		}

		if (startDate == null) {

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, NumberConstants.NUM3);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			planner.setInitDay(calendar.getTime());

		} else {

			planner.setInitDay(startDate);

		}

	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public final boolean equals(final Object planificador) {

		boolean result = false;

		try {
			if (planificador instanceof PlannerPeriod) {
				PlannerPeriod plannerDaily = (PlannerPeriod) planificador;

				if (plannerDaily.getPeriodInMilliSeconds() == this.getPeriodInMilliSeconds() && plannerDaily.planner.getInitDay().equals(planner.getInitDay())) {
					result = true;
				}
			}
		} catch (ClassCastException e) {
			result = false;
		}

		return result;

	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		final int prime = NumberConstants.NUM31;
		int result = NumberConstants.NUM1;
		Date inicio = planner.getInitDay();
		result = prime * result + (inicio == null ? 0 : inicio.hashCode());
		long periodo = getPeriodInMilliSeconds();
		result = prime * result + (int) (periodo ^ periodo >>> NumberConstants.NUM32);
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.planner.IPlanner#getNextExecutionInMilliseconds()
	 */
	@Override
	public final long getNextExecutionInMilliseconds() {
		long result = 0;
		Date startDate = planner.getInitDay();
		long periodMilliseconds = getPeriodInMilliSeconds();
		Date actualDate = Calendar.getInstance().getTime();
		// Si no hay fecha de inicio, se considera el periodo:
		if (startDate == null) {
			result = periodMilliseconds;
		}

		// Si la fecha de inicio es anterior a la fecha actual:
		else if (startDate.before(actualDate)) {
			long milliseconds = actualDate.getTime() - startDate.getTime();

			if (periodMilliseconds > 0) {
				result = milliseconds % periodMilliseconds;
				result = periodMilliseconds - result;
			} else {
				result = -1;
			}
		} else // La fecha actual (actualDate) es menor que inicio.
		{
			result = startDate.getTime() - actualDate.getTime();
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.planner.IPlanner#getNextExecutionDate()
	 */
	@Override
	public final Date getNextExecutionDate() {

		Date result = null;
		Date actualDate = Calendar.getInstance().getTime();
		Date startDate = planner.getInitDay();
		long periodMilliseconds = getPeriodInMilliSeconds();
		// Si no hay fecha de inicio, se toma a partir de la actual, el
		// siguiente periodo.
		if (startDate == null) {

			result = new Date(actualDate.getTime() + periodMilliseconds);

		} else {

			// Si la fecha de inicio es anterior a la actual, calculamos la
			// próxima según el periodo.
			if (startDate.before(actualDate)) {

				if (periodMilliseconds > 0) {

					long msBetweenDates = actualDate.getTime() - startDate.getTime();
					long restOfPeriod = msBetweenDates % periodMilliseconds;
					long toCompletePeriod = periodMilliseconds - restOfPeriod;
					result = new Date(actualDate.getTime() + toCompletePeriod);

				} else {

					result = null;

				}

			} else {

				result = startDate;

			}

		}

		return result;

	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.planner.IPlanner#getPeriodInMilliSeconds()
	 */
	@Override
	public final long getPeriodInMilliSeconds() {
		long hourPeriod = planner.getHourPeriod().longValue();
		long minutePeriod = planner.getMinutePeriod().longValue();
		long secondPeriod = planner.getSecondPeriod().longValue();
		return UtilsDate.getPeriod(hourPeriod, minutePeriod, secondPeriod);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.planner.IPlanner#getIdentifier()
	 */
	@Override
	public final String getIdentifier() {
		Long identifier = planner.getIdPlanner();
		if (identifier != null) {
			return identifier.toString();
		} else {
			return plannerTypeId;
		}
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.planner.IPlanner#setIdentifier(java.lang.Long)
	 */
	public final void setIdentifier(final Long identifier) {
		planner.setIdPlanner(identifier);
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.planner.IPlanner#getNumberOfRepetitions()
	 */
	@Override
	public final int getNumberOfRepetitions() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.planner.IPlanner#getPlanner()
	 */
	public final Planner getPlanner() {
		return planner;
	}

}
