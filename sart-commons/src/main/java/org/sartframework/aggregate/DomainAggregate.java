package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.AggregateCreatedEvent;
import org.sartframework.event.AggregateDestructedEvent;
import org.sartframework.event.AggregateDestructionReversedEvent;
import org.sartframework.event.AggregateFieldDecrementedEvent;
import org.sartframework.event.AggregateFieldElementAddedEvent;
import org.sartframework.event.AggregateFieldElementRemoveReversedEvent;
import org.sartframework.event.AggregateFieldElementRemovedEvent;
import org.sartframework.event.AggregateFieldIncrementedEvent;
import org.sartframework.event.AggregateFieldUpdatedEvent;

public interface DomainAggregate extends VersionedAggregate, UniqueAggregate, CommandChannel, EventChannel, ErrorChannel {
    
    public final static long XMAX_NOT_SET = -1;

    ChangeSet getChanges(long fromVersion, long toVersion);

    /*
     * Handling of atomic domain events
     */

    <C extends DomainCommand> Long handleEvent(AggregateCreatedEvent<C> aggregateCreatedEvent, Deferrable deferrable);

    <C extends DomainCommand> Long handleEvent(AggregateDestructedEvent<C> aggregateDeletedEvent, Deferrable deferrable);

    <C extends DomainCommand> Long handleEvent(AggregateDestructionReversedEvent<C> aggregateDeleteReversedEvent, Deferrable deferrable);

    <C extends DomainCommand> Long handleEvent(AggregateFieldElementAddedEvent<C> aggregateFieldAppendedEvent, Deferrable deferrable);

    <C extends DomainCommand> Long handleEvent(AggregateFieldElementRemovedEvent<C> aggregateFieldElementRemovedEvent, Deferrable deferrable);

    <C extends DomainCommand> Long handleEvent(AggregateFieldElementRemoveReversedEvent<C> aggregateFieldElementRemoveReversedEvent,
                                               Deferrable deferrable);

    <C extends DomainCommand> Long handleEvent(AggregateFieldIncrementedEvent<C> aggregateFieldIncrementedEvent, Deferrable deferrable);

    <C extends DomainCommand> Long handleEvent(AggregateFieldDecrementedEvent<C> aggregateFieldDecrementedEvent, Deferrable deferrable);

    <C extends DomainCommand> Long handleEvent(AggregateFieldUpdatedEvent<C> aggregateFieldUpdatedEvent, Deferrable deferrable);
    
    


}
        