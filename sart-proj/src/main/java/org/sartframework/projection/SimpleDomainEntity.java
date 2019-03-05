package org.sartframework.projection;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SimpleDomainEntity {
    
    public final static long XMAX_NOT_SET = -1;

    @Id
    protected String aggregateKey;

    protected long xid;

    protected long xmax = XMAX_NOT_SET;
    

    public SimpleDomainEntity() {
        super();
    }

    public SimpleDomainEntity(long xid, String aggregateKey) {
        super();
        this.xid = xid;
        this.aggregateKey = aggregateKey;
    }

    public long getXid() {
        return xid;
    }


    public void setXid(long xid) {
        this.xid = xid;
    }

    public String getAggregateKey() {
        return aggregateKey;
    }

    public void setAggregateKey(String id) {
        this.aggregateKey = id;
    }

   
    public long getXmax() {
        return xmax;
    }

    public void setXmax(long xmax) {
        this.xmax = xmax;
    }
}
