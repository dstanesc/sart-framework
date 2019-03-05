package org.sartframework.demo.cae.event;

import org.sartframework.demo.cae.command.InputDeckRemoveResultCommand;
import org.sartframework.event.GenericAggregateFieldElementAddedEvent;

public class InputDeckResultAddedEvent extends GenericAggregateFieldElementAddedEvent <InputDeckRemoveResultCommand>{

    String resultName;

    String resultFile;

    public InputDeckResultAddedEvent() {
        super();
    }

    public InputDeckResultAddedEvent(String inputDeckId, long inputDeckVersion, String resultId, String resultName, String resultFile) {
        super(inputDeckId, inputDeckVersion, resultId);
        this.resultName = resultName;
        this.resultFile = resultFile;
    }


    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    @Override
    public InputDeckRemoveResultCommand undo(long xid, long xcs) {
        
        // compensate by removing result
        
        return new InputDeckRemoveResultCommand(getAggregateKey(), getSourceAggregateVersion(), getAddedElementKey()).addTransactionHeader(xid, xcs);
    }

    @Override
    public String getChangeKey() {
    
        return  getAggregateKey().toString() + "_results_" +getAddedElementKey();
    }

    
    
}
