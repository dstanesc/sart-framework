package org.sartframework.command;

public class GenericTestAggregateCommand<T extends GenericTestAggregateCommand<?>> extends GenericDomainCommand<T> implements TestAggregateCommand{
    
    public GenericTestAggregateCommand() {}

    public GenericTestAggregateCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
