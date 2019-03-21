package org.sartframework.demo.cae.command;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.GenericTestAggregateCommand;

@Evolvable(identity="cae.command.AggregateDebug", version = 1)
public class AggregateDebugCommand extends GenericTestAggregateCommand<AggregateDebugCommand>{

    public AggregateDebugCommand() {
        super();
    }

    public AggregateDebugCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
