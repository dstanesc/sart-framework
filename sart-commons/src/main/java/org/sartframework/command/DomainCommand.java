package org.sartframework.command;

import org.sartframework.aggregate.AggregateReference;
import org.sartframework.aggregate.Transactional;

public interface DomainCommand extends Command, Transactional, AggregateReference {

 
}
