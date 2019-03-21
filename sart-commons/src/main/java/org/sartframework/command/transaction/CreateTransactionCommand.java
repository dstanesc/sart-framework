package org.sartframework.command.transaction;

import org.sartframework.annotation.Evolvable;

@Evolvable(version = 1)
public class CreateTransactionCommand implements TransactionCommand {

    long xid;

    public CreateTransactionCommand() {
        super();
    }

    public CreateTransactionCommand(long xid) {
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
