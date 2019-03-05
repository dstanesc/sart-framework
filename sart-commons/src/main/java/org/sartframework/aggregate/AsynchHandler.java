package org.sartframework.aggregate;

public interface AsynchHandler <T>{

    void handle(T message);

}