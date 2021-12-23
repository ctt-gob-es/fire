package es.gob.fire.persistence.validatordni;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
 
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = validatorDNI.class)
@Documented
public @interface Nif {
 
	String message() default "{com.autentia.core.persistentce.constraints.nif}";
 
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
	
}