package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;

public interface Conflict {

    DomainEvent<? extends DomainCommand> getCurrentChange();
    
    DomainEvent<? extends DomainCommand> getOtherChange();
}
