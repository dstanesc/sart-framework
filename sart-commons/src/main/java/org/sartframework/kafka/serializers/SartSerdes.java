package org.sartframework.kafka.serializers;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class SartSerdes {
    
    public static <T> Serde<T> Proto() {
        return new ProtoSerde<T>();
    }
    
    public static  Serde<String> String() {
        return Serdes.String();
    }
    
    public static  Serde<Long> Long() {
        return Serdes.Long();
    }
}
