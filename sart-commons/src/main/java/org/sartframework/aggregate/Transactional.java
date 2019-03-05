package org.sartframework.aggregate;

public interface Transactional extends TransactionAware {

    void setXid(long xid);
    
    void setXcs(long xcs);

    long getXcs();
}
