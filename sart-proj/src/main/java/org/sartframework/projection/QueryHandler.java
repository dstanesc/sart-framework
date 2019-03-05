package org.sartframework.projection;

import java.util.List;

import org.sartframework.query.DomainQuery;

public interface QueryHandler {

    <T extends DomainQuery, R> List<R> handleQuery(T domainQuery);
}
