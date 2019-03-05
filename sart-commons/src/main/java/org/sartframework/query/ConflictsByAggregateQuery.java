package org.sartframework.query;

import static org.sartframework.query.QueryVariables.of;
import static org.sartframework.query.QueryVariables.variable;

public class ConflictsByAggregateQuery extends AbstractQuery {

    String aggregateKey;

    public ConflictsByAggregateQuery() {
        super();
    }

    public ConflictsByAggregateQuery(String aggregateKey) {
        super();
        this.aggregateKey = aggregateKey;
    }

    public String getAggregateKey() {
        return aggregateKey;
    }

    public void setAggregateKey(String aggregateKey) {
        this.aggregateKey = aggregateKey;
    }

    public boolean matches(String aggregateKey) {
        return this.aggregateKey.equals(aggregateKey);
    }

}
