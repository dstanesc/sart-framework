package org.sartframework.demo.cae.client;

import org.sartframework.demo.cae.command.BatchInputDeckCreateCommand;
import org.sartframework.demo.cae.command.ForceValidationFailureCommand;
import org.sartframework.demo.cae.command.InputDeckAddResultCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.driver.LocalTopicCommandApi;

public class LocalTopicSimulationApi extends LocalTopicCommandApi {

    public LocalTopicSimulationApi() {
        super();
        registerCommandSupport(BatchInputDeckCreateCommand.class);
        registerCommandSupport(InputDeckCreateCommand.class);
        registerCommandSupport(InputDeckAddResultCommand.class);
        registerCommandSupport(InputDeckUpdateFileCommand.class);
        registerCommandSupport(ForceValidationFailureCommand.class);
    }

}
