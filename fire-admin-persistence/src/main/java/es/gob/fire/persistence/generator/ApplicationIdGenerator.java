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
 * <b>File:</b><p>es.gob.fire.persistence.generator.ApplicationIdGenerator.java.</p>
 * <b>Description:</b><p>Class that manages the generation of the identifier dor the table <i>TB_APLICACIONES</i>.</p>
  * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * <b>Date:</b><p>17/05/2021.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.0, 17/05/2021.
 */
package es.gob.fire.persistence.generator;

import java.io.Serializable;
import java.security.GeneralSecurityException;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import es.gob.fire.commons.utils.Hexify;

/**
 * <p>Class that manages the generation of the identifier dor the table <i>TB_APLICACIONES</i>.</p>
 * <b>Project:</b><p>Application for signing documents of @firma suite systems</p>
 * @version 1.0, 17/05/2021.
 */
public class ApplicationIdGenerator implements IdentifierGenerator {
	
	/**
	 * Attribute that represents the object that manages the log of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ApplicationIdGenerator.class);
    
    private static final String HMAC_ALGORITHM = "HmacMD5"; //$NON-NLS-1$

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) throws HibernateException {
		return generateId();
    }    
    
    /**
	 * Genera un nuevo identificador de aplicaci&oacute;n.
	 * @return Identificador de aplicaci&oacute;n.
	 * @throws GeneralSecurityException Cuando no se puede generar un identificador.
	 */
	private static String generateId() throws HibernateException {

		Mac mac;
		try {
			final KeyGenerator kGen = KeyGenerator.getInstance(HMAC_ALGORITHM);
			final SecretKey hmacKey = kGen.generateKey();

			mac = Mac.getInstance(hmacKey.getAlgorithm());
			mac.init(hmacKey);
		}
		catch (final GeneralSecurityException e) {
			LOGGER.fatal("No ha sido posible generar una clave aleatoria como identificador de aplicacion: " + e); //$NON-NLS-1$
			throw new HibernateException(e.getCause());
		}

		return Hexify.hexify(mac.doFinal(), "").substring(0, 12); //$NON-NLS-1$
	}
}