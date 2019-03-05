package org.sartframework.kafka.serializers;

public class GenericVersionedStructure implements VersionedStructure {

    int version;
    
    String structureName;
    
    public GenericVersionedStructure(int version, String structureName) {
        super();
        this.version = version;
        this.structureName = structureName;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public String getStructureName() {
        return structureName;
    }

}
