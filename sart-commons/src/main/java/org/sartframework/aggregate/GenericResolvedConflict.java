package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;

public class GenericResolvedConflict implements ResolvedConflict {

    Conflict conflict;
    
    boolean currentChangeApplied;
    
    public GenericResolvedConflict(Conflict conflict, boolean currentChangeApplied) {
        super();
        this.conflict = conflict;
        this.currentChangeApplied = currentChangeApplied;
    }

    @Override
    public boolean isCurrentChangeApplied() {

        return currentChangeApplied;
    }

    @Override
    public DomainEvent<? extends DomainCommand> getCurrentChange() {

        return conflict.getCurrentChange();
    }

    @Override
    public DomainEvent<? extends DomainCommand> getOtherChange() {

        return conflict.getOtherChange();
    }

}
