package org.sartframework.demo.cae;

import org.apache.kafka.common.serialization.Serde;
import org.junit.Assert;
import org.junit.Test;
import org.sartframework.aggregate.DomainAggregate;
import org.sartframework.annotation.Evolvable;
import org.sartframework.command.GenericCreateAggregateCommand;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.serializers.Adapter;
import org.sartframework.serializers.PlatformOperationRegistry;

public class SerializerPolymorphTest {

    @Evolvable(identity = "test.command.create", version = 1)
    static class MyCommand1 extends GenericCreateAggregateCommand<MyCommand1> {

        String inputDeckName;

        String inputDeckFile;

        public MyCommand1() {
            super();
        }

        public MyCommand1(String aggregateKey, String inputDeckName, String inputDeckFile) {
            super(aggregateKey, 0);
            this.inputDeckName = inputDeckName;
            this.inputDeckFile = inputDeckFile;
        }

        @Override
        public Class<? extends DomainAggregate> getAggregateType() {
            throw new UnsupportedOperationException();
        }

        public String getInputDeckName() {
            return inputDeckName;
        }

        public void setInputDeckName(String inputDeckName) {
            this.inputDeckName = inputDeckName;
        }

        public String getInputDeckFile() {
            return inputDeckFile;
        }

        public void setInputDeckFile(String inputDeckFile) {
            this.inputDeckFile = inputDeckFile;
        }
    }
    
    @Evolvable(identity = "test.command.create", version = 2)
    static class MyCommand2 extends MyCommand1 {

        String phase;

        public MyCommand2() {
            super();
        }

        public MyCommand2(String aggregateKey, String inputDeckName, String inputDeckFile, String phase) {
            super(aggregateKey, inputDeckName, inputDeckFile);
            this.phase = phase;
        }

        @Override
        public Class<? extends DomainAggregate> getAggregateType() {
            throw new UnsupportedOperationException();
        }

        public String getPhase() {
            return phase;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public String getInputDeckName() {
            return inputDeckName;
        }

        public void setInputDeckName(String inputDeckName) {
            this.inputDeckName = inputDeckName;
        }

        public String getInputDeckFile() {
            return inputDeckFile;
        }

        public void setInputDeckFile(String inputDeckFile) {
            this.inputDeckFile = inputDeckFile;
        }
    }
    
    
    static class PhaseAdapter implements Adapter<MyCommand2> {

        public void adapt(MyCommand2 input) {

            if (input.getPhase() == null)
                input.setPhase(DEFAULT_PHASE_VALUE);
        }
    }

    private static final String DEFAULT_PHASE_VALUE = "DefaultPhaseValue";

    @Test
    public void serializationTest1() throws Exception {

        new PlatformOperationRegistry().registerDefaults().init();

        MyCommand1 c = new MyCommand1("myId", "myName", "myFile");
        c.setXid(3);
        c.setXcs(5);

        Serde<MyCommand1> domainCommandSerde = SartSerdes.DomainCommandSerde();

        byte[] data = domainCommandSerde.serializer().serialize("irrelevant", c);

        MyCommand1 d = domainCommandSerde.deserializer().deserialize("irelevant", data);

        // version = 2 available, should pick MyCommand2
        
        Assert.assertEquals(MyCommand2.class, d.getClass());

        Assert.assertEquals(3, d.getXid());

        Assert.assertEquals(5, d.getXcs());

        Assert.assertEquals("myId", d.getAggregateKey());

        Assert.assertEquals("myName", d.getInputDeckName());

        Assert.assertEquals("myFile", d.getInputDeckFile());

        MyCommand2 dd = (MyCommand2) d;

        Assert.assertNull(dd.getPhase());
    }

    @Test
    public void serializationTest2() throws Exception {

        new PlatformOperationRegistry().registerDefaults().registerAdapter(MyCommand2.class, new PhaseAdapter()).init();

        MyCommand1 c = new MyCommand1("myId", "myName", "myFile");
        c.setXid(3);
        c.setXcs(5);

        Serde<MyCommand1> domainCommandSerde = SartSerdes.DomainCommandSerde();

        byte[] data = domainCommandSerde.serializer().serialize("irrelevant", c);

        MyCommand1 d = domainCommandSerde.deserializer().deserialize("irelevant", data);

        // version = 2 available(aka MyCommand2), should pick latest class and execute PhaseAdapter,

        Assert.assertEquals(MyCommand2.class, d.getClass());

        Assert.assertEquals(3, d.getXid());

        Assert.assertEquals(5, d.getXcs());

        Assert.assertEquals("myId", d.getAggregateKey());

        Assert.assertEquals("myName", d.getInputDeckName());

        Assert.assertEquals("myFile", d.getInputDeckFile());

        MyCommand2 dd = (MyCommand2) d;

        Assert.assertEquals(DEFAULT_PHASE_VALUE, dd.getPhase());
    }

}
