package org.sartframework.demo.cae.command;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.GenericModifyAggregateCommand;

@Evolvable(identity="cae.command.InputDeckAddResult", version = 1)
public class InputDeckAddResultCommand extends GenericModifyAggregateCommand<InputDeckAddResultCommand> {

    String resultId;

    String resultName;

    String resultFile;

    public InputDeckAddResultCommand() {
        super();
    }

    public InputDeckAddResultCommand(String inputDeckId, long inputDeckVersion, String resultId, String resultName, String resultFile) {
        super(inputDeckId, inputDeckVersion);
        this.resultId = resultId;
        this.resultName = resultName;
        this.resultFile = resultFile;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
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
    public String toString() {
        return "InputDeckAddResultCommand [resultId=" + resultId + ", resultName=" + resultName + ", resultFile=" + resultFile + "]";
    }

}
