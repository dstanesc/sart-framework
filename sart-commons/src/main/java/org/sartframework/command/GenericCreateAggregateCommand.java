package org.sartframework.command;

import org.sartframework.aggregate.DomainAggregate;

public abstract class GenericCreateAggregateCommand<C extends GenericCreateAggregateCommand<?>> extends GenericDomainCommand<C>
    implements CreateAggregateCommand {


    public GenericCreateAggregateCommand() {
    }

    public GenericCreateAggregateCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

    @Override
    public DomainAggregate newAggregate() {

        try {
            return getAggregateType().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
