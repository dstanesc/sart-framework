package org.sartframework.demo.cae.command;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.GenericModifyAggregateCommand;

@Evolvable(identity="cae.command.InputDeckUndoDelete", version = 1)
public class InputDeckUndoDeleteCommand extends GenericModifyAggregateCommand <InputDeckUndoDeleteCommand>{


    public InputDeckUndoDeleteCommand() {
        super();

    }

    public InputDeckUndoDeleteCommand(String inputDeckId, long inputDeckVersion) {
        super(inputDeckId, inputDeckVersion);
    }
}
