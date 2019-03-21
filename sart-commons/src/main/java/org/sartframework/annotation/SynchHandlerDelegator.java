package org.sartframework.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sartframework.aggregate.HandlerNotFound;
import org.sartframework.aggregate.SynchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchHandlerDelegator <T, A extends Annotation, R> implements SynchHandler<T, R> {

    final static Logger LOGGER = LoggerFactory.getLogger(SynchHandlerDelegator.class);
    
    final Object target;

    final Class<A> annotationClass;
    
    public SynchHandlerDelegator(Object target, Class<A> annotationClass) {
        super();
        this.target = target;
        this.annotationClass = annotationClass;
    }

    public static <T, A extends Annotation, R> SynchHandlerDelegator<T, A, R>  wrap(Object target, Class<A> annotationClass) {
        
        return new SynchHandlerDelegator<T, A, R>(target, annotationClass);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public R handle(T t) {
        
       Class<? extends Object> messageClass = t.getClass();

        Class<? extends Object> aggregateClass = target.getClass();
        
        Method found = null;
        
        for (final Method method : aggregateClass.getDeclaredMethods()) {

            final Annotation handler = method.getAnnotation(annotationClass);

            if (handler != null) {

                Class<?>[] parameterTypes = method.getParameterTypes();

                for (int i = 0; i < parameterTypes.length; i++) {
                    
                    Class<?> parameterType = parameterTypes[0];
                    
                    if (parameterType == messageClass) {
                        found = method;
                        break;
                    }
                }
            }
        }
        
        if(found != null) {
            
            try {
                
                return (R) found.invoke(target, t);
                
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                
               throw new IllegalStateException(e);
            }
            
        } else {
            
            LOGGER.error("handler for {} and {} message not found ", aggregateClass, messageClass);
            
            throw new HandlerNotFound(aggregateClass, aggregateClass);
        } 
    }

}
