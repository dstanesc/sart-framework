package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

@Evolvable(version = 1)
public class ConflictResolvedEvent extends GenericEvent implements TransactionEvent {

    ConflictResolvedData data;

    public ConflictResolvedEvent() {
        super();
    }

    public ConflictResolvedEvent(ConflictResolvedData data) {
        super(data.getXid());
        this.data = data;
    }

    public ConflictResolvedData getData() {
        return data;
    }

    public void setData(ConflictResolvedData data) {
        this.data = data;
    }
}
