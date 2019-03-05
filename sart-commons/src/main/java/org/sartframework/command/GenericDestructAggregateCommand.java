package org.sartframework.command;

public class GenericDestructAggregateCommand<T extends GenericDestructAggregateCommand<?>> extends GenericDomainCommand<T> implements DestructAggregateCommand{

    public GenericDestructAggregateCommand() {}

    public GenericDestructAggregateCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
