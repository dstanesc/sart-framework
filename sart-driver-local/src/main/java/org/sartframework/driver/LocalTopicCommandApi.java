package org.sartframework.driver;

import java.util.HashSet;
import java.util.Set;

import org.sartframework.command.DomainCommand;

public class LocalTopicCommandApi {

    Set<Class<? extends DomainCommand>> supportedCommands = new HashSet<>();

    public void registerCommandSupport(Class<? extends DomainCommand> commandType) {
        supportedCommands.add(commandType);
    }
    
    public boolean hasCommandSupport(Class<? extends DomainCommand> commandType) {
        return supportedCommands.contains(commandType);
    }
}
