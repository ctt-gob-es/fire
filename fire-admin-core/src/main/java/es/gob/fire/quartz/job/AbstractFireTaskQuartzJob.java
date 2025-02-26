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
 * <b>File:</b><p>es.gob.fire.quartz.job.AbstractFireTaskQuartzJob.java.</p>
 * <b>Description:</b><p>Class that represents a scheduler task in FIRe. This class must be extends
 * for all the scheduler task classes in FIRe.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>20/10/2021.</p>
 * @author Gobierno de España.
 * @version 1.1, 14/03/2023.
 */
package es.gob.fire.quartz.job;

import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.i18n.IQuartzGeneralMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.quartz.scheduler.AbstractQuartzScheduler;

/**
 * <p>Class that represents a scheduler task in FIRe. This class must be extends
 * for all the scheduler task classes in FIRe.</p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.1, 14/03/2023.
 */
public abstract class AbstractFireTaskQuartzJob implements Job {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractFireTaskQuartzJob.class);

	
	/**
	 * Attribute that represents the task name.
	 */
	private transient String taskName;

	public static void pruebaAbstractAfirmaTaskQuartzJob(){
		LOGGER.info("67");
		LOGGER.warn("67");
		LOGGER.debug("67");
		LOGGER.error("67");
		LOGGER.fatal("67");
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public final void execute(final JobExecutionContext jobExecContext) throws JobExecutionException {
		try {

			List<JobExecutionContext> jobs = jobExecContext.getScheduler().getCurrentlyExecutingJobs();
			for (JobExecutionContext job: jobs) {
				if (job.getTrigger().equals(jobExecContext.getTrigger()) && !job.getFireInstanceId().equals(jobExecContext.getFireInstanceId())) {
					String keyName = jobExecContext.getJobDetail().getKey().getName();
					LOGGER.warn(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ32, new Object[ ] { keyName }));
					return;
				}

			}

			// Se preparan los datos necesarios para la ejecución de la tarea.
			JobDataMap jobDataMap = jobExecContext.getJobDetail().getJobDataMap();
			// Almacenamos el nombre de la tarea quitándole la cadena de texto
			// "-job"
			taskName = jobExecContext.getJobDetail().getKey().getName();
			if (taskName.endsWith(AbstractQuartzScheduler.HYPHEN + AbstractQuartzScheduler.JOB)) {
				int index = taskName.lastIndexOf(AbstractQuartzScheduler.HYPHEN + AbstractQuartzScheduler.JOB);
				taskName = taskName.substring(0, index);
			}
			// Si hay datos, la tarea deberá recogerlos...
			if (jobDataMap.getWrappedMap() != null) {
				prepareParametersForTheTask(jobDataMap.getWrappedMap());
			}

			// Se ejecuta la tarea.
			doTask();

			// Se guardan los datos de la ejecución de la tarea.
			jobDataMap.clear();
			Map<String, Object> dataResult = getDataResult();
			if (dataResult != null) {
				jobDataMap.putAll(dataResult);
			}

		} catch (FireTaskException e) {

			LOGGER.error(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ00, new Object[ ] { jobExecContext.getJobDetail().getKey().getName() }));
			throw new JobExecutionException(e);

		} catch (SchedulerException e) {
			LOGGER.error(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ33));
			throw new JobExecutionException(e);

		}
	}
	
	/**
	 * Method that get from the map parameter, the necessary data for the execution of the task.
	 * @param dataMap Map with the data for the task. Can be null.
	 * @throws FireTaskException In case of some error preparing the parameters for the task.
	 */
	protected abstract void prepareParametersForTheTask(Map<String, Object> dataMap) throws FireTaskException;

	/**
	 * Method that execute the task.
	 * @throws FireTaskException In case of some error executing the task.
	 */
	protected abstract void doTask() throws FireTaskException;

	/**
	 * Method that returns in a map all the necessary data for the next execution of the task.
	 * @return Map with the data for the next execution of the task.
	 * @throws FireTaskException In case of some error setting the data for the next execution of the task.
	 */
	protected abstract Map<String, Object> getDataResult() throws FireTaskException;

	/**
	 * Gets the value of the attribute {@link #taskName}.
	 * @return the value of the attribute {@link #taskName}.
	 */
	public final String getTaskName() {
		return taskName;
	}
}
