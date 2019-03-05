package org.sartframework.demo.cae.command;

import org.sartframework.command.GenericModifyAggregateCommand;

public class InputDeckUndoRemoveResultCommand extends GenericModifyAggregateCommand<InputDeckUndoRemoveResultCommand> {

    String resultId;


    public InputDeckUndoRemoveResultCommand() {
        super();
    }

    public InputDeckUndoRemoveResultCommand(String inputDeckId, long inputDeckVersion, String resultId) {
        super(inputDeckId, inputDeckVersion);
        this.resultId = resultId;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    @Override
    public String toString() {
        return "InputDeckUndoRemoveResultCommand [resultId=" + resultId + "]";
    }

}
