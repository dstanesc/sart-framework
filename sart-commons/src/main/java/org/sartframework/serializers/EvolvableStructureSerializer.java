package org.sartframework.serializers;

import org.sartframework.serializers.protostuff.EnvelopeSerializerProtostuff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvolvableStructureSerializer<T> implements GenericSerializer<T> {
    
    final static Logger LOGGER = LoggerFactory.getLogger(EvolvableStructureSerializer.class);

    final EnvelopeSerializer envelopeSerializer;

    final PlatformOperationRegistry serializerRegistry = PlatformOperationRegistry.get();

    public EvolvableStructureSerializer() {
        super();
        this.envelopeSerializer = new EnvelopeSerializerProtostuff();
    }

    public T deserialize(byte[] data) {

        if (data == null)
            return null;

        SerializedStructure serializedStructure = envelopeSerializer.deserialize(data);

        String structureIdentity = serializedStructure.getStructureIdentity();

        int structureVersion = serializedStructure.getStructureVersion();

        LOGGER.debug("Deserializing identity={}, ver={}", structureIdentity, structureVersion);
        
        byte[] payload = serializedStructure.getPayload();

        ContentSerializer<T> contentSerializer = serializerRegistry.getContentSerializer(structureIdentity, structureVersion, true);

        T deserialized = contentSerializer.deserialize(payload);
        
        //check type safety w/ Spring's ParametrizedTypeReference
        @SuppressWarnings("unchecked")
        Class<T> operationClass = (Class<T>) deserialized.getClass();
        
        LOGGER.debug("Deserialized class={} ", operationClass);
        
        if(serializerRegistry.hasAdapter(operationClass)) {
            
            Adapter<T> adapter = serializerRegistry.getAdapter(operationClass);
            
            LOGGER.info("Executing adapter class={} ", adapter);
            
            adapter.adapt(deserialized);
        } 
        
        return deserialized;
    }

    @SuppressWarnings("unchecked")
    public byte[] serialize(T t) {

        if (t == null)
            return null;

        Class<T> operationClass = (Class<T>) t.getClass();
        
        EvolvableStructure<T> versionedStructure = serializerRegistry.getEvolvableStructureByJavaClass(operationClass);

        if (versionedStructure == null)
            throw new UnsupportedOperationException("Cannot serialize " + t.getClass() + " please register before usage");

        String structureIdentity = versionedStructure.getIdentity();

        int structureVersion = versionedStructure.getVersion();
        
        LOGGER.debug("Serializing identity={}, ver={}, class={}", structureIdentity, structureVersion, operationClass);

        ContentSerializer<T> contentSerializer = serializerRegistry.getContentSerializer(structureIdentity, structureVersion, false);

        byte[] payload = contentSerializer.serialize(t);

        SerializedStructure serializedStructure = new SerializedStructure(structureIdentity, structureVersion, payload);

        return envelopeSerializer.serialize(serializedStructure);
    }

}
