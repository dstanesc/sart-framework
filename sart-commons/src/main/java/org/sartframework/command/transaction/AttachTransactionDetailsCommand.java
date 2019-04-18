package org.sartframework.command.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.transaction.TransactionDetails;

@Evolvable(version = 1)
public class AttachTransactionDetailsCommand implements TransactionCommand {
    
    TransactionDetails details;
    
    public AttachTransactionDetailsCommand() {
        super();
    }

    public AttachTransactionDetailsCommand(TransactionDetails transactionDetails) {
        super();
        this.details = transactionDetails;
    }

    @Override
    public long getXid() {
        return details.getXid();
    }

    public TransactionDetails getDetails() {
        return details;
    }

}
