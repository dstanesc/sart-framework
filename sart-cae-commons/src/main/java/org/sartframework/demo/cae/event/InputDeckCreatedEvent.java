package org.sartframework.demo.cae.event;

import org.sartframework.annotation.Evolvable;
import org.sartframework.demo.cae.command.InputDeckDeleteCommand;
import org.sartframework.event.GenericAggregateCreatedEvent;

@Evolvable(identity="cae.event.InputDeckCreated", version = 1)
public class InputDeckCreatedEvent extends GenericAggregateCreatedEvent<InputDeckDeleteCommand> {

    String inputDeckName;

    String inputDeckFile;

    public InputDeckCreatedEvent() {
        super();
    }

    public InputDeckCreatedEvent(String inputDeckId, String inputDeckName, String inputDeckFile) {
        super(inputDeckId, 0);

        this.inputDeckName = inputDeckName;
        this.inputDeckFile = inputDeckFile;
    }

    public String getInputDeckName() {
        return inputDeckName;
    }

    public void setInputDeckName(String inputDeckName) {
        this.inputDeckName = inputDeckName;
    }

    public String getInputDeckFile() {
        return inputDeckFile;
    }

    public void setInputDeckFile(String inputDeckFile) {
        this.inputDeckFile = inputDeckFile;
    }

    @Override
    public InputDeckDeleteCommand undo(long xid, long xcs) {

        // inverse of create is delete
        
        return new InputDeckDeleteCommand(getAggregateKey(), getSourceAggregateVersion()).addTransactionHeader(xid, xcs);
    }

    @Override
    public String getChangeKey() {

        return getAggregateKey().toString();
    }
    
}
