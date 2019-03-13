package org.sartframework.kafka.serializers;

public interface SerializedStructure {
    
    VersionedStructure getStructure();

    
    Object getPayload();
}
