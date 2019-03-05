package org.sartframework.demo.cae.command;

import org.sartframework.command.GenericModifyAggregateCommand;

public class InputDeckRemoveResultCommand extends GenericModifyAggregateCommand<InputDeckRemoveResultCommand> {

    String resultId;


    public InputDeckRemoveResultCommand() {
        super();
    }

    public InputDeckRemoveResultCommand(String inputDeckId, long inputDeckVersion, String resultId) {
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
        return "InputDeckRemoveResultCommand [resultId=" + resultId + "]";
    }

}
