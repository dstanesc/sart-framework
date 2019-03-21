package org.sartframework.serializers.protostuff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.sartframework.serializers.ContentSerializer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ContentSerializerProtostuff<T> implements ContentSerializer<T> {

    final Schema<T> schema;

    public ContentSerializerProtostuff(Class<T> categoryClass) {
        this.schema = RuntimeSchema.getSchema(categoryClass);
    }

    @Override
    public T deserialize(byte[] data) {
        T t = schema.newMessage();
        ProtobufIOUtil.mergeFrom(data, t, schema);
        return t;
    }

    @Override
    public byte[] serialize(T t) {
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            LinkedBuffer buffer = LinkedBuffer.allocate();
            ProtobufIOUtil.writeTo(outputStream, t, schema, buffer);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
