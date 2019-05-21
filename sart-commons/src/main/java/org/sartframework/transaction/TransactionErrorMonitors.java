package org.sartframework.transaction;

import java.io.Closeable;
import java.io.IOException;

import org.sartframework.error.transaction.TransactionError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.ReplayProcessor;

public class TransactionErrorMonitors implements Closeable {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionErrorMonitors.class);

    ReplayProcessor<TransactionError> transactionErrorMonitor = ReplayProcessor.<TransactionError> create();

    final Long xid;

    public TransactionErrorMonitors(Long xid) {
        super();
        this.xid = xid;
    }

    public Long getXid() {
        return xid;
    }

    public void onNextTransactionError(TransactionError e) {
        transactionErrorMonitor().onNext(e);
    }

    public ReplayProcessor<TransactionError> transactionErrorMonitor() {
        return transactionErrorMonitor;
    }

    @Override
    public void close() {
        transactionErrorMonitor().dispose();
    }
}
