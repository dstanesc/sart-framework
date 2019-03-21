package org.sartframework.demo.cae.event;

import org.sartframework.annotation.Evolvable;
import org.sartframework.demo.cae.command.InputDeckDeleteCommand;
import org.sartframework.event.GenericAggregateDeleteReversedEvent;

@Evolvable(identity="cae.event.InputDeckDeleteUndone", version = 1)
public class InputDeckDeleteUndoneEvent extends GenericAggregateDeleteReversedEvent <InputDeckDeleteCommand> {

    public InputDeckDeleteUndoneEvent() {}

    public InputDeckDeleteUndoneEvent(String inputDeckId, int inputDeckVersion) {
        super(inputDeckId, inputDeckVersion);
    }

    @Override
    public InputDeckDeleteCommand undo(long xid, long xcs) {
        
        //policy: flag only delete, reverse by setting the delete flag: xmax
       
        return new InputDeckDeleteCommand(getAggregateKey(), getSourceAggregateVersion()).addTransactionHeader(xid, xcs);
    }

    @Override
    public String getChangeKey() {
 
        return getAggregateKey().toString();
    }

    
}
