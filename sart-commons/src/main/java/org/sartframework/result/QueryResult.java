package org.sartframework.result;

public interface QueryResult {

    
    public static String BROADCAST_RESULT_QUERY_KEY = "broadcastKey";
    
    String getSid();
    
    String getQueryKey();
    
    long getResultCreationTime();

}
