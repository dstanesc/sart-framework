package org.sartframework.event;

import java.time.Instant;

import org.sartframework.command.DomainCommand;

public abstract class GenericDomainEvent<C extends DomainCommand> extends GenericEvent implements DomainEvent<C> {

    long xcs;

    String aggregateKey;

    long sourceAggregateVersion;
    
    long targetAggregateVersion;
    
    
    public GenericDomainEvent() {
        super();
    }

    public GenericDomainEvent(String aggregateKey, long sourceAggregateVersion) {
        super();
        this.aggregateKey = aggregateKey;
        this.sourceAggregateVersion = sourceAggregateVersion;
    }

   
    @Override
    public long getXcs() {

        return xcs;
    }

    @Override
    public void setXcs(long xcs) {

        this.xcs = xcs;
    }

    @Override
    public long getSourceAggregateVersion() {

        return sourceAggregateVersion;
    }

    public void setAggregateKey(String aggregateKey) {

        this.aggregateKey = aggregateKey;
    }

    @Override
    public String getAggregateKey() {

        return aggregateKey;
    }

    @Override
    public long getTargetAggregateVersion() {
        return targetAggregateVersion;
    }
    
    @Override
    public void setTargetAggregateVersion(long targetAggregateVersion) {
        this.targetAggregateVersion = targetAggregateVersion;
    }

    public DomainEvent<C> addTransactionHeader(long xid, long xcs) {
        
        setXid(xid);
        
        setXcs(xcs);
        
        setCreationTime(System.currentTimeMillis());

        return this;
    }
}
