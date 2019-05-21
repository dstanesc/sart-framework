package org.sartframework.demo.cae.error;

import org.sartframework.annotation.Evolvable;
import org.sartframework.error.AbstractDomainError;

@Evolvable(identity = "cae.error.InputDeckCreated", version = 1)
public class InputDeckInvalidFileError extends AbstractDomainError {

    String inputDeckFile;

    public InputDeckInvalidFileError() {
        super();
    }

    public InputDeckInvalidFileError(String aggregateKey, long aggregateVersion, String inputDeckFile) {
        super(aggregateKey, aggregateVersion);
        this.inputDeckFile = inputDeckFile;
    }

    public String getInputDeckFile() {
        return inputDeckFile;
    }

    public void setInputDeckFile(String inputDeckFile) {
        this.inputDeckFile = inputDeckFile;
    }
}
