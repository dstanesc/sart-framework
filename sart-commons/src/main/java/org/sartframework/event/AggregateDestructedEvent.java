package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public interface AggregateDestructedEvent<C extends DomainCommand> extends DomainEvent<C> {

}
