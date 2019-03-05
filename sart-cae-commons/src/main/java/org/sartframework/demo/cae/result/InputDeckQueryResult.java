package org.sartframework.demo.cae.result;

import java.io.Serializable;
import java.time.Instant;

import org.sartframework.result.QueryResult;

public class InputDeckQueryResult implements QueryResult {

    String queryKey;

    long xid;

    String inputDeckId;

    long inputDeckVersion;

    String inputDeckName;

    String inputDeckFile;

    long resultCreationTime;
    
    long entityCreationTime;
    
    public InputDeckQueryResult() {
        super();
    }

    public InputDeckQueryResult(String queryKey, long xid, String inputDeckId, long inputDeckVersion, long entityCreationTime, String inputDeckName, String inputDeckFile) {

        this.queryKey = queryKey;
        this.xid = xid;
        this.inputDeckId = inputDeckId;
        this.inputDeckVersion = inputDeckVersion;
        this.entityCreationTime = entityCreationTime;
        this.resultCreationTime = System.currentTimeMillis();
        this.inputDeckName = inputDeckName;
        this.inputDeckFile = inputDeckFile;
    }

    @Override
    public String getQueryKey() {
        return queryKey;
    }

    public void setQueryKey(String resultKey) {
        this.queryKey = resultKey;
    }

    public long getXid() {
        return xid;
    }

    public void setXid(long xid) {
        this.xid = xid;
    }

    public String getInputDeckId() {
        return inputDeckId;
    }

    public void setInputDeckId(String inputDeckId) {
        this.inputDeckId = inputDeckId;
    }

    public long getInputDeckVersion() {
        return inputDeckVersion;
    }

    public void setInputDeckVersion(long inputDeckVersion) {
        this.inputDeckVersion = inputDeckVersion;
    }

    public String getInputDeckName() {
        return inputDeckName;
    }

    public void setInputDeckName(String inputDeckName) {
        this.inputDeckName = inputDeckName;
    }

    public String getInputDeckFile() {
        return inputDeckFile;
    }

    public void setInputDeckFile(String inputDeckFile) {
        this.inputDeckFile = inputDeckFile;
    }

    public long getResultCreationTime() {
        return resultCreationTime;
    }

    public void setResultCreationTime(long resultCreationTime) {
        this.resultCreationTime = resultCreationTime;
    }

    public long getEntityCreationTime() {
        return entityCreationTime;
    }

    public void setEntityCreationTime(long entityCreationTime) {
        this.entityCreationTime = entityCreationTime;
    }

}
