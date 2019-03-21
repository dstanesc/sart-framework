package org.sartframework.projection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.sartframework.aggregate.HandlerNotFound;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryDelegator<A extends Annotation, R extends QueryResult> implements QueryHandler<R> {

    final static Logger LOGGER = LoggerFactory.getLogger(QueryDelegator.class);

    final Object target;

    final Class<A> annotationClass;

    public QueryDelegator(Object target, Class<A> annotationClass) {
        super();
        this.target = target;
        this.annotationClass = annotationClass;
    }

    public static <A extends Annotation, R extends QueryResult> QueryDelegator<A, R> wrap(Object target, Class<A> annotationClass) {

        return new QueryDelegator<A,R>(target, annotationClass);
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends DomainQuery> List<R> handleQuery(T domainQuery) {

        Class<? extends Object> queryClass = domainQuery.getClass();

        Class<? extends Object> aggregateClass = target.getClass();

        Method found = null;

        for (final Method method : aggregateClass.getDeclaredMethods()) {

            final Annotation handler = method.getAnnotation(annotationClass);

            if (handler != null) {

                Class<?>[] parameterTypes = method.getParameterTypes();

                for (int i = 0; i < parameterTypes.length; i++) {

                    Class<?> parameterType = parameterTypes[0];

                    if (parameterType == queryClass) {

                        found = method;

                        Class<?> returnType = found.getReturnType();

                        if (List.class.isAssignableFrom(returnType)) {
                            break;
                        } else
                            throw new UnsupportedOperationException("Return type " + returnType + "unsupported");
                    }
                }
            }
        }

        if (found != null) {
            
            try {

                return (List<R>) found.invoke(target, domainQuery);

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {

                throw new RuntimeException(e);
            }

        } else {

            LOGGER.error("handler for {} and {} message not found ", aggregateClass, queryClass);

            throw new HandlerNotFound(aggregateClass, aggregateClass);
        }
    }

}
