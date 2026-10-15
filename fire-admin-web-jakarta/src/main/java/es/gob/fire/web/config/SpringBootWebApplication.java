package es.gob.fire.web.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * Java EE 8 entry point. The packaged WAR is migrated to Jakarta after
 * compilation.
 */
@SpringBootApplication
@ComponentScan("es.gob.fire")
public class SpringBootWebApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
		return application.sources(SpringBootWebApplication.class);
	}

	public static void main(final String[] args) {
		SpringApplication.run(SpringBootWebApplication.class, args);
	}
}
