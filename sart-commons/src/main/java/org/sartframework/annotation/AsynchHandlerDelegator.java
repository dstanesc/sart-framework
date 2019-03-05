package org.sartframework.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sartframework.aggregate.AsynchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsynchHandlerDelegator<T, A extends Annotation> implements AsynchHandler<T> {

    final static Logger LOGGER = LoggerFactory.getLogger(AsynchHandlerDelegator.class);

    final Object target;

    final Class<A> annotationClass;

    public AsynchHandlerDelegator(Object target, Class<A> annotationClass) {
        super();
        this.target = target;
        this.annotationClass = annotationClass;
    }

    public static <T, A extends Annotation> AsynchHandlerDelegator<T, A> wrap(Object target, Class<A> annotationClass) {

        return new AsynchHandlerDelegator<T, A>(target, annotationClass);
    }

    @Override
    public void handle(T t) {

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

        if (found != null) {

            try {

                found.invoke(target, t);

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {

                e.printStackTrace();
                
                throw new RuntimeException(e);
            }

        } else {

            LOGGER.error("handler for {} and {} message not found ", aggregateClass, messageClass);

            throw new RuntimeException("Handler not found for " + messageClass);
        }
    }

}
