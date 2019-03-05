package org.sartframework.demo.cae.projection.web;

import org.sartframework.demo.cae.query.InputDeckByIdQuery;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.query.InputDeckByXidQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.query.QueryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class SimplifiedSimulationProjectionRestController {

    final static Logger LOGGER = LoggerFactory.getLogger(SimplifiedSimulationProjectionRestController.class);

    QueryManager queryMananger;

    public SimplifiedSimulationProjectionRestController(QueryManager queryMananger) {
        super();
        this.queryMananger = queryMananger;
    }

    @PostMapping("/query/inputDeck/xid")
    public Flux<InputDeckQueryResult> getInputDecksByXid(InputDeckByXidQuery inputDeckByXidQuery) {
               return queryMananger.query(inputDeckByXidQuery, InputDeckQueryResult.class).doOnNext(inputDeckQueryResult -> {
            LOGGER.info("dispatched inputDeck by xid={} id={} version={} ", inputDeckByXidQuery.getXid(), inputDeckQueryResult.getInputDeckId(),
                inputDeckQueryResult.getInputDeckVersion());
        }).doOnComplete(() -> LOGGER.info("getInputDecksByXid query stream completed"));
    }

    @PostMapping("/query/inputDeck/id")
    public Flux<InputDeckQueryResult> getInputDecksById(@RequestBody InputDeckByIdQuery inputDeckByIdQuery) {
                return queryMananger.query(inputDeckByIdQuery, InputDeckQueryResult.class).doOnNext(inputDeckQueryResult -> {
            LOGGER.info("dispatched inputDeck by id={} version={} ", inputDeckQueryResult.getInputDeckId(),
                inputDeckQueryResult.getInputDeckVersion());
        }).doOnComplete(() -> LOGGER.info("getInputDecksById query stream completed"));
    }
    
    @PostMapping("/query/inputDeck/name")
    public Flux<InputDeckQueryResult> getInputDecksByName(@RequestBody InputDeckByNameQuery inputDeckByNameQuery) {
         return queryMananger.query(inputDeckByNameQuery, InputDeckQueryResult.class).doOnNext(inputDeckQueryResult -> {
            LOGGER.info("dispatched inputDeck by name={} id={} version={} ", inputDeckByNameQuery.getInputDeckName(), inputDeckQueryResult.getInputDeckId(),
                inputDeckQueryResult.getInputDeckVersion());
        }).doOnComplete(() -> LOGGER.info("getInputDecksByName query stream completed"));
    }
}
