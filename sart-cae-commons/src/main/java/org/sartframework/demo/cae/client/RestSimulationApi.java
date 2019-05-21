package org.sartframework.demo.cae.client;

import org.sartframework.demo.cae.command.BatchInputDeckCreateCommand;
import org.sartframework.demo.cae.command.ForceValidationFailureCommand;
import org.sartframework.demo.cae.command.InputDeckAddResultCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckDeleteCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.driver.RequestMapping;
import org.sartframework.driver.RequestMethod;
import org.sartframework.driver.RestRemoteApi;

public class RestSimulationApi extends RestRemoteApi {

    public RestSimulationApi() {
        super();
        registerCommandSupport(BatchInputDeckCreateCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/batch/create"));
        registerCommandSupport(InputDeckCreateCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/create"));
        registerCommandSupport(InputDeckDeleteCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/delete"));
        registerCommandSupport(InputDeckAddResultCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/addResult"));
        registerCommandSupport(InputDeckUpdateFileCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/updateFile"));
        registerCommandSupport(ForceValidationFailureCommand.class, new RequestMapping(RequestMethod.POST, "/inputDeck/forceFailure"));
    }
}
