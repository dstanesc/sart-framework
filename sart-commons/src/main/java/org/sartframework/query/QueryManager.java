package org.sartframework.query;

import reactor.core.publisher.Flux;

public interface QueryManager {

    <Q extends DomainQuery, T> Flux<T> query(Q query, Class<T> out);

}
