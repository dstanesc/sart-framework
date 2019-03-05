package org.sartframework.query;

import org.sartframework.session.SystemSnapshot;

public interface DomainQuery {

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
