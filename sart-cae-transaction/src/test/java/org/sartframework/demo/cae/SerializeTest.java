package org.sartframework.demo.cae;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.kafka.serializers.ProtoSerializer;

public class SerializeTest {

    class LongPrimitives {

        Long a;

        public LongPrimitives() {
            super();
        }

        public long getA() {
            return a;
        }

        public void setA(long a) {
            this.a = a;
        }
    }

    class AbstractPrimitives extends LongPrimitives {

    }

    class Primitives extends AbstractPrimitives {

        String b;

        public Primitives() {
            super();
        }

        public Primitives(String b) {
            super();
            this.b = b;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void serializationTest1() throws Exception {

        Primitives c = new Primitives("myFile");
        c.setA(7);

        byte[] bytes = new ProtoSerializer<Primitives>().serialize(null, c);

        Primitives d = new ProtoSerializer<Primitives>().deserialize(null, bytes);

        Assert.assertEquals("myFile", d.getB());

        Assert.assertEquals(7, d.getA());
    }

    @SuppressWarnings("resource")
    @Test
    public void serializationTest2() throws Exception {

        InputDeckCreateCommand c = new InputDeckCreateCommand("myId", "myName", "myFile");
        c.setXid(3);
        c.setXcs(5);

        byte[] bytes = new ProtoSerializer<InputDeckCreateCommand>().serialize(null, c);

        InputDeckCreateCommand d = new ProtoSerializer<InputDeckCreateCommand>().deserialize(null, bytes);

        Assert.assertEquals(3, d.getXid());

        Assert.assertEquals(5, d.getXcs());
    }

}
