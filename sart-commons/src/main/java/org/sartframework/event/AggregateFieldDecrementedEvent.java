package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public interface AggregateFieldDecrementedEvent<C extends DomainCommand> extends DomainEvent<C> {

}