package org.sartframework.event;

import org.sartframework.aggregate.Reversible;
import org.sartframework.aggregate.Transactional;
import org.sartframework.aggregate.UniqueChange;
import org.sartframework.command.DomainCommand;

public interface DomainEvent<C extends DomainCommand> extends Reversible<C>, UniqueChange, Transactional, Event {
    
    long getTargetAggregateVersion();
    
    void setTargetAggregateVersion(long targetAggregateVersion);

    long getSourceAggregateVersion();
    
    String getAggregateKey();
}
