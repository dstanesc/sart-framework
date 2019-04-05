package org.sartframework.session;

import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//https://github.com/FasterXML/jackson-annotations

@JsonIgnoreProperties(ignoreUnknown=true)
public class SystemSnapshot {
    
    String sid;
    
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

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "SystemSnapshot [sid=" + sid + ", timestamp=" + timestamp + ", highestCommitted=" + highestCommitted + ", running=" + running + "]";
    }

}
