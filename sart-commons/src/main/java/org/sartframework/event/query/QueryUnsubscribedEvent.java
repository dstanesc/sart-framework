package org.sartframework.event.query;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.QueryEvent;

@Evolvable(version=1)
public class QueryUnsubscribedEvent implements QueryEvent {

    long creationTime;
    
    String queryKey;
    
    public QueryUnsubscribedEvent() {}

    
    public QueryUnsubscribedEvent(String queryKey) {
        super();
        this.queryKey = queryKey;
    }


    public void setQueryKey(String queryKey) {
        this.queryKey = queryKey;
    }


    @Override
    public String getQueryKey() {

        return queryKey;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

}
