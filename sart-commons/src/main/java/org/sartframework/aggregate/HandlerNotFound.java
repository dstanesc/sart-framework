package org.sartframework.aggregate;

public class HandlerNotFound extends RuntimeException {

    private static final long serialVersionUID = 2519685575035933452L;

    final Class<?> handlingClass;
    
    final Class<?> argumentType;

    public HandlerNotFound(Class<?> handlingClass, Class<?> argumentType) {
        super();
        this.handlingClass = handlingClass;
        this.argumentType = argumentType;
    }

    public Class<?> getHandlingClass() {
        return handlingClass;
    }

    public Class<?> getArgumentType() {
        return argumentType;
    }

}
