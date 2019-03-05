package org.sartframework.aggregate;

import java.util.List;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;

public interface ChangeSet {

    boolean containsChanges(Object aspect);

    List<DomainEvent<? extends DomainCommand>> getChanges(Object aspect);
}
