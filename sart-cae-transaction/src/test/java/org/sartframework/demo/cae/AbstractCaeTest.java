package org.sartframework.demo.cae;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;

public class AbstractCaeTest {

    enum TestStatus {
        ABORTED, COMMITTED
    }

    static class InputDeckMonitor {

        Long commandCreationTime;

        Long eventCreationTime;

        Long entityCreationTime;

        Long queryReturnTime;

        Long resultCreationTime;

        public Long getCommandCreationTime() {
            return commandCreationTime;
        }

        public void setCommandCreationTime(Long commandCreationTime) {
            this.commandCreationTime = commandCreationTime;
        }

        public Long getEventCreationTime() {
            return eventCreationTime;
        }

        public void setEventCreationTime(Long eventCreationTime) {
            this.eventCreationTime = eventCreationTime;
        }

        public Long getEntityCreationTime() {
            return entityCreationTime;
        }

        public void setEntityCreationTime(Long entityCreationTime) {
            this.entityCreationTime = entityCreationTime;
        }

        public Long getResultCreationTime() {
            return resultCreationTime;
        }

        public void setResultCreationTime(Long resultCreationTime) {
            this.resultCreationTime = resultCreationTime;
        }

        public Long getQueryReturnTime() {
            return queryReturnTime;
        }

        public void setQueryReturnTime(Long queryReturnTime) {
            this.queryReturnTime = queryReturnTime;
        }

    }


    static AtomicInteger inputDeckCounter = new AtomicInteger(1);

    static AtomicInteger resultCounter = new AtomicInteger(1);

    static AtomicInteger fileCounter = new AtomicInteger(1);

    
    protected InputDeckUpdateFileCommand buildInputDeckUpdateFileCommand(String id, long inputDeckVersion, String inputDeckFile) {

        return new InputDeckUpdateFileCommand(id, inputDeckVersion, inputDeckFile);
    }

    protected String nextInputDeckIdentity() {
        int i = inputDeckCounter.incrementAndGet();
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        return "ID_" + i + "_" + timeInMillis;
    }

    protected String nextResultIdentity() {
        int i = resultCounter.incrementAndGet();
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        return "R_" + i + "_" + timeInMillis;
    }

    protected String nextFileIdentity() {
        int i = fileCounter.incrementAndGet();
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        return "FILE_" + i + "_" + timeInMillis;
    }

}
