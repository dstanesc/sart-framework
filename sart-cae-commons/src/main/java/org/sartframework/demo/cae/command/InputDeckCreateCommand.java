package org.sartframework.demo.cae.command;

import org.sartframework.aggregate.DomainAggregate;
import org.sartframework.annotation.Evolvable;
import org.sartframework.command.GenericCreateAggregateCommand;
import org.sartframework.demo.cae.aggregate.SimulationAggregate;

@Evolvable(identity="cae.command.InputDeckCreate", version = 1)
public class InputDeckCreateCommand extends GenericCreateAggregateCommand<InputDeckCreateCommand> {

    String inputDeckName;

    String inputDeckFile;

    public InputDeckCreateCommand() {
        super();
    }

    public InputDeckCreateCommand(String inputDeckId, String inputDeckName, String inputDeckFile) {
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
    public String toString() {
        return "InputDeckCreateCommand [inputDeckName=" + inputDeckName + ", inputDeckFile=" + inputDeckFile + ", aggregateKey=" + aggregateKey
            + ", aggregateVersion=" + aggregateVersion + ", xid=" + xid + ", xcs=" + xcs + "]";
    }

    @Override
    public Class<? extends DomainAggregate> getAggregateType() {
        return SimulationAggregate.class;
    }

}
