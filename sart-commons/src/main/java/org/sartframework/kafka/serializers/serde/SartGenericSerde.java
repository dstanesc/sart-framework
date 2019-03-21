package org.sartframework.kafka.serializers.serde;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.sartframework.kafka.serializers.SartGenericSerializer;

public class SartGenericSerde<T> implements Serde<T> {

    SartGenericSerializer<T> worker = new SartGenericSerializer<T>();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

        worker.configure(configs, isKey);
    }

    @Override
    public void close() {
        
        worker.close();
    }

    @Override
    public Serializer<T> serializer() {
        
        return worker;
    }

    @Override
    public Deserializer<T> deserializer() {

        return worker;
    }

}
