package org.sartframework.kafka.serializers;

public class GenericSerialized implements SerializedStructure {

    VersionedStructure structure;

    Object payload;
    
    public GenericSerialized(VersionedStructure structure, Object payload) {
        super();
        this.structure = structure;
        this.payload = payload;
    }

    @Override
    public VersionedStructure getStructure() {

        return structure;
    }


    @Override
    public Object getPayload() {

        return payload;
    }

}
