package org.sartframework.aggregate;

import org.sartframework.command.DomainCommand;

public interface Reversible<C extends DomainCommand> {

    C undo(long xid, long xcs);
}
