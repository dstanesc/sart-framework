package org.sartframework.driver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionStatus.Isolation;
import org.sartframework.driver.RemoteApi;
import org.sartframework.driver.RequestMapping;
import org.sartframework.driver.RequestMethod;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.query.DomainQuery;
import org.sartframework.session.SystemSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RemoteTransactionDriver implements TransactionDriverInternal, TransactionDriver, RemoteDriver  {

    final static Logger LOGGER = LoggerFactory.getLogger(RemoteTransactionDriver.class);

    RemoteApi transactionApi;

    Set<RemoteApi> projectionApis = new HashSet<>();

    Set<RemoteApi> commandApis = new HashSet<>();

    WebClient transactionClient;

    public RemoteTransactionDriver() {
    }

    @Override
    public TransactionDriver init() {
        this.transactionClient = WebClient.create(transactionApi.toUrl());
        return this;
    }

    public RemoteDriver registerTransactionApi(RemoteApi transactionListener) {
        this.transactionApi = transactionListener;
        return this;
    }

    @Override
    public RemoteDriver registerProjectionApi(RemoteApi projectionListener) {
        projectionApis.add(projectionListener);
        return this;
    }

    @Override
    public TransactionDriver registerCommandApi(RemoteApi api) {
        commandApis.add(api);
        return this;
    }

    @Override
    public DomainTransaction createDomainTransaction() {

        return new DefaultDomainTransaction(this, this).next();
    }

    @Override
    public DomainTransaction createDomainTransaction(Isolation isolation) {

        return new DefaultDomainTransaction(this, this).setIsolation(isolation).next();
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

    @Override
    public void onStart(Consumer<TransactionStartedEvent> startConsumer, Long xid) {

        LOGGER.info("Subscribe to start event");

        String apiUrl = "/transaction/{xid}/startListener";

        Mono<TransactionStartedEvent> startedMono = transactionClient.get().uri(apiUrl, xid).retrieve().bodyToMono(TransactionStartedEvent.class);

        startedMono.subscribe(startConsumer);
    }

    @Override
    public void onCommit(Consumer<TransactionCommittedEvent> commitConsumer, long xid) {

        LOGGER.info("Subscribe to commit event");

        String apiUrl = "/transaction/{xid}/commitListener";

        Mono<TransactionCommittedEvent> committedMono = transactionClient.get().uri(apiUrl, xid).retrieve()
            .bodyToMono(TransactionCommittedEvent.class);

        committedMono.subscribe(commitConsumer);
    }

    @Override
    public void onAbort(Consumer<TransactionAbortedEvent> abortConsumer, long xid) {

        LOGGER.info("Subscribe to abort event");

        String apiUrl = "/transaction/{xid}/abortListener";

        Mono<TransactionAbortedEvent> commitedMono = transactionClient.get().uri(apiUrl, xid).retrieve().bodyToMono(TransactionAbortedEvent.class);

        commitedMono.subscribe(abortConsumer);
    }

    @Override
    public void onComplete(Consumer<TransactionCompletedEvent> completeConsumer, long xid) {

        LOGGER.info("Subscribe to complete event");

        String apiUrl = "/transaction/{xid}/completeListener";

        Mono<TransactionCompletedEvent> completedMono = transactionClient.get().uri(apiUrl, xid).retrieve()
            .bodyToMono(TransactionCompletedEvent.class);

        completedMono.subscribe(completeConsumer);
    }

    @Override
    public void onConflict(Consumer<ConflictResolvedEvent> conflictConsumer, Long xid) {

        LOGGER.info("Subscribe to conflict resolved event");

        String apiUrl = "/transaction/{xid}/conflictListener";

        Flux<ConflictResolvedEvent> progressFlux = transactionClient.get().uri(apiUrl, xid).accept(MediaType.APPLICATION_STREAM_JSON).retrieve()
            .bodyToFlux(ConflictResolvedEvent.class);

        progressFlux.subscribe(conflictConsumer);
    }

    @Override
    public <T extends DomainEvent<? extends DomainCommand>> void onProgress(Consumer<T> progressConsumer, Class<T> eventType, long xid) {

        LOGGER.info("Subscribe to transaction progress events of {}", eventType);

        String apiUrl = "/transaction/{xid}/{eventType}/progressListener";

        Flux<T> progressFlux = transactionClient.get().uri(apiUrl, xid, eventType.getSimpleName()).accept(MediaType.APPLICATION_STREAM_JSON)
            .retrieve().bodyToFlux(eventType);

        progressFlux.subscribe(progressConsumer);
    }

    @Override
    public <T extends DomainEvent<? extends DomainCommand>> void onCompensate(Consumer<T> compensateConsumer, Class<T> eventType, long xid) {

        LOGGER.info("Subscribe to transaction compensate events of {}", eventType);

        String apiUrl = "/transaction/{xid}/{eventType}/compensateListener";

        Flux<T> compensateFlux = transactionClient.get().uri(apiUrl, xid, eventType.getSimpleName()).accept(MediaType.APPLICATION_STREAM_JSON)
            .retrieve().bodyToFlux(eventType);

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

        Optional<RemoteApi> apiOptional = projectionApis.stream().filter(api -> api.hasQuerySupport(queryType)).findFirst();

        if (apiOptional.isPresent()) {

            RemoteApi remoteApi = apiOptional.get();

            RequestMapping requestMapping = remoteApi.getQuerySupportApiUrl(domainQuery.getClass());

            WebClient projectionClient = WebClient.create(remoteApi.toUrl());

            // Flux<R> resultFlux =
            // projectionClient.get().uri(requestMapping.getUrl(),
            // domainQuery.getVariables().getContent())
            // .accept(MediaType.APPLICATION_STREAM_JSON).retrieve().bodyToFlux(resultType);

            RequestMethod requestMethod = requestMapping.getMethod();

            final RequestBodyUriSpec request;

            switch (requestMethod) {
                case POST:
                    request = projectionClient.post();
                    break;
                case PATCH:
                    request = projectionClient.patch();
                    break;
                case PUT:
                    request = projectionClient.put();
                    break;
                default:
                    throw new UnsupportedOperationException("Method not handled " + requestMethod);
            }

            Flux<R> resultFlux = request.uri(requestMapping.getUrl()).accept(MediaType.APPLICATION_STREAM_JSON)
                .body(BodyInserters.fromObject(domainQuery)).retrieve().bodyToFlux(resultType);

            resultFlux.subscribe(resultConsumer, errorConsumer, onComplete);
            
           

        } else
            throw new UnsupportedOperationException("Unsupported query " + domainQuery);
    }

    // https://www.baeldung.com/spring-5-webclient

    @Override
    public <C extends DomainCommand> void sendCommand(C domainCommand) {

        Class<? extends DomainCommand> commandType = domainCommand.getClass();

        Optional<RemoteApi> apiOptional = commandApis.stream().filter(api -> api.hasCommandSupport(commandType)).findFirst();

        if (apiOptional.isPresent()) {

            RemoteApi remoteApi = apiOptional.get();

            RequestMapping requestMapping = remoteApi.getCommandSupportApiUrl(domainCommand.getClass());

            // WebClient commandClient = WebClient.create(webApi.toUrl());

            RequestMethod requestMethod = requestMapping.getMethod();

            String url = remoteApi.toUrl() + requestMapping.getUrl();

            // final RequestBodyUriSpec request;

            final Request request;

            switch (requestMethod) {
                case POST:
                    // request = commandClient.post();
                    request = Request.Post(url);
                    break;
                case PATCH:
                    // request = commandClient.patch();
                    request = Request.Patch(url);
                    break;
                case PUT:
                    // request = commandClient.put();
                    request = Request.Put(url);
                    break;
                default:
                    throw new UnsupportedOperationException("Method not handled " + requestMethod);
            }

            // request.uri(requestMapping.getUrl()).accept(MediaType.APPLICATION_JSON_UTF8)
            // .body(BodyInserters.fromObject(domainCommand)).exchange().subscribe();

            // FIXME use blocking apache client or elaborate a non blocking
            // pattern for a sequence of commands

            // HttpStatus status =
            // request.uri(requestMapping.getUrl()).accept(MediaType.APPLICATION_JSON_UTF8)
            // .body(BodyInserters.fromObject(domainCommand)).exchange().block().statusCode();

            ObjectMapper objectMapper = new ObjectMapper();

            try {

                String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(domainCommand);

                String status = request.connectTimeout(100000).socketTimeout(100000).addHeader("Content-Type", "application/json")
                    .bodyString(payload, ContentType.APPLICATION_JSON).execute().returnContent().asString();

                LOGGER.info("Send command returned : " + status);
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
            //
            // if (status.compareTo(HttpStatus.OK) != 0)
            // throw new RuntimeException("Wrong status");

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
