package org.sartframework.command;

import org.sartframework.annotation.Evolvable;

@Evolvable(version = 1)
public class DefaultVoidDomainCommand extends GenericDomainCommand<DefaultVoidDomainCommand> implements VoidDomainCommand {

    public DefaultVoidDomainCommand() {

    }

    public DefaultVoidDomainCommand(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
