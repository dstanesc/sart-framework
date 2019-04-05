package org.sartframework.driver;

import org.sartframework.command.transaction.TransactionStatus.Isolation;

public interface TransactionDriver {

    TransactionDriver init();
    
    DomainTransaction createDomainTransaction();
    
    DomainTransaction createDomainTransaction(Isolation isolation);

}
