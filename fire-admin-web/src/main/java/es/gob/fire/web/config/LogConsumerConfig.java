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
 * <b>File:</b><p>es.gob.monitoriza.spring.config.WebLocalizedConfig.java.</p>
 * <b>Description:</b><p>Class that enables and configures Localization for the Monitoriz@ application.</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>14 mar. 2018.</p>
 * @author Gobierno de España.
 * @version 1.0, 14 mar. 2018.
 */
package es.gob.fire.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import es.gob.fire.persistence.dto.LogConsumerConnectionDTO;
import es.gob.log.consumer.client.LogConsumerClient;

/**
 * <p>Class that enables and configures Localization for the Monitoriz@ application.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems.</p>
 * @version 1.0, 14 mar. 2018.
 */
@Configuration
public class LogConsumerConfig {

	/**
	 * Method that creates a new LogConsumerClient instance for this session.
	 * @return A LogConsumerClient instance.
	 */
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LogConsumerClient logConsumerBean() {
		return new LogConsumerClient();
	}

	/**
	 * Method that creates a new LogConsumerConnectionInfo instance for this session.
	 * @return A LogConsumerConnectionInfo instance.
	 */
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public LogConsumerConnectionDTO connectionInfoBean() {
		return new LogConsumerConnectionDTO();
	}
}
