package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;
import org.sartframework.command.VoidDomainCommand;

public interface CommandHandlingAggregate extends DomainAggregate, AsynchHandler<DomainCommand> {
    
    void handleVoidCommand(VoidDomainCommand voidDomainCommand);
}
