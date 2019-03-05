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
    public Class<?> getJavaType() {

        try {
            return Class.forName(getStructure().getStructureName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getPayload() {

        return payload;
    }

}
