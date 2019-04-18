package org.sartframework.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDetails {

    long xid;

    List<AbstractDetail> details = new ArrayList<>();

    public TransactionDetails() {
        super();
    }

    public TransactionDetails(long xid) {
        super();
        this.xid = xid;
    }

    public TransactionDetails addDetail(AbstractDetail transactionDetail) {
        details.add(transactionDetail);
        return this;
    }

    public TransactionDetails addDetails(TransactionDetails transactionDetails) {
        details.addAll(transactionDetails.getDetails());
        return this;
    }
    
    public long getXid() {
        return xid;
    }

    public List<AbstractDetail> getDetails() {
        return details;
    }
    
    public <T extends AbstractDetail>  Optional<T> getDetail(String detailName) {
        
        Optional<T> out = null;
        
        for (AbstractDetail detail : details) {
            if(detail.getName().equals(detailName)) {
                out = Optional.of((T)detail);
                break;
            }
        }
        
        if(out == null)
            out = Optional.empty();
        
       return out;
    }
}
