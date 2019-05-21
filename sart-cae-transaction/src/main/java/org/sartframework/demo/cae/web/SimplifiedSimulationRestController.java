package org.sartframework.demo.cae.web;

import org.sartframework.demo.cae.command.BatchInputDeckCreateCommand;
import org.sartframework.demo.cae.command.ForceValidationFailureCommand;
import org.sartframework.demo.cae.command.InputDeckAddResultCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckDeleteCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.transaction.BusinessTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimplifiedSimulationRestController {
    final static Logger LOGGER = LoggerFactory.getLogger(SimplifiedSimulationRestController.class);

    private final BusinessTransactionManager transactionManager;

    public SimplifiedSimulationRestController(BusinessTransactionManager transactionManager) {
        super();
        this.transactionManager = transactionManager;
    }

    @PostMapping("/inputDeck/batch/create")
    @ResponseStatus(value = HttpStatus.OK)
    public void createInputDeck(@RequestBody BatchInputDeckCreateCommand batchCreateInputDeckCommand) {
        LOGGER.info("REST call batch input deck create");
        for (InputDeckCreateCommand inputDeckCreateCommand : batchCreateInputDeckCommand) {
            LOGGER.info(" -> create individual inputDeckId = {}, xid={}, xcs={}", inputDeckCreateCommand.getAggregateKey(),
                inputDeckCreateCommand.getXid(), inputDeckCreateCommand.getXcs());
            transactionManager.publish(inputDeckCreateCommand);
        }
    }

    @PostMapping("/inputDeck/create")
    @ResponseStatus(value = HttpStatus.OK)
    public void createInputDeck(@RequestBody InputDeckCreateCommand inputDeckCreateCommand) {

        LOGGER.info("REST call create inputDeckId = {}, xid={}, xcs={}", inputDeckCreateCommand.getAggregateKey(), inputDeckCreateCommand.getXid(),
            inputDeckCreateCommand.getXcs());

        transactionManager.publish(inputDeckCreateCommand);
    }
    
    @PostMapping("/inputDeck/delete")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteInputDeck(@RequestBody InputDeckDeleteCommand inputDeckDeleteCommand) {

        LOGGER.info("REST call delete inputDeckId = {}, xid={}, xcs={}", inputDeckDeleteCommand.getAggregateKey(), inputDeckDeleteCommand.getXid(),
            inputDeckDeleteCommand.getXcs());

        transactionManager.publish(inputDeckDeleteCommand);
    }

    @PostMapping("/inputDeck/addResult")
    @ResponseStatus(value = HttpStatus.OK)
    public void addResult(@RequestBody InputDeckAddResultCommand addResultCommand) {

        LOGGER.info("REST call addResult inputDeckId = {}, resultId= {}, resultFile = {}", addResultCommand.getAggregateKey(),
            addResultCommand.getResultId(), addResultCommand.getResultFile());

        transactionManager.publish(addResultCommand);
    }

    @PostMapping("/inputDeck/updateFile")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateFile(@RequestBody InputDeckUpdateFileCommand inputDeckUpdateFileCommand) {

        LOGGER.info("REST call updateFile inputDeckId = {}, inputDeckVersion= {}, inputDeckFile = {}", inputDeckUpdateFileCommand.getAggregateKey(),
            inputDeckUpdateFileCommand.getAggregateVersion(), inputDeckUpdateFileCommand.getInputDeckFile());

        transactionManager.publish(inputDeckUpdateFileCommand);
    }

    @PostMapping("/inputDeck/forceFailure")
    @ResponseStatus(value = HttpStatus.OK)
    public void forceValidationFailure(@RequestBody ForceValidationFailureCommand forceValidationFailureCommand) {

        LOGGER.info("REST call forceFailure xid {}, xcs{}, inputDeckId = {}, inputDeckVersion = {}", forceValidationFailureCommand.getXid(),
            forceValidationFailureCommand.getXcs(), forceValidationFailureCommand.getAggregateKey(),
            forceValidationFailureCommand.getAggregateVersion());

        transactionManager.publish(forceValidationFailureCommand);
    }
}
