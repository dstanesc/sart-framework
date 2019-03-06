package org.sartframework.query;

import org.sartframework.session.SystemSnapshot;

public interface DomainQuery {
    
    public final static int READ_UNCOMMITTED_ISOLATION = 1;
    
    public final static int READ_COMMITTED_ISOLATION = 2;
    
    public final static int READ_SNAPSHOT_ISOLATION = 4;

    boolean isQuerySubscription();

    String getQueryKey();
    
    long getQueryXid();
    
    SystemSnapshot getSystemSnapshot();
    
    int getIsolation();
    
    void setQuerySubscription(boolean subscription);
    
    void setQueryKey(String queryKey);
    
    void setQueryXid(long queryXid);
    
    void setSystemSnapshot(SystemSnapshot systemSnapshot);
    
    void setIsolation(int isolation);

}
