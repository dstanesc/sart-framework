package org.sartframework.command.transaction;

import org.sartframework.aggregate.TransactionAware;

public interface TransactionStatus extends TransactionAware {

    enum Status {
        UNKNOWN, CREATED, RUNNING, COMMITED, ABORTED
    }
    
    enum Isolation {
        READ_UNCOMMITTED, READ_COMMITTED, READ_SNAPSHOT
    }

    Status getStatus();
    
}