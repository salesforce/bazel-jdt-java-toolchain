package sample.processor.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface GeneratedResourceFamilyDefinition {

	Class<?> feature();

	WrapperDefinition wrapper() default @WrapperDefinition(className="");

}
