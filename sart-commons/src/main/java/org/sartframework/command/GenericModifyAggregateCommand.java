package org.sartframework.command;

public class GenericModifyAggregateCommand<T extends GenericModifyAggregateCommand<?>> extends GenericDomainCommand<T> implements ModifyAggregateCommand{

    public GenericModifyAggregateCommand() {}

    public GenericModifyAggregateCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
