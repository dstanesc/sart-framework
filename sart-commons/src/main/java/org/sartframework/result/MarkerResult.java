package org.sartframework.result;

public abstract class MarkerResult implements QueryResult {

    String queryKey;
    
    long resultCreationTime;

    public MarkerResult() {
        super();
    }

    public MarkerResult(String resultKey) {
        super();
        this.queryKey = resultKey;
        this.resultCreationTime = System.currentTimeMillis();
    }

    @Override
    public String getQueryKey() {
        
        return queryKey;
    }

    public void setQueryKey(String resultKey) {
        this.queryKey = resultKey;
    }

    @Override
    public long getResultCreationTime() {

        return resultCreationTime;
    }

    public void setResultCreationTime(long resultCreationTime) {
        
        this.resultCreationTime = resultCreationTime;
    }

}
