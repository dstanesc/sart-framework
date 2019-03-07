package org.sartframework.projection;

import java.util.List;

import org.sartframework.query.DomainQuery;
import org.sartframework.result.QueryResult;

public interface QueryHandler<R extends QueryResult> {

    <T extends DomainQuery> List<R> handleQuery(T domainQuery);
}
