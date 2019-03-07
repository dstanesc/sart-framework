package org.sartframework.command;

public class GenericVoidDomainCommand extends GenericDomainCommand<GenericVoidDomainCommand> implements VoidDomainCommand {

    public GenericVoidDomainCommand() {
       
    }

    public GenericVoidDomainCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
