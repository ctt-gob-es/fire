

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
 * <b>File:</b><p>es.gob.fire.service.impl.UserDetailsServiceImpl.java.</p>
 * <b>Description:</b><p> .</p>
  * <b>Project:</b><p>Application for monitoring the services of @firma suite systems</p>
 * <b>Date:</b><p>4/03/2020.</p>
 * @author Gobierno de España.
 * @version 1.1, 30/01/2020.
 */
package es.gob.fire.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.gob.fire.persistence.configuration.model.entity.User;
import es.gob.fire.persistence.configuration.model.repository.UserRepository;

/**
 * <p>Service for retrieving the user's authentication and authorization information from a registered user.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.1, 30/01/2019.
 */
@Service("userDetailService")
public class UserDetailsServiceImpl implements UserDetailsService  {

	/**
	 * Attribute that represents the interface that provides access to the CRUD operations
	 * for the UserMonitoriza entity.
	 */
	private final UserRepository repository;

	/**
	 * Constructor method for the class UserDetailsServiceImpl.java.
	 * @param repositoryParam {@link #UserRepository}
	 */
	@Autowired
	public UserDetailsServiceImpl(final UserRepository repositoryParam) {
		this.repository = repositoryParam;
	}

    /**
     * {@inheritDoc}
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException
    {
    	final User curruser = this.repository.findByUsername(username);

        UserDetails user = null;

        if (curruser != null) {
        	user = new org.springframework.security.core.userdetails.User(username, curruser.getPassword(), true,
        	                                                      		true, true, true, AuthorityUtils.createAuthorityList("USER"));
        } else {

        	throw new UsernameNotFoundException("Usuario incorrecto");
        }

        return user;
    }

}
