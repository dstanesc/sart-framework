package org.sartframework.demo.cae.command;

import org.sartframework.command.GenericModifyAggregateCommand;

public class InputDeckUndoDeleteCommand extends GenericModifyAggregateCommand <InputDeckUndoDeleteCommand>{


    public InputDeckUndoDeleteCommand() {
        super();

    }

    public InputDeckUndoDeleteCommand(String inputDeckId, long inputDeckVersion) {
        super(inputDeckId, inputDeckVersion);
    }
}
