package org.sartframework.aggregate;

public interface ConflictResolver {

    boolean resolve(Conflict conflict, DomainAggregate aggregate);
}
