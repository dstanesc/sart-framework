package org.sartframework.result;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.transaction.ConflictResolvedEvent;

@Evolvable(version = 1)
public class ConflictResolvedResult extends ConflictResolvedEvent implements QueryResult {
    
    String sid;

    String queryKey;

    long resultCreationTime;
    
    public ConflictResolvedResult() {
        super();
    }

    public ConflictResolvedResult(String sid, String resultKey, long xid, String aggregateKey, String changeKey, long winnerVersion, long otherVersion, long winnerXid,
                                  long otherXid, String winnerEvent, String otherEvent) {
        super(xid, aggregateKey, changeKey, winnerVersion, otherVersion, winnerXid, otherXid, winnerEvent, otherEvent);
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

}
