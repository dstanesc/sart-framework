package org.sartframework.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.sartframework.serializers.ContentSerializer;
import org.sartframework.serializers.UnspecifiedContentSerializer;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Evolvable {

    int version();
    
    String identity() default "";
    
    @SuppressWarnings("rawtypes")
    Class<? extends ContentSerializer> serializer() default UnspecifiedContentSerializer.class;
}
