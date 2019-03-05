package org.sartframework.query;

import static org.sartframework.query.QueryVariables.of;
import static org.sartframework.query.QueryVariables.variable;

public class ConflictsByXidQuery extends AbstractQuery {

    final long xid;

    public ConflictsByXidQuery(long xid) {
        super();
        this.xid = xid;
    }

    public boolean matches(long winnerXid, long otherXid) {

        return this.xid == winnerXid || this.xid == otherXid;
    }

    public long getXid() {
        return xid;
    }

}
