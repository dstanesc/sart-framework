package org.sartframework.command.transaction;

public class CommitTransactionCommand implements TransactionCommand {

    long xid;
    
    long xct;

    
    public CommitTransactionCommand() {
        super();
    }

    public CommitTransactionCommand(long xid, long xct) {
        super();
        this.xid = xid;
        this.xct = xct;
    }

    @Override
    public long getXid() {

        return xid;
    }


    public long getXct() {
        return xct;
    }

    public void setXid(long xid) {
        this.xid = xid;
    }

    public void setXct(long xct) {
        this.xct = xct;
    }
    
    
}
