package es.gob.fire.quartz.scheduler;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.commons.utils.UtilsStringChar;
import es.gob.fire.i18n.IQuartzGeneralMessages;
import es.gob.fire.i18n.Language;

import static org.quartz.TriggerBuilder.newTrigger;

public abstract class AbstractQuartzScheduler {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(AbstractQuartzScheduler.class);
	
	/**
	 * Constant attribute that represents the hyphen.
	 */
	public static final String HYPHEN = "-";

	/**
	 * Constant attribute that represents an extension for a job name.
	 */
	public static final String JOB = "job";

	/**
	 * Constant attribute that represents an extension for a trigger name.
	 */
	private static final String TRIGGER = "trigger";

	/**
	 * Attribute that represents if the actions "start" and "stop" schedulers
	 * are blocked.
	 */
	private transient boolean blockScheduler;

	/**
	 * Private constructor method for the class AbstractQuartzScheduler.java.
	 */
	protected AbstractQuartzScheduler() {
		// This constructor is intentionally empty. Nothing special is needed
		// here.
	}

	/**
	 * Abstract method that gets the name of the scheduler group.
	 * This name must be a constant.
	 * @return String with the name of the scheduler group.
	 */
	protected abstract String getSchedulerGroup();

	/**
	 * Abstract method that gets Scheduler for the tasks.
	 * This must be a static attribute in the subclass.
	 * @return The scheduler to manage.
	 */
	protected abstract Scheduler getScheduler();

	/**
	 * Methods that sets the scheduler to manage.
	 * This must be setted in the static attribute in the subclass.
	 * @param scheduler Scheduler to set.
	 */
	protected abstract void setScheduler(Scheduler scheduler);

	/**
	 * Method that get the path to the properties file with the properties
	 * to initialize the scheduler.
	 * @return Path to the properties file.
	 */
	protected abstract String getPathPropertiesFile();

	/**
	 * Abstract method that specifies if this node must run the scheduler.
	 * @return <code>true</code> if the scheduler must be runned on this node, otherwise <code>false</code>.
	 */
	protected abstract boolean initTheSchedulerOnThisNode();
	
	/**
	 * Method that initialize the scheduler for the tasks of the management precesses module.
	 * @return <code>true</code> if the scheduler has been initialized, otherwise <code>false</code>.
	 */
	private boolean initializeScheduler() {

		boolean result = true;

		String filePath = getPathPropertiesFile();
		try {
			StdSchedulerFactory schedulerFactory = null;
			if (UtilsStringChar.isNullOrEmptyTrim(filePath)) {
				schedulerFactory = new StdSchedulerFactory();
			} else {
				schedulerFactory = new StdSchedulerFactory(filePath);
			}

			Scheduler scheduler = schedulerFactory.getScheduler();
			setScheduler(scheduler);
			scheduler.start();
			LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ06, new Object[ ] { filePath }));
		} catch (SchedulerException e) {
			result = false;
			LOGGER.error(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ07, new Object[ ] { filePath, e }));
		}

		return result;

	}
	/**
	 * Method that check if the scheduler is initialized. If not is the case, then try
	 * to initialize.
	 * @return <code>true</code> if the scheduler is initialized, otherwise <code>false</code>.
	 */
	private boolean isInitialized() {

		boolean result = false;

		if (initTheSchedulerOnThisNode()) {

			if (blockScheduler) {

				LOGGER.warn(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ28));

			} else {

				try {

					if (getScheduler() == null || !getScheduler().isStarted()) {
						result = initializeScheduler();
					} else {
						result = true;
					}

				} catch (SchedulerException e) {
					LOGGER.error(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ26, new Object[ ] { e }));
				}

			}

		}

		return result;

	}

	/**
	 * Private method that create a job key from the job name.
	 * @param jobName String with the job name.
	 * @return JobKey to use in the scheduler.
	 */
	protected final JobKey createJobKey(final String jobName) {

		String fullJobName = jobName + HYPHEN + JOB;
		return new JobKey(fullJobName, getSchedulerGroup());

	}

	/**
	 * Private method that create a trigger key from the job name and trigger identifier.
	 * @param jobName String with the name of the job which is going to be associated
	 * the trigger.
	 * @param triggerId Triger identifier.
	 * @return TriggerKey to use in the scheduler.
	 */
	protected final TriggerKey createTriggerKey(final String jobName, final String triggerId) {

		String triggerName = jobName + HYPHEN + triggerId + HYPHEN + TRIGGER;
		return new TriggerKey(triggerName, getSchedulerGroup());

	}

	/**
	 * Method that add a job with a trigger into a scheduler. If the job already exist in the
	 * scheduler, then the dataMap is going to be ignored.
	 * @param startTime Date and time to start the job.
	 * @param repeatMillis Milliseconds to wait for throw again the job.
	 * @param numberOfReps Number of repettition to apply. If this number is less or equal
	 * to 0, then loop forever.
	 * @param jobName Name of the job.
	 * @param triggerId Identifier to apply to the identifier.
	 * @param jobClass Class that contains the job to apply.
	 * @param dataMap Data that has to be passed to the job. Null if there is not data to be passed.
	 * @return <code>true</code> if the job has been added correctly to the scheduler,
	 * otherwise <code>false</code>.
	 * @throws SchedulerException In case of some error while is adding the job or updating the trigger
	 * associated to this.
	 */
	protected final boolean addOrReplaceJobTrigger(final Date startTime, final long repeatMillis, final int numberOfReps, final String jobName, final String triggerId, final Class<? extends Job> jobClass, final JobDataMap dataMap) throws SchedulerException {

		boolean result = false;

		// Comprobamos los parámetros de entrada del método.
		boolean checkParamsIn = startTime == null || repeatMillis <= 0 || UtilsStringChar.isNullOrEmptyTrim(jobName);
		checkParamsIn = checkParamsIn || UtilsStringChar.isNullOrEmptyTrim(triggerId) || jobClass == null;
		if (checkParamsIn) {

			LOGGER.debug(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ08));

		} else {

			// Comprobamos si el scheduler está ya inicializado, y si no es así,
			// se intenta inicializar.
			if (isInitialized()) {

				result = addOrReplaceJobTriggerAux(startTime, repeatMillis, numberOfReps, jobName, triggerId, jobClass, dataMap);

			}

		}

		return result;

	}

	/**
	 * Auxiliar method to reduce the cyclomatic complexity.
	 * Method that add a job with a trigger into a scheduler. If the job already exist in the
	 * scheduler, then the dataMap is going to be ignored.
	 * @param startTime Date and time to start the job.
	 * @param repeatMillis Milliseconds to wait for throw again the job.
	 * @param numberOfReps Number of repettition to apply. If this number is less or equal
	 * to 0, then loop forever.
	 * @param jobName Name of the job.
	 * @param triggerId Identifier to apply to the identifier.
	 * @param jobClass Class that contains the job to apply.
	 * @param dataMap Data that has to be passed to the job. Null if there is not data to be passed.
	 * @return <code>true</code> if the job has been added correctly to the scheduler,
	 * otherwise <code>false</code>.
	 * @throws SchedulerException In case of some error while is adding the job or updating the trigger
	 * associated to this.
	 */
	private boolean addOrReplaceJobTriggerAux(final Date startTime, final long repeatMillis, final int numberOfReps, final String jobName, final String triggerId, final Class<? extends Job> jobClass, final JobDataMap dataMap) throws SchedulerException {

		// Creamos el nombre del job y del trigger bajo el mismo
		// grupo.
		JobKey jobKey = createJobKey(jobName);
		TriggerKey triggerKey = createTriggerKey(jobName, triggerId);
		JobDetail job = null;
		SimpleTrigger trigger = null;

		// Comprobamos si el job ya está definido en el scheduler,
		// por ejemplo
		// si lo ha tomado del cluster al iniciar, o simplemente
		// estamos añadiendo
		// otro trigger para el mismo job.
		if (getScheduler().checkExists(jobKey)) {

			// Si ya existe el job, lo recuperamos del scheduler.
			LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ09, new Object[ ] { jobName, getSchedulerGroup() }));
			job = getScheduler().getJobDetail(jobKey);

		} else {

			// Comprobamos los datos a pasar en el job, para que en
			// el caso de que sea nulo,
			// se inicialice a vacío.
			JobDataMap jobDataMap = dataMap == null ? new JobDataMap() : dataMap;
			// Si no existe, lo añadimos.
			LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ10, new Object[ ] { jobName, getSchedulerGroup() }));
			// Creamos el job.
			job = JobBuilder.newJob(jobClass).withIdentity(jobKey).usingJobData(jobDataMap).requestRecovery(true).build();

		}

		// En función del número de repeticiones a aplicar,
		// generamos el trigger.
		if (numberOfReps <= 0) {

			LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ11, new Object[ ] { repeatMillis }));
			trigger = (SimpleTrigger) newTrigger().withIdentity(triggerKey).startAt(startTime).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(repeatMillis).repeatForever().withMisfireHandlingInstructionNowWithRemainingCount()).forJob(job).build();

		} else {

			LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ12, new Object[ ] { startTime }));
			trigger = (SimpleTrigger) newTrigger().withIdentity(triggerKey).startAt(startTime).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(repeatMillis).withRepeatCount(numberOfReps - 1)).forJob(job).build();

		}

		// Comprobamos si el trigger ya está definido en el
		// scheduler.
		// Debido a la forma de generar el nombre del trigger, este
		// es único
		// para un job concreto.
		if (getScheduler().checkExists(triggerKey)) {

			// Si existe, lo relanzamos con la nueva configuración.
			LOGGER.debug(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ13));
			getScheduler().rescheduleJob(triggerKey, trigger);

		} else {

			LOGGER.debug(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ14));
			// En caso de que ya existiera el Job, tan solo hay que
			// indicar
			// que se lance el trigger.
			if (getScheduler().checkExists(jobKey)) {
				getScheduler().scheduleJob(trigger);
			} else {
				// Si no existía, se lanzan el job y el trigger.
				getScheduler().scheduleJob(job, trigger);
			}

		}

		return true;

	}

	/**
	 * Method that remove the job from the scheduler.
	 * @param jobName String with the name of the job to delete.
	 * All the triggers associated is going to be deleted also.
	 * @return <code>true</code> if the job has been removed. Otherwise <code>false</code>.
	 * @throws SchedulerException In case of some error while is removing the job from
	 * the scheduler.
	 */
	protected final boolean removeJob(final String jobName) throws SchedulerException {

		boolean result = false;

		if (UtilsStringChar.isNullOrEmptyTrim(jobName)) {

			LOGGER.debug(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ15));

		} else {

			if (isInitialized()) {

				JobKey jobKey = createJobKey(jobName);

				result = getScheduler().deleteJob(jobKey);
				if (result) {
					LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ16, new Object[ ] { jobKey.getName(), getSchedulerGroup() }));
				} else {
					LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ17, new Object[ ] { jobKey.getName(), getSchedulerGroup() }));
				}

			}

		}

		return result;

	}

	/**
	 * Method that remove the trigger from the scheduler.
	 * If the related job does not have any other triggers, and the job is not durable,
	 * then the job will also be deleted.
	 * @param jobName String with the name of the job associated to the trigger.
	 * @param triggerId String with the identifier of the trigger to remove.
	 * @return <code>true</code> if the job has been removed. Otherwise <code>false</code>.
	 * @throws SchedulerException In case of some error while is unscheduling the trigger of the job.
	 */
	protected final boolean removeTrigger(final String jobName, final String triggerId) throws SchedulerException {

		boolean result = false;

		if (UtilsStringChar.isNullOrEmptyTrim(jobName) || UtilsStringChar.isNullOrEmptyTrim(triggerId)) {

			LOGGER.debug(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ18));

		} else {

			if (isInitialized()) {

				TriggerKey triggerKey = createTriggerKey(jobName, triggerId);

				result = getScheduler().unscheduleJob(triggerKey);
				if (result) {
					LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ19, new Object[ ] { triggerKey.getName(), getSchedulerGroup() }));
				} else {
					LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ20, new Object[ ] { triggerKey.getName(), getSchedulerGroup() }));
				}

			}

		}

		return result;

	}

	/**
	 * Method that checks if a determinated job exists in the scheduler.
	 * @param jobName Name of the job to check. If this parameter is a null or empty string,
	 * the return of the method is <code>false</code>.
	 * @return <code>true</code> if the job exists in the scheduler, otherwise <code>false</code>.
	 * @throws SchedulerException In case of some error while is checking the existence of the job
	 * in the scheduler.
	 */
	protected final boolean checkIfExistJob(final String jobName) throws SchedulerException {

		boolean result = false;

		if (UtilsStringChar.isNullOrEmptyTrim(jobName)) {

			LOGGER.debug(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ21));

		} else {

			if (isInitialized()) {

				JobKey jobKey = createJobKey(jobName);

				result = getScheduler().checkExists(jobKey);
				if (result) {
					LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ22, new Object[ ] { jobName, getSchedulerGroup() }));
				} else {
					LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ23, new Object[ ] { jobName, getSchedulerGroup() }));
				}

			}

		}

		return result;

	}

	/**
	 * Method that start the scheduler.
	 * @return <code>true</code> if the scheduler has been initialized, or already was initialized.
	 * Otherwise <code>false</code>.
	 */
	protected final boolean startScheduler() {

		return isInitialized();

	}

	/**
	 * Method that stop and destroy the scheduler and all the jobs.
	 * @param taskManagerName Name for task manager
	 * @return <code>true</code> if the scheduler has been stopped, otherwise <code>false</code>.
	 */
	protected final boolean stopScheduler(final String taskManagerName) {

		boolean result = false;

		if (initTheSchedulerOnThisNode()) {

			if (blockScheduler) {

				LOGGER.warn(Language.getResQuartzGeneral(IQuartzGeneralMessages.LOGMQ27));

				// Aunque el Scheduler esté bloqueado (no se puedan realizar
				// operaciones sobre este),
				// podemos comprobar si ya está detenido.
				try {
					result = getScheduler() == null || !getScheduler().isStarted();
				} catch (SchedulerException e) {
					LOGGER.error(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ25, new Object[ ] { taskManagerName, getPathPropertiesFile(), e }));
				}

			} else {
				try {
					if (getScheduler() == null) {
						LOGGER.warn(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ29, new Object[ ] { taskManagerName, getPathPropertiesFile() }));
						result = true;
					} else {
						if (!getScheduler().isStarted()) {
							LOGGER.warn(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ30, new Object[ ] { taskManagerName, getPathPropertiesFile() }));
							result = true;
						} else {
							List<JobExecutionContext> executingJobs = getScheduler().getCurrentlyExecutingJobs();
							if (executingJobs != null) {
								Iterator<JobExecutionContext> executingJobsIt = executingJobs.iterator();
								StringBuffer execJobsList = new StringBuffer();
								while (executingJobsIt.hasNext()) {
									execJobsList.append(executingJobsIt.next().getJobDetail().getKey().getName());
									if (executingJobsIt.hasNext()) {
										execJobsList.append(", ");
									}
								}
								if (execJobsList.length() > 0) {
									LOGGER.error(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ31, new Object[ ] { execJobsList.toString() }));
								}
							}
							getScheduler().shutdown(true);
							setScheduler(null);
							LOGGER.debug(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ24, new Object[ ] { getPathPropertiesFile() }));
							result = true;
						}
					}
				} catch (SchedulerException e) {
					LOGGER.error(Language.getFormatResQuartzGeneral(IQuartzGeneralMessages.LOGMQ25, new Object[ ] { taskManagerName, getPathPropertiesFile(), e }));
				}
			}

		} else {

			// Si no se debe iniciar este scheduler, evidentemente significa que
			// está detenido.
			result = true;

		}

		return result;

	}

	/**
	 * Method that blocks the operations for the initialization and detention
	 * of the Scheduler.
	 */
	protected final void blockStartAndStopOperations() {
		blockScheduler = true;
	}

	/**
	 * Method that unblocks the operations for the initialization and detention
	 * of the Scheduler.
	 */
	protected final void unblockStartAndStopOperations() {
		blockScheduler = false;
	}

}
