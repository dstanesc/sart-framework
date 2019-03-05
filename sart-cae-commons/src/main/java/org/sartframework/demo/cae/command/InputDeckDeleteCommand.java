package org.sartframework.demo.cae.command;

import org.sartframework.command.GenericDestructAggregateCommand;

public class InputDeckDeleteCommand extends GenericDestructAggregateCommand <InputDeckDeleteCommand>{


    public InputDeckDeleteCommand() {
        super();

    }

    public InputDeckDeleteCommand(String inputDeckId, long inputDeckVersion) {
        super(inputDeckId, inputDeckVersion);
    }

    @Override
    public String toString() {
        return "InputDeckDeleteCommand [aggregateKey=" + getAggregateKey() + "]";
    }

}
