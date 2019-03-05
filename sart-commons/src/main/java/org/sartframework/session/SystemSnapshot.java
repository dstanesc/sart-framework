package org.sartframework.session;

import java.util.SortedSet;
import java.util.TreeSet;

public class SystemSnapshot {
    
    Long timestamp;

    Long highestCommitted;
    
    SortedSet<Long> running;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getHighestCommitted() {
        return highestCommitted;
    }

    public void setHighestCommitted(Long highestCommitted) {
        this.highestCommitted = highestCommitted;
    }


    public SortedSet<Long> getRunning() {
        if(running == null) return new TreeSet<Long>();
        return running;
    }

    public void setRunning(SortedSet<Long> running) {
        this.running = running;
    }
    
    public boolean isEmpty() {
        return highestCommitted == null;
    }

    @Override
    public String toString() {
        return "SystemSnapshot [timestamp=" + timestamp + ", highestCommitted=" + highestCommitted + ", running=" + running + "]";
    }


}
