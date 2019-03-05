package org.sartframework.kafka.serializers;

public interface VersionedStructure {

    int getVersion();
    
    String getStructureName();
}
