package org.sartframework.command;

import org.sartframework.aggregate.Transactional;

public abstract class GenericDomainCommand<T extends GenericDomainCommand<?>> implements DomainCommand,  Transactional {

    protected Long xid;

    protected Long xcs;
    
    protected String aggregateKey;

    protected Long aggregateVersion;
    
    protected long creationTime;

    public GenericDomainCommand() {
        super();
    }

    public GenericDomainCommand(String aggregateKey, long aggregateVersion) {
        super();
        this.aggregateKey = aggregateKey;
        this.aggregateVersion = aggregateVersion;
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public String getAggregateKey() {
        return aggregateKey;
    }

    @Override
    public long getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateKey(String aggregateKey) {
        this.aggregateKey = aggregateKey;
    }

    public void setAggregateVersion(long aggregateVersion) {
        this.aggregateVersion = aggregateVersion;
    }
    
    @Override
    public long getXid() {
        return xid;
    }

    @Override
    public void setXid(long xid) {
        this.xid = xid;
    }

    @Override
    public long getXcs() {
        return xcs;
    }

    @Override
    public void setXcs(long xcs) {
        this.xcs = xcs;
    }
    
   
    @SuppressWarnings("unchecked")
    public T addTransactionHeader(long xid, long xcs) {
        setXid(xid);
        setXcs(xcs);
        return (T) this;
    }

    
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public String toString() {
        return "GenericDomainCommand [xid=" + xid + ", xcs=" + xcs + ", aggregateKey=" + aggregateKey + ", aggregateVersion="
            + aggregateVersion + "]";
    }

}
