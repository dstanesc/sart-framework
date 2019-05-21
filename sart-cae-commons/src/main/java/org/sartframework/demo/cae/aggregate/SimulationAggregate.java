package org.sartframework.demo.cae.aggregate;

import java.util.LinkedHashMap;
import java.util.Map;

import org.sartframework.aggregate.AnnotatedDomainAggregate;
import org.sartframework.annotation.DomainCommandHandler;
import org.sartframework.annotation.DomainEventHandler;
import org.sartframework.annotation.Evolvable;
import org.sartframework.command.DomainCommand;
import org.sartframework.demo.cae.command.ForceValidationFailureCommand;
import org.sartframework.demo.cae.command.InputDeckAddResultCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckDeleteCommand;
import org.sartframework.demo.cae.command.InputDeckRemoveResultCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.demo.cae.error.InputDeckInvalidFileError;
import org.sartframework.demo.cae.event.InputDeckCreatedEvent;
import org.sartframework.demo.cae.event.InputDeckDeletedEvent;
import org.sartframework.demo.cae.event.InputDeckFileUpdatedEvent;
import org.sartframework.demo.cae.event.InputDeckResultAddedEvent;
import org.sartframework.demo.cae.event.InputDeckResultRemoveReversedEvent;
import org.sartframework.demo.cae.event.InputDeckResultRemovedEvent;
import org.sartframework.error.DomainError;
import org.sartframework.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Evolvable(identity="cae.aggregate.SimulationAggregate", version = 1)
public class SimulationAggregate extends AnnotatedDomainAggregate {

    final static Logger LOGGER = LoggerFactory.getLogger(SimulationAggregate.class);

    Map<String, Result> results = new LinkedHashMap<>();

    String inputDeckName;

    String inputDeckFile;

    public SimulationAggregate() {
    }

    public String getInputDeckName() {
        return inputDeckName;
    }

    public void setInputDeckName(String inputDeckName) {
        this.inputDeckName = inputDeckName;
    }

    public String getInputDeckFile() {
        return inputDeckFile;
    }

    public void setInputDeckFile(String inputDeckFile) {
        this.inputDeckFile = inputDeckFile;
    }

    @DomainCommandHandler
    public void handle(InputDeckCreateCommand c) {

        long xid = c.getXid();

        long xcs = c.getXcs();

        if (validateFile(c)) {

            DomainEvent<? extends DomainCommand> inputDeckCreated = new InputDeckCreatedEvent(c.getAggregateKey(), c.getInputDeckName(),
                c.getInputDeckFile()).addTransactionHeader(xid, xcs);

            dispatch(inputDeckCreated);

        } else {

            DomainError fileError = new InputDeckInvalidFileError(c.getAggregateKey(), 0L, c.getInputDeckFile()).addTransactionHeader(xid, xcs);
            
            dispatch(fileError);
        }
    }

    
    private boolean validateFile(InputDeckCreateCommand c) {

       return isFileValid(c.getInputDeckFile());
    }

    private boolean isFileValid(String inputDeckFile) {
        
        // Dummy check
        return !inputDeckFile.contains("invalid");
    }

    @DomainCommandHandler
    public void handle(InputDeckUpdateFileCommand c) {

        long xid = c.getXid();

        long xcs = c.getXcs();
        

        DomainEvent<? extends DomainCommand> fileUpdated = new InputDeckFileUpdatedEvent(c.getAggregateKey(), c.getAggregateVersion(),
            c.getInputDeckFile(), getInputDeckFile()).addTransactionHeader(xid, xcs);

        dispatch(fileUpdated);
    }


    @DomainCommandHandler
    public void handle(InputDeckAddResultCommand c) {

        long sourceAggregateVersion = c.getAggregateVersion();

        long xid = c.getXid();

        long xcs = c.getXcs();

        DomainEvent<? extends DomainCommand> resultAdded = new InputDeckResultAddedEvent(c.getAggregateKey(), sourceAggregateVersion, c.getResultId(),
            c.getResultName(), c.getResultFile()).addTransactionHeader(xid, xcs);

        dispatch(resultAdded);
    }


    @DomainCommandHandler
    public void handle(InputDeckRemoveResultCommand c) {

        long sourceAggregateVersion = c.getAggregateVersion();

        long xid = c.getXid();

        long xcs = c.getXcs();

        
        DomainEvent<? extends DomainCommand> resultRemoved = new InputDeckResultRemovedEvent(c.getAggregateKey(), sourceAggregateVersion,
            c.getResultId()).addTransactionHeader(xid, xcs);

        dispatch(resultRemoved);
    }

    
    @DomainCommandHandler
    public void handle(InputDeckDeleteCommand c) {
        
        long xid = c.getXid();

        long xcs = c.getXcs();

        DomainEvent<? extends DomainCommand>  inputDeckDeletedEvent = new InputDeckDeletedEvent(c.getAggregateKey(), c.getAggregateVersion()).addTransactionHeader(xid, xcs);
        
        dispatch(inputDeckDeletedEvent);
    }

    @DomainCommandHandler
    public void handle(ForceValidationFailureCommand c) {

        LOGGER.info("Force validation failure command received {} ", c);
        
        abortTransaction(c.getXid());
    }


    @DomainEventHandler
    public Long on(InputDeckCreatedEvent e) {

        return handleEvent(e, () -> {

            setAggregateKey(e.getAggregateKey());

            setAggregateVersion(0);

            setInputDeckName(e.getInputDeckName());

            setInputDeckFile(e.getInputDeckFile());

        });
    }

    @DomainEventHandler
    public Long on(InputDeckDeletedEvent e) {

       return handleEvent(e, () -> {
           
           LOGGER.info("Handling agregate destruction {} ", e);
        });
    }


    @DomainEventHandler
    public Long on(InputDeckFileUpdatedEvent e) {

        return handleEvent(e, () -> setInputDeckFile(e.getInputDeckFile()));
    }


    @DomainEventHandler
    public Long on(InputDeckResultAddedEvent e) {

        return handleEvent(e, () -> {

            addResult(new Result(e.getAddedElementKey(), e.getResultName(), e.getResultFile(), XMAX_NOT_SET));

        });
    }


    @DomainEventHandler
    public Long on(InputDeckResultRemovedEvent e) {

        return handleEvent(e, () -> {

            removeResult(e.getRemovedElementKey(), e.getXid());
        });
    }


    @DomainEventHandler
    public Long on(InputDeckResultRemoveReversedEvent e) {

        return handleEvent(e, () -> {

        });
    }

    private void addResult(Result result) {

        this.results.put(result.getResultId(), result);
    }

    private void removeResult(String resultId, long xid) {

        Result result = this.results.get(resultId);

        result.setXmax(xid);
    }

}
