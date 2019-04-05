package org.sartframework.query;

import org.sartframework.session.SystemSnapshot;

public abstract class AbstractQuery implements DomainQuery {

    private String queryKey;
    
    private boolean querySubscription;
    
    private long queryXid;
    
    private SystemSnapshot systemSnapshot;
    
    private int isolation;
    
    public AbstractQuery() {
        super();
    }
    
   
    @Override
    public final String getQueryKey() {
        return queryKey;
    }
    
    @Override
    public final boolean isQuerySubscription() {
        return querySubscription;
    }
    
    @Override
    public final long getQueryXid() {
        return queryXid;
    }

    
    public SystemSnapshot getSystemSnapshot() {
        return systemSnapshot;
    }

   
    public final void setQuerySubscription(boolean subscription) {
        this.querySubscription = subscription;
    }
    
    public final void setQueryKey(String queryKey) {
        this.queryKey = queryKey;

    }

    public final void setQueryXid(long queryXid) {
        this.queryXid = queryXid;

    }
    
    public void setSystemSnapshot(SystemSnapshot systemSnapshot) {
        this.systemSnapshot = systemSnapshot;
    }

    public int getIsolation() {
        return isolation;
    }

    public void setIsolation(int isolation) {
        this.isolation = isolation;
    }

}
