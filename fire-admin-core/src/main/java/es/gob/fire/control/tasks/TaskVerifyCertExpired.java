package es.gob.fire.control.tasks;

import java.util.Map;

import es.gob.fire.commons.log.Logger;
import es.gob.fire.quartz.job.FireTaskException;
import es.gob.fire.quartz.task.FireTask;

public class TaskVerifyCertExpired extends FireTask {

	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TaskVerifyCertExpired.class);
	
	@Override
	protected void initialMessage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doActionOfTheTask() throws Exception {
		LOGGER.info("Hola mundo de quartz");
	}

	@Override
	protected void endMessage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void prepareParametersForTheTask(Map<String, Object> dataMap) throws FireTaskException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Map<String, Object> getDataResult() throws FireTaskException {
		// TODO Auto-generated method stub
		return null;
	}

}
