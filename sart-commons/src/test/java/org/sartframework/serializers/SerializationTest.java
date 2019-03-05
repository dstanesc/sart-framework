package org.sartframework.serializers;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.aggregate.AnnotatedDomainAggregate;
import org.sartframework.aggregate.DomainAggregate;
import org.sartframework.command.GenericCreateAggregateCommand;
import org.sartframework.command.GenericDestructAggregateCommand;
import org.sartframework.event.GenericAggregateCreatedEvent;
import org.sartframework.kafka.serializers.ProtoSerializer;


public class SerializationTest {

    class FooAggregate extends AnnotatedDomainAggregate {
    }

    class FooCmd extends GenericCreateAggregateCommand<GenericCreateAggregateCommand<FooCmd>> {

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

    @Test
    public void test1() {
        ProtoSerializer<FooCmd> ser = new ProtoSerializer<FooCmd>();
        FooCmd cmd1 = new FooCmd("key1", 1001);
        byte[] serialized = ser.serialize(null, cmd1);
        FooCmd cmd2 = ser.deserialize(null, serialized);
        Assert.assertEquals("Key compare", cmd1.getAggregateKey(), cmd2.getAggregateKey());
        Assert.assertEquals("Version compare", cmd1.getAggregateVersion(), cmd2.getAggregateVersion());
    }
}
