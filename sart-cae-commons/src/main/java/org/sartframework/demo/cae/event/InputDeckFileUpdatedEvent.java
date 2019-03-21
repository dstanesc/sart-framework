package org.sartframework.demo.cae.event;

import org.sartframework.annotation.Evolvable;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.event.GenericAggregateFieldUpdatedEvent;

@Evolvable(identity="cae.event.InputDeckFileUpdated", version = 1)
public class InputDeckFileUpdatedEvent extends GenericAggregateFieldUpdatedEvent<InputDeckUpdateFileCommand> {

    String inputDeckFile;

    String originalInputDeckFile;

    public InputDeckFileUpdatedEvent() {
        super();
    }

    public InputDeckFileUpdatedEvent(String inputDeckId, long inputDeckVersion, String inputDeckFile, String originalInputDeckFile) {
        super(inputDeckId, inputDeckVersion);
        this.inputDeckFile = inputDeckFile;
        this.originalInputDeckFile = originalInputDeckFile;
    }

    public String getInputDeckFile() {
        return inputDeckFile;
    }

    public void setInputDeckFile(String inputDeckFile) {
        this.inputDeckFile = inputDeckFile;
    }

    public String getOriginalInputDeckFile() {
        return originalInputDeckFile;
    }

    public void setOriginalInputDeckFile(String originalInputDeckFile) {
        this.originalInputDeckFile = originalInputDeckFile;
    }

    @Override
    public InputDeckUpdateFileCommand undo(long xid, long xcs) {

        // preserve original values in event and apply to reverse change

        return new InputDeckUpdateFileCommand(getAggregateKey(), getSourceAggregateVersion(), originalInputDeckFile).addTransactionHeader(xid, xcs);
    }

    @Override
    public String getChangeKey() {

        return  getAggregateKey().toString() + "_inputDeckFile";
    }

    
}
