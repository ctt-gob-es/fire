package es.gob.fire.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import es.gob.fire.commons.utils.Constants;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	public static final String SESSION_TRACKING_COOKIE_NAME = "JSESSIONID";

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/css/**", "/images/**", "/js/**", "/fonts/**",
						"/fonts/icons/themify/**", "/fonts/fontawesome/**", "/less/**",
						"/loginClave", "/ResponseClave", "/loginWithCertificate")
					.permitAll()
			.and()
				.authorizeRequests()
					.antMatchers("/", "add", "delete/{id}", "edit/{id}", "save", "users")
						.access("hasRole(" + Constants.ROLE_ADMIN + ")")
					.anyRequest().authenticated()
			.and()
				.formLogin()
					.loginPage("/")
					.defaultSuccessUrl("/inicio", true)
					.failureUrl("/login-error")
					.permitAll()
			.and()
				.logout()
					.invalidateHttpSession(true)
					.deleteCookies(SESSION_TRACKING_COOKIE_NAME)
					.clearAuthentication(true)
					.logoutSuccessUrl("/?logout")
					.permitAll()
			.and()
				.httpBasic()
			.and()
				.csrf().disable()
				.sessionManagement()
					.sessionFixation().migrateSession()
					.maximumSessions(1)
					.maxSessionsPreventsLogin(false)
					.expiredUrl("/login.html");
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}
}
