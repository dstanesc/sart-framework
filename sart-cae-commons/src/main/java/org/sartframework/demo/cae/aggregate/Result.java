package org.sartframework.demo.cae.aggregate;

public class Result {

    String resultId;

    String resultName;

    String resultFile;

    long xmax;


    public Result(String resultId, String resultName, String resultFile, long xmax) {
        super();
        this.resultId = resultId;
        this.resultName = resultName;
        this.resultFile = resultFile;
        this.xmax = xmax;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public long getXmax() {
        return xmax;
    }

    public void setXmax(long xmax) {
        this.xmax = xmax;
    }
}
