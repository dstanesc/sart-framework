package org.sartframework.demo.cae.event;

import org.sartframework.annotation.Evolvable;
import org.sartframework.demo.cae.command.InputDeckUndoRemoveResultCommand;
import org.sartframework.event.GenericAggregateFieldElementRemovedEvent;

@Evolvable(identity="cae.event.InputDeckResultRemoved", version = 1)
public class InputDeckResultRemovedEvent
    extends GenericAggregateFieldElementRemovedEvent<InputDeckUndoRemoveResultCommand> {

    public InputDeckResultRemovedEvent() {
    }

    public InputDeckResultRemovedEvent(String inputDeckId, long inputDeckVersion, String resultId) {
        super(inputDeckId, inputDeckVersion, resultId);
    }

    @Override
    public InputDeckUndoRemoveResultCommand undo(long xid, long xcs) {

        // policy: flag only remove. Reverse by resetting the remove flag: xmax

        return new InputDeckUndoRemoveResultCommand(getAggregateKey(), getSourceAggregateVersion(), getRemovedElementKey()).addTransactionHeader(xid, xcs);
    }

    @Override
    public String getChangeKey() {
        
        return  getAggregateKey().toString() + "_results_" + getRemovedElementKey() + "_xmax";
    }
    
    

}
