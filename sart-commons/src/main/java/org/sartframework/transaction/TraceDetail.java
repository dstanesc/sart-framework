package org.sartframework.transaction;

public class TraceDetail extends AbstractDetail {
    
    public final static String START_TRACE = "startTrace";  

    String hostName;
    
    String hostAddress;
    
    String className;
    
    String methodName;
    
    int lineNumber;
    
    StackTraceElement[] stackTrace;

    public TraceDetail() {
        super();
    }

    public TraceDetail(String name) {
        super(name);
    }

    public String getHostName() {
        return hostName;
    }

    public TraceDetail setHostName(String host) {
        this.hostName = host;
        return this;
    }
    
    public String getHostAddress() {
        return hostAddress;
    }

    public TraceDetail setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public TraceDetail setClassName(String enclosingClass) {
        this.className = enclosingClass;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public TraceDetail setMethodName(String enclosingMethod) {
        this.methodName = enclosingMethod;
        return this;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public TraceDetail setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public TraceDetail setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }
}
