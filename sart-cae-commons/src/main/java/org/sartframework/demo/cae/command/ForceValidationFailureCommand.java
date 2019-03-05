package org.sartframework.demo.cae.command;

import org.sartframework.command.GenericTestAggregateCommand;

public class ForceValidationFailureCommand extends GenericTestAggregateCommand<ForceValidationFailureCommand>{

    public ForceValidationFailureCommand() {
        super();
    }

    public ForceValidationFailureCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

    @Override
    public String toString() {
        return "ForceValidationFailureCommand [xid=" + xid + ", xcs=" + xcs + ", aggregateKey=" + aggregateKey + ", aggregateVersion="
            + aggregateVersion + "]";
    }

   
}
