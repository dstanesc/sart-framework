package org.sartframework.kafka.serializers;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.sartframework.serializers.EvolvableStructureSerializer;

public class SartGenericSerializer <T> implements Serializer<T>, Deserializer<T> {

    EvolvableStructureSerializer<T> worker = new EvolvableStructureSerializer<T>();
    
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}
    
    @Override
    public T deserialize(String topic, byte[] data) {
        
        return worker.deserialize(data);
    }
    
    @Override
    public byte[] serialize(String topic, T data) {
      
        return worker.serialize(data);
    }

    @Override
    public void close() {}

}
