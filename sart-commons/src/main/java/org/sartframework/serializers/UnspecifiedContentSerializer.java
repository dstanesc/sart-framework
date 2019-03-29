package org.sartframework.serializers;

public class UnspecifiedContentSerializer<T> implements ContentSerializer<T> {

    @Override
    public T deserialize(byte[] data) {
        throw new UnsupportedOperationException("Please register a default platform serializer");
    }

    @Override
    public byte[] serialize(T t) {
        throw new UnsupportedOperationException("Please register a default platform serializer");
    }

}
