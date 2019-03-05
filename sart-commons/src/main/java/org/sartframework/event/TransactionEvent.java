package org.sartframework.event;

import org.sartframework.aggregate.TransactionAware;

public interface TransactionEvent extends Event, TransactionAware {

}
