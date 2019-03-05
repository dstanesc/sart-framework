package org.sartframework.aggregate;

public interface SynchHandler <T, R>{

    R handle(T message);

}