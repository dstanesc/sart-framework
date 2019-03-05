package org.sartframework.projection;

import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@IdClass(EntityIdentity.class)
public abstract class ProjectedEntity {
    
    public final static long XMAX_NOT_SET = -1;

    @Id
    protected String aggregateKey;

    @Id
    protected long aggregateVersion;
    
    protected long xmin;

    protected long xmax = XMAX_NOT_SET;
    
    protected long entityCreationTime; 

    public ProjectedEntity() {
        super();
    }

    public ProjectedEntity(long xid, String aggregateKey, long aggregateVersion) {
        super();
        this.xmin = xid;
        this.aggregateKey = aggregateKey;
        this.aggregateVersion = aggregateVersion;
        this.entityCreationTime = System.currentTimeMillis();
    }

    public long getXmin() {
        return xmin;
    }

    public void setXmin(long xid) {
        this.xmin = xid;
    }

    public String getAggregateKey() {
        return aggregateKey;
    }

    public void setAggregateKey(String id) {
        this.aggregateKey = id;
    }

    public long getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateVersion(long version) {
        this.aggregateVersion = version;
    }

    public long getXmax() {
        return xmax;
    }

    public void setXmax(long xmax) {
        this.xmax = xmax;
    }

    public long getEntityCreationTime() {
        return entityCreationTime;
    }

    public void setEntityCreationTime(long entityCreationTime) {
        this.entityCreationTime = entityCreationTime;
    }

}
