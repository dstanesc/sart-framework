package org.sartframework.service;

public interface ManagedService <T>{

    T start();
    
    T stop();
}
