package org.sartframework.command.transaction;

import org.sartframework.session.SystemSnapshot;

public class StartTransactionCommand implements TransactionCommand {

    long xid;
    
    int isolation;
    
    SystemSnapshot systemSnapshot;

    public StartTransactionCommand() {
        super();
    }

    public StartTransactionCommand(long xid, int isolation, SystemSnapshot systemSnapshot) {
        super();
        this.xid = xid;
        this.isolation = isolation;
        this.systemSnapshot = systemSnapshot;
    }

    @Override
    public long getXid() {

        return xid;
    }

    public void setXid(long xid) {
        this.xid = xid;
    }

    
    public int getIsolation() {
        return isolation;
    }

    public void setIsolation(int isolation) {
        this.isolation = isolation;
    }

    public SystemSnapshot getSystemTransactions() {
        return systemSnapshot;
    }
    
    
    public void setSystemTransactions(SystemSnapshot systemSnapshot) {
        this.systemSnapshot = systemSnapshot;
    }

}
