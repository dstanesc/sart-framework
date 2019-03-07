package org.sartframework.projection.kafka.query;

import org.sartframework.event.QueryEvent;
import org.sartframework.projection.AnnotatedTransactionProjection;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.QueryResult;
import org.springframework.kafka.core.KafkaTemplate;

public abstract class KafkaTransactionProjection <R extends QueryResult> extends AnnotatedTransactionProjection <R> {
    

    public abstract <Q extends DomainQuery> KafkaTemplate<String, Q> getQueryWriter();
    

    public abstract KafkaTemplate<String, R> getQueryResultWriter();
    
    
    public abstract <E extends QueryEvent> KafkaTemplate<String, E> getQueryEventWriter();

    
}
