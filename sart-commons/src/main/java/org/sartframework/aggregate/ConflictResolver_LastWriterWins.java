package org.sartframework.aggregate;

public class ConflictResolver_LastWriterWins implements ConflictResolver {

    @Override
    public boolean resolve(Conflict conflict, DomainAggregate aggregate) {
        
        return conflict.getCurrentChange().getXid() > conflict.getOtherChange().getXid();
    }
}
