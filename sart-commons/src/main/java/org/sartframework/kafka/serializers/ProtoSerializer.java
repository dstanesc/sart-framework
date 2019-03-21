package org.sartframework.kafka.serializers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.sartframework.serializers.ContentSerializer;
import org.sartframework.serializers.SerializedStructure;
import org.sartframework.serializers.protostuff.ContentSerializerProtostuff;
import org.springframework.util.Assert;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

//https://github.com/protostuff/

public class ProtoSerializer<T> implements Serializer<T>, Deserializer<T> {

    public final static Schema<SerializedStructure> GENERIC_SCHEMA = RuntimeSchema.getSchema(SerializedStructure.class);

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(String topic, byte[] data) {

        SerializedStructure genericSerialized = GENERIC_SCHEMA.newMessage();

        if (data != null) {
            ProtobufIOUtil.mergeFrom(data, genericSerialized, GENERIC_SCHEMA);
        }

      //  VersionedStructure serializedVersion = genericSerialized.getStructure();
        
//
//        Class<?> javaType = genericSerialized.getJavaType();

        return (T) genericSerialized.getPayload();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, T data) {

        Assert.notNull(data, "payload cannot be null");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            LinkedBuffer buffer = LinkedBuffer.allocate();
            
            ContentSerializer<T> contentSerializer = new ContentSerializerProtostuff<T>((Class<T>)data.getClass()); 
             
            byte[] byteArray = contentSerializer.serialize(data);
            
            SerializedStructure genericSerialized = new SerializedStructure(getStructureIdentity(data), getVersion(data), byteArray);

            ProtobufIOUtil.writeTo(outputStream, genericSerialized, GENERIC_SCHEMA, buffer);

            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Could not serialize data", e);
        }
    }

    private int getVersion(T data) {

        return 0;
    }

    private String getStructureIdentity(T data) {

        return data.getClass().getName();
    }

    @Override
    public void close() {
    }

}
