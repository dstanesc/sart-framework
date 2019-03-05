package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public interface AggregateFieldIncrementedEvent<C extends DomainCommand> extends DomainEvent<C> {

}