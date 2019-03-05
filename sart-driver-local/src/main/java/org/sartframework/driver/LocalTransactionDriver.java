package org.sartframework.driver;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionStatus.Isolation;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.query.QueryUnsubscribedEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.kafka.channels.KafkaWriters;
import org.sartframework.projection.ProjectionConfiguration;
import org.sartframework.projection.kafka.services.QueryResultListenerService;
import org.sartframework.query.DomainQuery;
import org.sartframework.session.SystemSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

public class LocalTransactionDriver implements TransactionDriverInternal, TransactionDriver, LocalDriver {

    final static Logger LOGGER = LoggerFactory.getLogger(LocalTransactionDriver.class);

    RemoteApi transactionApi;

    Set<QueryLocalApi> projectionApis = new HashSet<>();

    Set<CommandLocalApi> commandApis = new HashSet<>();

    WebClient transactionClient;

    final private KafkaWriters writeChannels;

    Map<Long,ClientTransactionLifecycleMonitorService> monitorServices = new HashMap<>();

    public LocalTransactionDriver(KafkaWriters writeChannels) {
        super();
        this.writeChannels = writeChannels;
    }

    @Override
    public TransactionDriver init() {
        this.transactionClient = WebClient.create(transactionApi.toUrl());
        return this;
    }

    @Override
    public LocalDriver registerTransactionApi(RemoteTransactionApi transactionListener) {
        this.transactionApi = transactionListener;
        return this;
    }

    @Override
    public LocalDriver registerProjectionApi(QueryLocalApi projectionListener) {
        projectionApis.add(projectionListener);
        return this;
    }

    @Override
    public LocalDriver registerCommandApi(CommandLocalApi api) {
        commandApis.add(api);
        return this;
    }

    @Override
    public DomainTransaction createDomainTransaction() {

        return createDomainTransaction(Isolation.READ_SNAPSHOT);
    }

    @Override
    public DomainTransaction createDomainTransaction(Isolation isolation) {

        DomainTransaction transaction = new DefaultDomainTransaction(this, this).setIsolation(isolation).next();

        long xid = transaction.getXid();

        ClientTransactionLifecycleMonitorService transactionLifecycleMonitorService = new ClientTransactionLifecycleMonitorService(writeChannels.getSartKafkaConfiguration(), xid).start();

        monitorServices.put(xid, transactionLifecycleMonitorService);
        
        onComplete(c -> {
            
            ClientTransactionLifecycleMonitorService monitorService = monitorServices.remove(xid);
            
            monitorService.stop();
            
        }, xid);
        
        return transaction;
    }

    @Override
    public long nextTransactionInternal() throws IOException {

        String apiUrl = "/transaction/get";

        Request request = Request.Get(transactionApi.toUrl() + apiUrl);

        long xid = Long.parseLong(performRequest(request));

        LOGGER.info("Acquired unique xid {}", xid);

        return xid;
    }

    @Override
    public void startTransactionInternal(long xid, int isolation) throws IOException {

        LOGGER.info("Start transaction {}", xid);

        String apiUrl = "/transaction/" + xid + "/" + isolation + "/start";

        Request request = Request.Post(transactionApi.toUrl() + apiUrl);

        performMappedRequest(request);

    }

    @Override
    public void commitTransactionInternal(long xid, long xct) throws IOException {

        LOGGER.info("Commit transaction xid={}, xct={}", xid, xct);

        String apiUrl = "/transaction/" + xid + "/" + xct + "/commit";

        Request request = Request.Patch(transactionApi.toUrl() + apiUrl);

        performMappedRequest(request);

    }

    @Override
    public void abortTransactionInternal(long xid) throws IOException {

        LOGGER.info("Rollback transaction {}", xid);

        String apiUrl = "/transaction/" + xid + "/abort";

        Request request = Request.Patch(transactionApi.toUrl() + apiUrl);

        performMappedRequest(request);

    }

    @Override
    public int statusTransactionInternal(long xid) throws IOException {

        String apiUrl = "/transaction/" + xid + "/status";

        Request request = Request.Get(transactionApi.toUrl() + apiUrl);

        int status = Integer.parseInt(performRequest(request));

        LOGGER.info("Retrieved transaction status {} -> {}", xid, status);

        return status;
    }

    @Override
    public SystemSnapshot snapshotTransactionInternal(long xid) throws IOException {

        String apiUrl = "/transaction/" + xid + "/snapshot";

        Request request = Request.Get(transactionApi.toUrl() + apiUrl);

        String jsonSnapshot = performRequest(request);

        LOGGER.info("Retrieved transaction snapshot {} -> {}", xid, jsonSnapshot);

        ObjectMapper mapper = new ObjectMapper();

        SystemSnapshot snapshot = mapper.readValue(jsonSnapshot, SystemSnapshot.class);

        return snapshot;
    }

    private ClientTransactionLifecycleMonitorService getMonitorService(Long xid) {
        
        ClientTransactionLifecycleMonitorService transactionLifecycleMonitorService = monitorServices.get(xid);
        
        if(transactionLifecycleMonitorService == null) throw new IllegalStateException("Lifecycle monitor service not available");
        
       return transactionLifecycleMonitorService;
    }
    
    
    @Override
    public void onStart(Consumer<TransactionStartedEvent> startConsumer, Long xid) {

        LOGGER.info("Subscribe to start event");
        
        ClientTransactionLifecycleMonitorService monitorService = getMonitorService(xid);

        Mono<TransactionStartedEvent> startedMono = monitorService.getTransactionMonitors().startMonitor();

        startedMono.subscribe(startConsumer);
    }

    @Override
    public void onCommit(Consumer<TransactionCommittedEvent> commitConsumer, long xid) {

        LOGGER.info("Subscribe to commit event");
        
        ClientTransactionLifecycleMonitorService monitorService = getMonitorService(xid);

        Mono<TransactionCommittedEvent> committedMono = monitorService.getTransactionMonitors().commitMonitor();

        committedMono.subscribe(commitConsumer);
    }

    @Override
    public void onAbort(Consumer<TransactionAbortedEvent> abortConsumer, long xid) {

        LOGGER.info("Subscribe to abort event");
        
        ClientTransactionLifecycleMonitorService monitorService = getMonitorService(xid);

        Mono<TransactionAbortedEvent> abortedMono = monitorService.getTransactionMonitors().abortMonitor();

        abortedMono.subscribe(abortConsumer);
    }

    @Override
    public void onComplete(Consumer<TransactionCompletedEvent> completeConsumer, long xid) {

        LOGGER.info("Subscribe to complete event");

        ClientTransactionLifecycleMonitorService monitorService = getMonitorService(xid);
        
        Mono<TransactionCompletedEvent> completedMono = monitorService.getTransactionMonitors().completeMonitor();

        completedMono.subscribe(completeConsumer);
    }

    @Override
    public void onConflict(Consumer<ConflictResolvedEvent> conflictConsumer, Long xid) {

        LOGGER.info("Subscribe to conflict resolved event");

        ClientTransactionLifecycleMonitorService monitorService = getMonitorService(xid);
        
        Flux<ConflictResolvedEvent> progressFlux = monitorService.getTransactionMonitors().conflictResolvedMonitor();

        progressFlux.subscribe(conflictConsumer);
    }

    @Override
    public <T extends DomainEvent<? extends DomainCommand>> void onProgress(Consumer<T> progressConsumer, Class<T> eventType, long xid) {

        LOGGER.info("Subscribe to transaction progress events of {}", eventType);
        
        ClientTransactionLifecycleMonitorService monitorService = getMonitorService(xid);

        ReplayProcessor<DomainEvent<? extends DomainCommand>> progressMonitor = monitorService.getTransactionMonitors()
            .progressMonitor();

        Flux<T> progressFlux = (Flux<T>) progressMonitor.filter(e -> e.getClass().equals(eventType));

        progressFlux.subscribe(progressConsumer);
    }

    @Override
    public <T extends DomainEvent<? extends DomainCommand>> void onCompensate(Consumer<T> compensateConsumer, Class<T> eventType, long xid) {

        LOGGER.info("Subscribe to transaction compensate events of {}", eventType);
        
        ClientTransactionLifecycleMonitorService monitorService = getMonitorService(xid);

        ReplayProcessor<DomainEvent<? extends DomainCommand>> compensateMonitor = monitorService.getTransactionMonitors()
            .compensateMonitor();

        Flux<T> compensateFlux = (Flux<T>) compensateMonitor.filter(e -> e.getClass().equals(eventType));

        compensateFlux.subscribe(compensateConsumer);
    }

    @Override
    public <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery,
                                                   Class<R> resultType, Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer,
                                                   Runnable onComplete) {

        Class<? extends DomainQuery> queryType = domainQuery.getClass();

        domainQuery.setQueryKey(UUID.randomUUID().toString());
        domainQuery.setQueryXid(xid);
        domainQuery.setSystemSnapshot(systemSnapshot);
        domainQuery.setQuerySubscription(subscribe);
        domainQuery.setIsolation(isolation);

        Optional<QueryLocalApi> apiOptional = projectionApis.stream().filter(api -> api.hasQuerySupport(queryType)).findFirst();

        if (apiOptional.isPresent()) {

            String queryKey = domainQuery.getQueryKey();

            QueryLocalApi projectionInternalApi = apiOptional.get();

            ProjectionConfiguration domainProjection = projectionInternalApi.getQuerySupportProjection(queryType);

            QueryResultListenerService<R> resultListenerService = new QueryResultListenerService<R>(writeChannels.getSartKafkaConfiguration(),
                domainProjection, domainQuery).start();

            resultListenerService.getResultPublisher().subscribe(resultConsumer);

            // FIXME get topic from projection
            writeChannels.domainQueryWriter().sendDefault(domainQuery.getQueryKey(), domainQuery);

            resultListenerService.getResultPublisher().doAfterTerminate(() -> {

                // FIXME get topic from projection
                writeChannels.domainQueryEventWriter().sendDefault(queryKey, new QueryUnsubscribedEvent(queryKey));

                resultListenerService.stop();
            });

        } else
            throw new UnsupportedOperationException("Unsupported query " + domainQuery);
    }

    @Override
    public <C extends DomainCommand> void sendCommand(C domainCommand) {

        Class<? extends DomainCommand> commandType = domainCommand.getClass();

        Optional<CommandLocalApi> apiOptional = commandApis.stream().filter(api -> api.hasCommandSupport(commandType)).findFirst();

        if (apiOptional.isPresent()) {

            CommandLocalApi remoteApi = apiOptional.get();

            writeChannels.domainCommandWriter().sendDefault(domainCommand.getAggregateKey(), domainCommand);

        } else
            throw new UnsupportedOperationException("Unsupported command " + domainCommand);
    }

    @Override
    public <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery,
                                                   Class<R> resultType, Consumer<R> resultConsumer, Runnable onComplete) {
        onQuery(xid, isolation, systemSnapshot, subscribe, domainQuery, resultType, resultConsumer, null, onComplete);
    }

    @Override
    public <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery,
                                                   Class<R> resultType, Consumer<R> resultConsumer) {
        onQuery(xid, isolation, systemSnapshot, subscribe, domainQuery, resultType, resultConsumer, null);
    }

    private void performMappedRequest(Request request) throws ClientProtocolException, IOException, JsonParseException, JsonMappingException {

        String jsonString = performRequest(request);

        LOGGER.info(jsonString);
    }

    private String performRequest(Request request) throws ClientProtocolException, IOException {
        return request.connectTimeout(100000).socketTimeout(100000).addHeader("Content-Type", "application/json").execute().returnContent()
            .asString();
    }
}
