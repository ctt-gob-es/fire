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
 * <b>File:</b><p>es.gob.fire.quartz.task.FireTask.java.</p>
 * <b>Description:</b><p>Class that represents a scheduler task and the action that it realizes.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p>20/10/2021.</p>
 * @author Gobierno de España.
 * @version 1.0, 20/10/2021.
 */
package es.gob.fire.quartz.task;

import es.gob.fire.exceptions.IFireException;
import es.gob.fire.quartz.job.AbstractFireTaskQuartzJob;
import es.gob.fire.quartz.job.FireTaskException;

/** 
 * <p>Class that represents a scheduler task and the action that it realizes.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * @version 1.1, 15/12/2022.
 */
public abstract class FireTask extends AbstractFireTaskQuartzJob {
	
	/**
	 * Constant attribute that represents the scheduler service
	 */
	public static final String SCHEDULER_SERVICE = "schedulerService";

	/**
	 * 
	 * {@inheritDoc}
	 * @see es.gob.fire.quartz.job.AbstractAfirmaTaskQuartzJob#doTask()
	 */
	@Override
	protected final void doTask() throws FireTaskException {

		initialMessage();

		try {
			doActionOfTheTask();
		} catch (Exception e) {
			throw new FireTaskException(IFireException.COD_185, e.getMessage(), e);
		}

		endMessage();

	}
	
	/**
	 * Abstract method that must show in the log the initial message for the task.
	 */	
	protected abstract void initialMessage();

	/**
	 * Method that must realize the action of the task.
	 * @throws Exception In case of some error while is running the task.
	 */
	protected abstract void doActionOfTheTask() throws Exception;

	/**
	 * Abstract method that must show in the log the end message for the task.
	 */
	protected abstract void endMessage();
	
}
