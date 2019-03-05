package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public interface AggregateFieldElementAddedEvent<C extends DomainCommand> extends DomainEvent<C> {

}