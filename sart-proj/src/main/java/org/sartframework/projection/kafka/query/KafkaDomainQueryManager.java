package org.sartframework.projection.kafka.query;

import javax.annotation.PostConstruct;

import org.sartframework.event.query.QueryUnsubscribedEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.projection.kafka.services.QueryResultListenerService;
import org.sartframework.query.DomainQuery;
import org.sartframework.query.QueryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

public class KafkaDomainQueryManager implements QueryManager {

    final static Logger LOGGER = LoggerFactory.getLogger(KafkaDomainQueryManager.class);

    private static QueryManager instance;

    final private SartKafkaConfiguration kafkaStreamsConfiguration;
    
    final KafkaDomainProjection domainProjection;

    public KafkaDomainQueryManager(SartKafkaConfiguration kafkaConfiguration, KafkaDomainProjection domainProjection) {
        super();
        this.kafkaStreamsConfiguration = kafkaConfiguration;
        this.domainProjection = domainProjection;
    }

    @PostConstruct
    public QueryManager init() {

        instance = this;

        return this;
    }

    public static QueryManager get() {

        return instance;
    }

    @Override
    public <Q extends DomainQuery, T> Flux<T> query(Q domainQuery, Class<T> resultType) {
        
        String queryKey = domainQuery.getQueryKey();
        //create stream before publishing the query
        QueryResultListenerService<T> resultListenerService = new QueryResultListenerService<T>(kafkaStreamsConfiguration, domainProjection, domainQuery).start();

        domainProjection.getQueryWriter().sendDefault(domainQuery.getQueryKey(), domainQuery);
        
        return resultListenerService.getResultPublisher().doAfterTerminate(() -> {

            domainProjection.getQueryEventWriter().sendDefault(queryKey, new QueryUnsubscribedEvent(queryKey));

            resultListenerService.stop();
        });
    }

}
