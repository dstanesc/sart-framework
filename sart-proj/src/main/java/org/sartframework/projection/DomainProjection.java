package org.sartframework.projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.sartframework.aggregate.SynchHandler;
import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.EmptyResult;
import org.sartframework.result.EndResult;
import org.sartframework.result.QueryResult;
import org.sartframework.session.SystemSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DomainProjection<P extends ProjectedEntity, R extends QueryResult>
    extends SynchHandler<DomainEvent<? extends DomainCommand>, Long>, QueryHandler<R>, ProjectionConfiguration {

    final static Logger LOGGER = LoggerFactory.getLogger(DomainProjection.class);

    String getName();

    <E extends DomainEvent<?>> boolean hasEventType(Class<E> eventType);

    <Q extends DomainQuery> boolean hasQueryType(Class<Q> queryType);

    <Q extends DomainQuery> void unsubscribe(String queryKey);

    default List<P> filterVisibility(DomainQuery domainQuery, List<P> entityList) {

        int isolation = domainQuery.getIsolation();

        SystemSnapshot systemSnapshot = domainQuery.getSystemSnapshot();

        if (isolation == DomainQuery.READ_UNCOMMITTED_ISOLATION
            || systemSnapshot.isEmpty() /* first transaction in the system */)
            return entityList.stream().filter(entity -> {

                long xmax = entity.getXmax();

                boolean visible = xmax == ProjectedEntity.XMAX_NOT_SET;

                return visible;

            }).collect(Collectors.toList());

        else if (isolation == DomainQuery.READ_COMMITTED_ISOLATION || isolation == DomainQuery.READ_SNAPSHOT_ISOLATION) {

            Long highestCommitted = systemSnapshot.getHighestCommitted();

            SortedSet<Long> running = systemSnapshot.getRunning();

            long xid = domainQuery.getQueryXid();

            return entityList.stream().filter(entity -> {

                try {

                    long xmin = entity.getXmin();

                    long xmax = entity.getXmax();

                    boolean visible = xmin == xid
                        || (xmin <= highestCommitted && !running.contains(xmin) && (xmax == ProjectedEntity.XMAX_NOT_SET || xmax > xid));

                    LOGGER.info(
                        "Returned = {} :  xmin {} == xid {} || (xmin {} <= highestCommitted {} AND  xmin {} /= running transactions {} AND (xmax {} == NOT SET OR xmax {} > xid {})) ",
                        visible, xmin, xid, xmin, highestCommitted, xmin, running, xmax, xmax, xid);

                    return visible;

                } catch (Exception e) {

                    throw new RuntimeException();
                }

            }).collect(Collectors.toList());
        }

        else
            throw new UnsupportedOperationException("Invalid transaction isolation level : " + isolation);
    }

    default List<? extends QueryResult> resultList(DomainQuery query, List<P> entityList) {
        
        return entityList.isEmpty() ? emptyResult(query) : nonEmptyResult(query, entityList);
    }

    default List<? extends QueryResult> emptyResult(DomainQuery query) {
        
        return query.isQuerySubscription() ? new ArrayList<>(0) : Arrays.asList(new EmptyResult(query.getSystemSnapshot().getSid(), query.getQueryKey()));
    }

    default List<? extends QueryResult> nonEmptyResult(DomainQuery query, List<P> entityList) {

        List<QueryResult> resultList = entityList.stream().map(e -> {

            return newQueryResult(query, e);

        }).collect(Collectors.toList());

        if (!query.isQuerySubscription()) {
            resultList.add(new EndResult());
        }

        return resultList;
    }

    R newQueryResult(DomainQuery query, P projectedEntity);
}
