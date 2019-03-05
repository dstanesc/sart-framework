package org.sartframework.command.transaction;

import org.sartframework.aggregate.TransactionAware;
import org.sartframework.command.Command;

public interface TransactionCommand extends Command, TransactionAware {

}
