package org.sartframework.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgumentTypeCollector <T, A extends Annotation> {

    final static Logger LOGGER = LoggerFactory.getLogger(ArgumentTypeCollector.class);
    
    final Object target;

    final Class<A> annotationClass;
    
    final Set<Class<?>> parameterTypes = new HashSet<>();
    
    public ArgumentTypeCollector(Object target, Class<A> annotationClass) {
        super();
        this.target = target;
        this.annotationClass = annotationClass;
    }

    public static <T, A extends Annotation> ArgumentTypeCollector<T, A>  wrap(Object target, Class<A> annotationClass) {
        
        return new ArgumentTypeCollector<T, A>(target, annotationClass).collect();
    }
    
  
    public ArgumentTypeCollector<T, A>  collect() {
        
        Class<? extends Object> beanClass = target.getClass();
        
        for (final Method method : beanClass.getDeclaredMethods()) {

            final Annotation annotation = method.getAnnotation(annotationClass);

            if (annotation != null) {

                Class<?>[] argTypes = method.getParameterTypes();

                for (int i = 0; i < argTypes.length; i++) {
                    
                    Class<?> parameterType = argTypes[0];
                    
                    parameterTypes.add(parameterType);
                }
            }
        }
        
        return this;
    }

    
    public boolean hasParameterType(Class<?> parameterType) {
        
        return parameterTypes.contains(parameterType);
    }
}
