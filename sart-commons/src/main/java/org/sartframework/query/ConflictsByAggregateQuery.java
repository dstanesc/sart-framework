package org.sartframework.query;

import org.sartframework.annotation.Evolvable;

@Evolvable(version = 1)
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
