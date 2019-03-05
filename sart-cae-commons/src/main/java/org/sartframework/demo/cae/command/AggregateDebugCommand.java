package org.sartframework.demo.cae.command;

import org.sartframework.command.GenericTestAggregateCommand;

public class AggregateDebugCommand extends GenericTestAggregateCommand<AggregateDebugCommand>{

    public AggregateDebugCommand() {
        super();
    }

    public AggregateDebugCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
