package org.sartframework.result;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.transaction.ConflictResolvedData;

@Evolvable(version = 1)
public class ConflictResolvedResult implements QueryResult {
    
    String sid;

    String queryKey;

    long resultCreationTime;
    
    ConflictResolvedData data;
    
    public ConflictResolvedResult() {
        super();
    }

    public ConflictResolvedResult(String sid, String resultKey,  ConflictResolvedData data) {
        this.data = data;
        this.sid = sid;
        this.queryKey = resultKey;
        this.resultCreationTime = System.currentTimeMillis();
    }
    
    @Override
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setQueryKey(String resultKey) {
        this.queryKey = resultKey;
    }

    @Override
    public String getQueryKey() {
        return queryKey;
    }

    @Override
    public long getResultCreationTime() {
        return resultCreationTime;
    }

    public void setResultCreationTime(long resultCreationTime) {
        this.resultCreationTime = resultCreationTime;
    }

    public ConflictResolvedData getData() {
        return data;
    }

    public void setData(ConflictResolvedData data) {
        this.data = data;
    }

}
