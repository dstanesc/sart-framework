package org.sartframework.transaction;

import java.io.Closeable;

import org.sartframework.error.DomainError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.ReplayProcessor;

public class DomainErrorMonitors implements Closeable {

    final static Logger LOGGER = LoggerFactory.getLogger(DomainErrorMonitors.class);

    ReplayProcessor<DomainError> domainErrorMonitor = ReplayProcessor.<DomainError> create();
    
    final Long xid;

    public DomainErrorMonitors(Long xid) {
        super();
        this.xid = xid;
    }

    public Long getXid() {
        return xid;
    }

    public void onNextDomainError(DomainError e) {
        domainErrorMonitor().onNext(e);
    }
    
    public ReplayProcessor<DomainError> domainErrorMonitor() {
        return domainErrorMonitor;
    }

    @Override
    public void close() {
        domainErrorMonitor().dispose();
    }
}
