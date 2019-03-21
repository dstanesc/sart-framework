package org.sartframework.result;

public abstract class MarkerResult implements QueryResult {

    String queryKey;
    
    long resultCreationTime;

    public MarkerResult() {
        super();
    }

    public MarkerResult(String queryKey) {
        super();
        this.queryKey = queryKey;
        this.resultCreationTime = System.currentTimeMillis();
    }

    @Override
    public String getQueryKey() {
        
        return queryKey;
    }

    public void setQueryKey(String queryKey) {
        
        this.queryKey = queryKey;
    }

    @Override
    public long getResultCreationTime() {

        return resultCreationTime;
    }

    public void setResultCreationTime(long resultCreationTime) {
        
        this.resultCreationTime = resultCreationTime;
    }

}
