package org.sartframework.driver;

import org.sartframework.driver.RemoteApi;
import org.sartframework.driver.RequestMapping;
import org.sartframework.driver.RequestMethod;
import org.sartframework.query.ConflictsByAggregateQuery;
import org.sartframework.query.ConflictsByChangeQuery;
import org.sartframework.query.ConflictsByXidQuery;

public class RemoteConflictQueryApi extends RemoteApi {

    public RemoteConflictQueryApi() {
        super();
        setPort(8081);
        registerQuerySupport(ConflictsByAggregateQuery.class, new RequestMapping(RequestMethod.POST, "/query/conflicts/aggregate"));
        registerQuerySupport(ConflictsByChangeQuery.class, new RequestMapping(RequestMethod.POST, "/query/conflicts/change"));
        registerQuerySupport(ConflictsByXidQuery.class, new RequestMapping(RequestMethod.POST, "/query/conflicts/xid"));
    }
}
