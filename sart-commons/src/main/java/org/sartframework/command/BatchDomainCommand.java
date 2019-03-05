package org.sartframework.command;


public interface BatchDomainCommand<C extends DomainCommand> extends DomainCommand, Iterable<C> {

    void add(C element);
}
