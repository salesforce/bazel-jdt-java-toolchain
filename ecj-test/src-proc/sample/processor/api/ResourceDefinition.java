package sample.processor.api;

public @interface ResourceDefinition {

	Class<?> family();

	String[] checks() default {};

}
