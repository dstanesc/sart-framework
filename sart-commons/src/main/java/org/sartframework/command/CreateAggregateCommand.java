package org.sartframework.command;

import org.sartframework.aggregate.DomainAggregate;

public interface CreateAggregateCommand extends DomainCommand {

    DomainAggregate newAggregate();
    
    Class<? extends DomainAggregate> getAggregateType();
}
