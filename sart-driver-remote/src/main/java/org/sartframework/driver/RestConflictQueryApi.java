package org.sartframework.driver;

import org.sartframework.driver.RestRemoteApi;
import org.sartframework.driver.RequestMapping;
import org.sartframework.driver.RequestMethod;
import org.sartframework.query.ConflictsByAggregateQuery;
import org.sartframework.query.ConflictsByChangeQuery;
import org.sartframework.query.ConflictsByXidQuery;

public class RestConflictQueryApi extends RestRemoteApi {

    public RestConflictQueryApi() {
        super();
        setServerPort(8081);
        registerQuerySupport(ConflictsByAggregateQuery.class, new RequestMapping(RequestMethod.POST, "/query/conflicts/aggregate"));
        registerQuerySupport(ConflictsByChangeQuery.class, new RequestMapping(RequestMethod.POST, "/query/conflicts/change"));
        registerQuerySupport(ConflictsByXidQuery.class, new RequestMapping(RequestMethod.POST, "/query/conflicts/xid"));
    }
}
