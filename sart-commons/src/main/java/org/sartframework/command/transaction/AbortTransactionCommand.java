package org.sartframework.command.transaction;

public class AbortTransactionCommand implements TransactionCommand {

    long xid;

    public AbortTransactionCommand() {
        super();
    }

    public AbortTransactionCommand(long xid) {
        super();
        this.xid = xid;
    }

    @Override
    public long getXid() {

        return xid;
    }

    public void setXid(long xid) {
        this.xid = xid;
    }

}
