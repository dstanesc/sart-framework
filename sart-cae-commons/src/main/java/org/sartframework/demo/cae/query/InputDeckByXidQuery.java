package org.sartframework.demo.cae.query;

import org.sartframework.annotation.Evolvable;
import org.sartframework.query.AbstractQuery;

@Evolvable(identity="cae.query.InputDeckByXid", version = 1)
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
