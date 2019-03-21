package org.sartframework.serializers.protostuff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.sartframework.serializers.EnvelopeSerializer;
import org.sartframework.serializers.SerializedStructure;

import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.UninitializedMessageException;

public class EnvelopeSerializerProtostuff implements EnvelopeSerializer {

    public static class SerializedStructureSchema implements Schema<SerializedStructure> {

        public final static int STRUCTURE_IDENTITY_INDEX = 1;

        public final static int STRUCTURE_VERSION_INDEX = 2;

        public final static int PAYLOAD_INDEX = 3;

        public final static String STRUCTURE_IDENTITY_NAME = "structureIdentity";

        public final static String STRUCTURE_VERSION_NAME = "structureVersion";

        public final static String PAYLOAD_NAME = "payload";

        @Override
        public String getFieldName(int number) {
            switch (number) {
                case STRUCTURE_IDENTITY_INDEX:
                    return STRUCTURE_IDENTITY_NAME;
                case STRUCTURE_VERSION_INDEX:
                    return STRUCTURE_VERSION_NAME;
                case PAYLOAD_INDEX:
                    return PAYLOAD_NAME;
                default:
                    throw new IllegalArgumentException("Unknown index " + number);
            }
        }

        @Override
        public int getFieldNumber(String name) {
            switch (name) {
                case STRUCTURE_IDENTITY_NAME:
                    return STRUCTURE_IDENTITY_INDEX;
                case STRUCTURE_VERSION_NAME:
                    return STRUCTURE_VERSION_INDEX;
                case PAYLOAD_NAME:
                    return PAYLOAD_INDEX;
                default:
                    throw new IllegalArgumentException("Unknown name " + name);
            }
        }

        @Override
        public boolean isInitialized(SerializedStructure message) {

            return message.getStructureIdentity() != null;
        }

        @Override
        public SerializedStructure newMessage() {

            return new SerializedStructure();
        }

        @Override
        public String messageName() {

            return "envelope";
        }

        @Override
        public String messageFullName() {

            return "envelope";
        }

        @Override
        public Class<? super SerializedStructure> typeClass() {

            return SerializedStructure.class;
        }

        @Override
        public void mergeFrom(Input input, SerializedStructure message) throws IOException {
            while (true) {
                int number = input.readFieldNumber(this);
                switch (number) {
                    case 0: // indicates end of serialization
                        return;
                    case STRUCTURE_IDENTITY_INDEX:
                        message.setStructureIdentity(input.readString());
                        break;
                    case STRUCTURE_VERSION_INDEX:
                        message.setStructureVersion(input.readInt32());
                        break;
                    case PAYLOAD_INDEX:
                        message.setPayload(input.readByteArray());
                        break;
                    default:
                        input.handleUnknownField(number, this);
                        break;
                }
            }
        }

        @Override
        public void writeTo(Output output, SerializedStructure message) throws IOException {
            
            if (message.getStructureIdentity() == null)
                throw new UninitializedMessageException(message, this);

            output.writeString(STRUCTURE_IDENTITY_INDEX, message.getStructureIdentity(), false);

            output.writeInt32(STRUCTURE_VERSION_INDEX, message.getStructureVersion(), false);

            output.writeByteArray(PAYLOAD_INDEX, message.getPayload(), false);
        }
    }

    static Schema<SerializedStructure> SERIALIZED_STRUCTURE_SCHEMA = new SerializedStructureSchema();

    public SerializedStructure deserialize(byte[] data) {

        SerializedStructure serializedStructure = SERIALIZED_STRUCTURE_SCHEMA.newMessage();

        if (data != null) {
            ProtobufIOUtil.mergeFrom(data, serializedStructure, SERIALIZED_STRUCTURE_SCHEMA);
        }

        return serializedStructure;
    }

    public byte[] serialize(SerializedStructure serializedStructure) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            LinkedBuffer buffer = LinkedBuffer.allocate();

            ProtobufIOUtil.writeTo(outputStream, serializedStructure, SERIALIZED_STRUCTURE_SCHEMA, buffer);

            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Could not serialize data", e);
        }
    }
}
