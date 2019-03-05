package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;

public class GenericConflict implements Conflict {

    final DomainEvent<? extends DomainCommand> currentChange;
    
    final DomainEvent<? extends DomainCommand> otherChange;
    

    public GenericConflict(DomainEvent<?> currentChange, DomainEvent<?> otherChange) {
        super();
        this.currentChange = currentChange;
        this.otherChange = otherChange;
    }

    @Override
    public DomainEvent<? extends DomainCommand> getCurrentChange() {

        return currentChange;
    }

    @Override
    public DomainEvent<? extends DomainCommand> getOtherChange() {

        return otherChange;
    }

}
