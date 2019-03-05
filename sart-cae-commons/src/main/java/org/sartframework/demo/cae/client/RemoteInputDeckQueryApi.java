package org.sartframework.demo.cae.client;

import org.sartframework.demo.cae.query.InputDeckByIdQuery;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.query.InputDeckByXidQuery;
import org.sartframework.driver.RemoteApi;
import org.sartframework.driver.RequestMapping;
import org.sartframework.driver.RequestMethod;

public class RemoteInputDeckQueryApi extends RemoteApi {

    public RemoteInputDeckQueryApi() {
        super();
        setPort(8082);
        registerQuerySupport(InputDeckByXidQuery.class, new RequestMapping(RequestMethod.POST, "/query/inputDeck/xid"));
        registerQuerySupport(InputDeckByIdQuery.class, new RequestMapping(RequestMethod.POST, "/query/inputDeck/id"));
        registerQuerySupport(InputDeckByNameQuery.class, new RequestMapping(RequestMethod.POST, "/query/inputDeck/name"));
    }
}
