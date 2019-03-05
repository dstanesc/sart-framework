package org.sartframework.projection.kafka.query;

import org.sartframework.event.QueryEvent;
import org.sartframework.projection.AnnotatedDomainProjection;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.QueryResult;
import org.springframework.kafka.core.KafkaTemplate;

public abstract class KafkaDomainProjection extends AnnotatedDomainProjection {
    

    public abstract <Q extends DomainQuery> KafkaTemplate<String, Q> getQueryWriter();
    

    public abstract <R extends QueryResult> KafkaTemplate<String, R> getQueryResultWriter();
    
    
    public abstract <E extends QueryEvent> KafkaTemplate<String, E> getQueryEventWriter();
    
}
