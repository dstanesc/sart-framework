package org.sartframework.aggregate;

import org.sartframework.error.DomainError;
import org.sartframework.error.transaction.TransactionError;

public interface ErrorChannel {

    void publish(TransactionError transactionError);

    void publish(DomainError domainError);
}
