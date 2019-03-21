package org.sartframework.demo.cae.command;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.GenericDestructAggregateCommand;

@Evolvable(identity="cae.command.InputDeckDelete", version = 1)
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
