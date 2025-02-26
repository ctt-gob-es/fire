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
 * <b>File:</b><p>es.gob.valet.spring.config.WebSecurityConfig.java.</p>
 * <b>Description:</b><p> Class that enables and configures the security of the FIRe application.</p>
  * <b>Project:</b><p></p>
 * <b>Date:</b><p>13 jun. 2018.</p>
 * @author Gobierno de Espa&ntilde;a.
 * @version 1.3, 24/02/2025.
 */
package es.gob.fire.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import es.gob.fire.commons.utils.Constants;

/** 
 * <p>Class that enables and configures the security of the FIRe application. </p>
 * <b>Project:</b><p></p>
 * @version 1.3, 24/02/2025.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	/**
	 * Constant that represents the name of the cookie for session tracking. 
	 */
	public static final String SESSION_TRACKING_COOKIE_NAME = "JSESSIONID";
	
	/**
	 * {@inheritDoc}
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
	@Override
    protected void configure(HttpSecurity http) throws Exception {  
      http.authorizeRequests()
	    .antMatchers("/css/**", "/images/**", "/js/**", "/fonts/**", "/fonts/icons/themify/**", "/fonts/fontawesome/**", "/less/**", "/chartist/**", // Enable css, images and js when logged out 
	    		"/loginClave", "/ResponseClave", "/loginWithCertificate") // Rutas publicas
	    .permitAll()
		.and()
		.authorizeRequests()
		.antMatchers("/", "add", "delete/{id}", "edit/{id}", "save", "users")
		.access("hasRole(" + Constants.ROLE_ADMIN + ")")
		.anyRequest()
		.authenticated()
		.and()
		.formLogin()
        .loginPage("/")
        .defaultSuccessUrl("/inicio")
        .permitAll()
        .failureUrl("/login-error")
        .and()
		.logout().invalidateHttpSession(true).deleteCookies(SESSION_TRACKING_COOKIE_NAME).clearAuthentication(true).logoutSuccessUrl("/?logout")
		.permitAll()
		.and()
		.httpBasic()
		.and()
		.csrf()
		.disable() // Disable CSRF
		.sessionManagement()
        .sessionFixation().migrateSession()
    	.maximumSessions(1)
    	.maxSessionsPreventsLogin(false)
    	.expiredUrl("/login.html");
    }
	
	/**
	 * Method that creates a new HttpSessionEventPublisher instance.
	 * @return new HttpSessionEventPublisher instance
	 */
	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
	    return new HttpSessionEventPublisher();
	}

}
