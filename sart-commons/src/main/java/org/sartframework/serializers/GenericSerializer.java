package org.sartframework.serializers;

public interface GenericSerializer <T> {

    T deserialize(byte[] data);
    
    byte[] serialize(T t);
}
