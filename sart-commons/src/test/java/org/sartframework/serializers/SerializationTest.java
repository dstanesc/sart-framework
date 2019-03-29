package org.sartframework.serializers;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.aggregate.AnnotatedDomainAggregate;
import org.sartframework.aggregate.DomainAggregate;
import org.sartframework.annotation.Evolvable;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.GenericCreateAggregateCommand;
import org.sartframework.command.GenericDestructAggregateCommand;
import org.sartframework.event.GenericAggregateCreatedEvent;
import org.sartframework.kafka.serializers.ProtoSerializer;
import org.sartframework.serializers.protostuff.ContentSerializerProtostuff;


public class SerializationTest {

    @Evolvable(identity="test.foo", version = 1)
    class FooAggregate extends AnnotatedDomainAggregate {
    }

    @Evolvable(identity="test.foo", version=1)
    public static class FooCmd extends GenericCreateAggregateCommand<GenericCreateAggregateCommand<FooCmd>> {

        public FooCmd() {
            super();
        }

        public FooCmd(String aggregateKey, long aggregateVersion) {
            super(aggregateKey, aggregateVersion);
        }

        @Override
        public Class<? extends DomainAggregate> getAggregateType() {
            return FooAggregate.class;
        }
    }
    
    @Evolvable(identity="test.foo", version=2)
    public static class FooCmd2 extends GenericCreateAggregateCommand<GenericCreateAggregateCommand<FooCmd>> {

        String foo2Attr; 
        
        public FooCmd2() {
            super();
        }

        public FooCmd2(String aggregateKey, long aggregateVersion,  String foo2Attr) {
            super(aggregateKey, aggregateVersion);
            this.foo2Attr = foo2Attr;
        }

        @Override
        public Class<? extends DomainAggregate> getAggregateType() {
            return FooAggregate.class;
        }
    }
    
    

    class DelFooCmd extends GenericDestructAggregateCommand<DelFooCmd> {

        public DelFooCmd() {
            super();
        }

        public DelFooCmd(String aggregateKey, long aggregateVersion) {
            super(aggregateKey, aggregateVersion);
        }
    }

    class FooEvt extends GenericAggregateCreatedEvent<DelFooCmd> {

        public FooEvt() {
            super();
        }

        public FooEvt(String aggregateKey, long aggregateVersion) {
            super(aggregateKey, aggregateVersion);
        }

        @Override
        public DelFooCmd undo(long xid, long xcs) {

            throw new UnsupportedOperationException();
        }

        @Override
        public String getChangeKey() {

            return getAggregateKey();
        }

    }

    @SuppressWarnings("resource")
    //@Test
    public void test1() {
        ProtoSerializer<FooCmd> ser = new ProtoSerializer<FooCmd>();
        FooCmd cmd1 = new FooCmd("key1", 1001);
        byte[] serialized = ser.serialize(null, cmd1);
        FooCmd cmd2 = ser.deserialize(null, serialized);
        Assert.assertEquals("Key compare", cmd1.getAggregateKey(), cmd2.getAggregateKey());
        Assert.assertEquals("Version compare", cmd1.getAggregateVersion(), cmd2.getAggregateVersion());
    }
    
    @SuppressWarnings("resource")
   // @Test
    public void test2() {
        ProtoSerializer<FooCmd> ser = new ProtoSerializer<FooCmd>();
        FooCmd cmd1 = new FooCmd("key1", 1001);
        byte[] serialized = ser.serialize(null, cmd1);
        ProtoSerializer<FooCmd2> ser2 = new ProtoSerializer<FooCmd2>();
        FooCmd2 cmd2 = ser2.deserialize(null, serialized);
        Assert.assertEquals("Key compare", cmd1.getAggregateKey(), cmd2.getAggregateKey());
        Assert.assertEquals("Version compare", cmd1.getAggregateVersion(), cmd2.getAggregateVersion());
    }
    
    @SuppressWarnings("resource")
    //@Test
    public void test3() {
        ProtoSerializer<GenericCreateAggregateCommand<?>> ser = new ProtoSerializer<GenericCreateAggregateCommand<?>>();
        FooCmd cmd1 = new FooCmd("key1", 1001);
        byte[] serialized = ser.serialize(null, cmd1);
        ProtoSerializer<GenericCreateAggregateCommand<?>> ser2 = new ProtoSerializer<GenericCreateAggregateCommand<?>>();
        GenericCreateAggregateCommand cmd2 = ser2.deserialize(null, serialized);
        Assert.assertEquals("Key compare", cmd1.getAggregateKey(), cmd2.getAggregateKey());
        Assert.assertEquals("Version compare", cmd1.getAggregateVersion(), cmd2.getAggregateVersion());
    }
    
    @Test
    public void test4() {
     
        new PlatformOperationRegistry()
        .registerDefaultContentSerializer(ContentSerializerProtostuff.class)
        .registerOperationCategory(DomainCommand.class, "org.sartframework")
        .init();

        EvolvableStructureSerializer<DomainCommand> domainCommandSerializer = new EvolvableStructureSerializer<>();
        FooCmd cmd1 = new FooCmd("key1", 1001);
        byte[] byteArray = domainCommandSerializer.serialize(cmd1);
        DomainCommand cmd2 = domainCommandSerializer.deserialize(byteArray);
        System.out.println("In class " + cmd1.getClass());
        System.out.println("Out class " + cmd2.getClass());
        Assert.assertEquals("Key compare", cmd1.getAggregateKey(), cmd2.getAggregateKey());
        Assert.assertEquals("Version compare", cmd1.getAggregateVersion(), cmd2.getAggregateVersion());
    }
    
}
