package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public interface AggregateFieldUpdatedEvent<C extends DomainCommand> extends DomainEvent<C> {

}