package org.sartframework.aggregate;

import org.sartframework.command.transaction.TransactionCommand;

public interface TransactionAggregate extends AsynchHandler<TransactionCommand> {

}
