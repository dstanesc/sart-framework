package org.sartframework.error;

public class AbstractDomainError implements DomainError {

    long xid;
    
    long xcs;
    
    long creationTime;

    String aggregateKey;
    
    long aggregateVersion;

    public AbstractDomainError() {
        super();
    }

    public AbstractDomainError(String aggregateKey, long aggregateVersion) {
        super();
        this.aggregateKey = aggregateKey;
        this.aggregateVersion = aggregateVersion;
    }

    @Override
    public long getXid() {
        return xid;
    }
    
    public void setXid(long xid) {
        this.xid = xid;
    }
    
    @Override
    public String getAggregateKey() {
        return aggregateKey;
    }

    public void setAggregateKey(String aggregateKey) {
        this.aggregateKey = aggregateKey;
    }
    
    public long getXcs() {
        return xcs;
    }

    public void setXcs(long xcs) {
        this.xcs = xcs;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateVersion(long aggregateVersion) {
        this.aggregateVersion = aggregateVersion;
    }

    public DomainError addTransactionHeader(long xid, long xcs) {
        
        setXid(xid);
        
        setXcs(xcs);
        
        setCreationTime(System.currentTimeMillis());

        return this;
    }
}
