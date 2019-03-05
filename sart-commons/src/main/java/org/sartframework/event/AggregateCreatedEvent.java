package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public interface AggregateCreatedEvent<C extends DomainCommand> extends DomainEvent<C> {

}