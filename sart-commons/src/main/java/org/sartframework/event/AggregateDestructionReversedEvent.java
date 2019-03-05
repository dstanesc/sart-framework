package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public interface AggregateDestructionReversedEvent<C extends DomainCommand> extends DomainEvent<C> {

}
