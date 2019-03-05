package org.sartframework.kafka.serializers;

public interface SerializedStructure {
    
    VersionedStructure getStructure();

    Class<?> getJavaType();

    Object getPayload();
}
