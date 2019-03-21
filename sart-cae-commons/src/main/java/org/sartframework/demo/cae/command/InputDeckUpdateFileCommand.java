package org.sartframework.demo.cae.command;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.GenericModifyAggregateCommand;

@Evolvable(identity="cae.command.InputDeckUpdateFile", version = 1)
public class InputDeckUpdateFileCommand extends GenericModifyAggregateCommand<InputDeckUpdateFileCommand> {

    String inputDeckFile;

    public InputDeckUpdateFileCommand() {
        super();

    }

    public InputDeckUpdateFileCommand(String inputDeckId, long inputDeckVersion, String inputDeckFile) {
        super(inputDeckId, inputDeckVersion);
        this.inputDeckFile = inputDeckFile;
    }

    public String getInputDeckFile() {
        return inputDeckFile;
    }

    public void setInputDeckFile(String inputDeckFile) {
        this.inputDeckFile = inputDeckFile;
    }

    @Override
    public String toString() {
        return "InputDeckUpdateFileCommand [inputDeckFile=" + inputDeckFile + "]";
    }

}
