package org.sartframework.web;

import org.sartframework.query.ConflictsByAggregateQuery;
import org.sartframework.query.ConflictsByChangeQuery;
import org.sartframework.query.ConflictsByXidQuery;
import org.sartframework.query.QueryManager;
import org.sartframework.result.ConflictResolvedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class SimplifiedConflictRestController {

    final static Logger LOGGER = LoggerFactory.getLogger(SimplifiedConflictRestController.class);

    QueryManager queryMananger;

    public SimplifiedConflictRestController(QueryManager queryMananger) {
        super();
        this.queryMananger = queryMananger;
    }

    @PostMapping("/query/conflicts/xid")
    public Flux<ConflictResolvedResult> getConflictsByXid(@RequestBody ConflictsByXidQuery conflictsByXidQuery) {
         return queryMananger.query(conflictsByXidQuery, ConflictResolvedResult.class).doOnNext(conflictResolution -> {
            LOGGER.info("Dispatched conflict resolution {} by xid {} queryKey={}", conflictResolution, conflictsByXidQuery.getXid(), conflictsByXidQuery.getQueryKey());
        });
    }

    @PostMapping("/query/conflicts/aggregate")
    public Flux<ConflictResolvedResult> getConflictsByAggregate(@RequestBody  ConflictsByAggregateQuery conflictsByAggregateQuery) {
                return queryMananger.query(conflictsByAggregateQuery, ConflictResolvedResult.class).doOnNext(conflictResolution -> {
            LOGGER.info("Dispatched conflict resolution {} by aggregate {} queryKey={}", conflictResolution, conflictsByAggregateQuery.getAggregateKey(), conflictsByAggregateQuery.getQueryKey());
        });
    }

    @PostMapping("/query/conflicts/change")
    public Flux<ConflictResolvedResult> getConflictsByChange(@RequestBody ConflictsByChangeQuery conflictsByChangeQuery) {
                return queryMananger.query(conflictsByChangeQuery, ConflictResolvedResult.class).doOnNext(conflictResolution -> {
            LOGGER.info("Dispatched conflict resolution {} by change {} queryKey={}", conflictResolution, conflictsByChangeQuery.getChangeKey(), conflictsByChangeQuery.getQueryKey());
        });
    }
}
