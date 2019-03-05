package org.sartframework.demo.cae.query;

import static org.sartframework.query.QueryVariables.of;
import static org.sartframework.query.QueryVariables.variable;

import org.sartframework.query.AbstractQuery;
import org.sartframework.query.DomainQuery;
import org.sartframework.query.QueryVariables;

public class InputDeckByXidQuery extends AbstractQuery {

    final long xid;

    public InputDeckByXidQuery(long xid) {
        super();
        this.xid = xid;
    }

    public boolean matches(long xid) {
        
        return this.xid == xid;
    }

    public long getXid() {
        return xid;
    }

}
