package org.sartframework.demo.cae.command;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.GenericTestAggregateCommand;

@Evolvable(identity="cae.command.ForceValidationFailure", version = 1)
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
