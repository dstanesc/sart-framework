package org.sartframework.transaction;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceDetailFactory implements DetailFactory<TraceDetail>{

    final static Logger LOGGER = LoggerFactory.getLogger(TraceDetailFactory.class);

    public TraceDetail collect(String name) {

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        StackTraceElement[] stackTraceRange = Arrays.copyOfRange(stackTrace, 0, 10);

        StackTraceElement relevantCaller = findRelevantTraceElement(stackTraceRange);

        String className = relevantCaller.getClassName();

        String methodName = relevantCaller.getMethodName();

        int lineNumber = relevantCaller.getLineNumber();

        String hostAddress = null;

        String hostName = null;

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostAddress = localHost.getHostAddress();
            hostName = localHost.getHostName();
        } catch (UnknownHostException e) {
            // ignore missing info
            LOGGER.error("Could not determine host", e);
        }

        TraceDetail execDetail = new TraceDetail(name)

            .setStackTrace(stackTraceRange).setHostName(hostName).setHostAddress(hostAddress).setClassName(className).setMethodName(methodName)
            .setLineNumber(lineNumber);

        return execDetail;
    }

    private StackTraceElement findRelevantTraceElement(StackTraceElement[] stackTrace) {

        //FIXME elaborate robust solution
        
        StackTraceElement secondElement = stackTrace[3];

        if (secondElement.getClassName().equals("org.sartframework.driver.DefaultDomainTransaction")) {

            return stackTrace[8];

        } else
            return secondElement;

    }
}
