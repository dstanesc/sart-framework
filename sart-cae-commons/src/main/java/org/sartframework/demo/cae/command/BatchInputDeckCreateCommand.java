package org.sartframework.demo.cae.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sartframework.command.BatchDomainCommand;
import org.sartframework.command.GenericDomainCommand;

public class BatchInputDeckCreateCommand extends GenericDomainCommand<BatchInputDeckCreateCommand> implements BatchDomainCommand<InputDeckCreateCommand> {

    List<InputDeckCreateCommand> content = new ArrayList<>();

    public BatchInputDeckCreateCommand() {
        super();
        setAggregateKey("dummy");
        setAggregateVersion(-1);
        setXcs(-1);
    }
    
    @Override
    public void add(InputDeckCreateCommand element) {
        content.add(element);
    }

    @Override
    public Iterator<InputDeckCreateCommand> iterator() {

        return content.iterator();
    }

    public List<InputDeckCreateCommand> getContent() {
        return content;
    }

    public void setContent(List<InputDeckCreateCommand> content) {
        this.content = content;
    }

}
