package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.TransactionEvent;

public interface EventChannel {

    void publish(TransactionEvent transactionEvent);

    void publish(DomainEvent<? extends DomainCommand> atomicEvent);
}
