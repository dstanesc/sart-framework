package org.sartframework.aggregate;

import java.util.ConcurrentModificationException;

public class ConflictResolver_Exception implements ConflictResolver {

    @Override
    public boolean resolve(Conflict conflict, DomainAggregate aggregate) {
        
        throw new ConcurrentModificationException();
    }
}
