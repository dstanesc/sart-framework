package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.TransactionEvent;

public interface Publisher {

    void publish(DomainCommand domainCommand);
    
    void publish(DomainEvent<? extends DomainCommand> domainEvent);
    
    void publish(TransactionCommand transactionCommand);
    
    void publish(TransactionEvent transactionEvent);
}
