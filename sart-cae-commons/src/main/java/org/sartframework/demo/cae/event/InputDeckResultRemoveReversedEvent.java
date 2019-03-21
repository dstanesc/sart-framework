package org.sartframework.demo.cae.event;

import org.sartframework.annotation.Evolvable;
import org.sartframework.demo.cae.command.InputDeckRemoveResultCommand;
import org.sartframework.event.GenericAggregateFieldElementRemoveReversedEvent;

@Evolvable(identity="cae.event.InputDeckResultRemoveReversed", version = 1)
public class InputDeckResultRemoveReversedEvent
    extends GenericAggregateFieldElementRemoveReversedEvent<InputDeckRemoveResultCommand> {

    public InputDeckResultRemoveReversedEvent() {
    }

    public InputDeckResultRemoveReversedEvent(String inputDeckId, int inputDeckVersion, String resultId) {
        super(inputDeckId, inputDeckVersion, resultId);
    }

    @Override
    public InputDeckRemoveResultCommand undo(long xid, long xcs) {

        // policy: flag only remove. Reverse by resetting the remove flag: xmax

        return new InputDeckRemoveResultCommand(getAggregateKey(), getSourceAggregateVersion(), getRemovedElementKey()).addTransactionHeader(xid, xcs);
    }

    @Override
    public String getChangeKey() {

        return getAggregateKey().toString() + "_results_" + getRemovedElementKey() + "_xmax";
    }

}
