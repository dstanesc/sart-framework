package org.sartframework.demo.cae.client;

import org.sartframework.demo.cae.command.BatchInputDeckCreateCommand;
import org.sartframework.demo.cae.command.ForceValidationFailureCommand;
import org.sartframework.demo.cae.command.InputDeckAddResultCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.driver.RemoteApi;
import org.sartframework.driver.RequestMapping;
import org.sartframework.driver.RequestMethod;

public class RemoteSimulationApi extends RemoteApi {

    public RemoteSimulationApi() {
        super();
        registerCommandSupport(BatchInputDeckCreateCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/batch/create"));
        registerCommandSupport(InputDeckCreateCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/create"));
        registerCommandSupport(InputDeckAddResultCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/addResult"));
        registerCommandSupport(InputDeckUpdateFileCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/updateFile"));
        registerCommandSupport(ForceValidationFailureCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/forceFailure"));
    }
}
