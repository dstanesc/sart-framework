package org.sartframework.error;

import org.sartframework.aggregate.Transactional;

public interface DomainError extends Transactional {

    String getAggregateKey();
    
    long getAggregateVersion();
}
