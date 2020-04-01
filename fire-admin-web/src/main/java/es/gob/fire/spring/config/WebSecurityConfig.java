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
 * <b>File:</b><p>es.gob.valet.spring.config.WebSecurityConfig.java.</p>
 * <b>Description:</b><p> Class that enables and configures the security of the Valet application.</p>
  * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * <b>Date:</b><p>13 jun. 2018.</p>
 * @author Gobierno de España.
 * @version 1.0, 13 jun. 2018.
 */
package es.gob.fire.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import es.gob.fire.service.impl.UserDetailsServiceImpl;

/** 
 * <p>Class that enables and configures the security of the Valet application. </p>
 * <b>Project:</b><p>Platform for detection and validation of certificates recognized in European TSL.</p>
 * @version 1.0, 13 jun. 2018.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	/**
	 * Attribute that represents the injected service for user authentication. 
	 */
	@Autowired
    private UserDetailsServiceImpl userDetailsService;
	
	/**
	 * {@inheritDoc}
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
	@Override
    protected void configure(HttpSecurity http) throws Exception {
        
      http
        .authorizeRequests()
        	.antMatchers("/css/**", "/images/**", "/js/**", "/fonts/**", "/fonts/icons/themify/**", "/fonts/fontawesome/**", "/less/**")
        	.permitAll() // Enable css, images and js when logged out
        	.and()
        .authorizeRequests()
          	.antMatchers("/", "add", "delete/{id}", "edit/{id}", "save", "users")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
        .formLogin()
            .loginPage("/")
            .defaultSuccessUrl("/inicio")
            .permitAll()
            .and()
        .logout()
            .permitAll()
            .and()
        .httpBasic()
        	.and()
        .csrf()
        	.disable()			//Disable CSRF
        .sessionManagement()
	        .sessionFixation().migrateSession()
	    	.maximumSessions(1)
	    	.maxSessionsPreventsLogin(false)
	    	.expiredUrl("/login.html"); 

    }
	 BCryptPasswordEncoder bCryptPasswordEncoder;
	    //Crea el encriptador de contraseñas
	    @Bean
	    public BCryptPasswordEncoder passwordEncoder() {
			this.bCryptPasswordEncoder = new BCryptPasswordEncoder(4);
	//El numero 4 representa que tan fuerte quieres la encriptacion.
	//Se puede en un rango entre 4 y 31.
	//Si no pones un numero el programa utilizara uno aleatoriamente cada vez
	//que inicies la aplicacion, por lo cual tus contrasenas encriptadas no funcionaran bien
	        return this.bCryptPasswordEncoder;
	    }
    /**
     * Method that sets the authentication global configuration.
     * @param auth Object that represents the Spring security builder.
     * @throws Exception Object that represents the exception thrown in case of error.
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }
}
