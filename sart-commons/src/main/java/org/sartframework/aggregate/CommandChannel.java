package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionCommand;

public interface CommandChannel {

    void publish(TransactionCommand transactionCommand);
    
    void publish(DomainCommand domainCommand);
}
