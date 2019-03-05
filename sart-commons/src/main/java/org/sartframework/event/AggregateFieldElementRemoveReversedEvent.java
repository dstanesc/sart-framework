package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public interface AggregateFieldElementRemoveReversedEvent<C extends DomainCommand> extends DomainEvent<C> {

}