package org.sartframework.event.transaction;

import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

public class ConflictResolvedEvent extends GenericEvent implements TransactionEvent {

    long xid;
    
    String aggregateKey;

    String changeKey;

    long winnerVersion;

    long otherVersion;

    long winnerXid;

    long otherXid;

    String winnerEvent;

    String otherEvent;

    public ConflictResolvedEvent() {
        super();
       
    }

    public ConflictResolvedEvent(long xid, String aggregateKey, String changeKey, long winnerVersion, long otherVersion, long winnerXid, long otherXid,
                                       String winnerEvent, String otherEvent) {
        super();
        this.xid = xid;
        this.aggregateKey = aggregateKey;
        this.changeKey = changeKey;
        this.winnerVersion = winnerVersion;
        this.otherVersion = otherVersion;
        this.winnerXid = winnerXid;
        this.otherXid = otherXid;
        this.winnerEvent = winnerEvent;
        this.otherEvent = otherEvent;
    }



    @Override
    public long getXid() {
        return xid;
    }



    public String getAggregateKey() {
        return aggregateKey;
    }



    public void setAggregateKey(String aggregateKey) {
        this.aggregateKey = aggregateKey;
    }



    public String getChangeKey() {
        return changeKey;
    }



    public void setChangeKey(String changeKey) {
        this.changeKey = changeKey;
    }



    public long getWinnerVersion() {
        return winnerVersion;
    }



    public void setWinnerVersion(long winnerVersion) {
        this.winnerVersion = winnerVersion;
    }



    public long getOtherVersion() {
        return otherVersion;
    }



    public void setOtherVersion(long otherVersion) {
        this.otherVersion = otherVersion;
    }



    public long getWinnerXid() {
        return winnerXid;
    }



    public void setWinnerXid(long winnerXid) {
        this.winnerXid = winnerXid;
    }



    public long getOtherXid() {
        return otherXid;
    }



    public void setOtherXid(long otherXid) {
        this.otherXid = otherXid;
    }



    public String getWinnerEvent() {
        return winnerEvent;
    }



    public void setWinnerEvent(String winnerEvent) {
        this.winnerEvent = winnerEvent;
    }



    public String getOtherEvent() {
        return otherEvent;
    }



    public void setOtherEvent(String otherEvent) {
        this.otherEvent = otherEvent;
    }

}
