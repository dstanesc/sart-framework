package org.sartframework.demo.cae.event;

import org.sartframework.demo.cae.command.InputDeckUndoDeleteCommand;
import org.sartframework.event.GenericAggregateDeletedEvent;

public class InputDeckDeletedEvent extends GenericAggregateDeletedEvent<InputDeckUndoDeleteCommand> {

    public InputDeckDeletedEvent() {}

    public InputDeckDeletedEvent(String inputDeckId, long inputDeckVersion) {
        super(inputDeckId, inputDeckVersion);
    }

    @Override
    public InputDeckUndoDeleteCommand undo(long xid, long xcs) {
        
        //policy: flag only delete, reverse by resetting the delete flag: xmax
       
        return new InputDeckUndoDeleteCommand(getAggregateKey(), getSourceAggregateVersion()).addTransactionHeader(xid, xcs);
    }

    @Override
    public String getChangeKey() {

        return getAggregateKey().toString();
    }

}
