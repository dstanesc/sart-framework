package org.sartframework.session;

import java.util.SortedSet;
import java.util.TreeSet;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.TransactionEvent;

@Evolvable(version = 1)
public class RunningTransactions {
    
    TransactionEvent lastEvent;
    
    Long highestCommitted = -1L;

    SortedSet<Long> txn;

    public RunningTransactions() {
        this.txn = new TreeSet<>();
    }

    public void add(Long xid) {
        txn.add(xid);
    }

    public void remove(Long xid) {
        txn.remove(xid);
    }

    public SortedSet<Long> getTxn() {
        return txn;
    }
    
    public TransactionEvent getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(TransactionEvent lastEvent) {
        this.lastEvent = lastEvent;
    }

    public Long getHighestCommitted() {
        return highestCommitted;
    }

    public void setHighestCommitted(Long higestCommited) {
        this.highestCommitted = higestCommited;
    }

    public void updateHighestCommited(Long other) {
        if(other > highestCommitted) {
            setHighestCommitted(other);
        }
    }
}