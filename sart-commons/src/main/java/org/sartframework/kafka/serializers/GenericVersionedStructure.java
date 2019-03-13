package org.sartframework.kafka.serializers;

public class GenericVersionedStructure implements VersionedStructure {

    int version;
    
    String identity;
    
    public GenericVersionedStructure(int version, String identity) {
        super();
        this.version = version;
        this.identity = identity;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public String getIdentity() {
        return identity;
    }

}
