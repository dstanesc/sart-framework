package org.sartframework.driver;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionStatus.Isolation;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.query.DomainQuery;
import org.sartframework.session.SystemSnapshot;
import org.sartframework.session.SystemTransaction;
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

public class DefaultRestTransactionDriver implements TransactionDriverInternal, RestTransactionDriver  {

    final static Logger LOGGER = LoggerFactory.getLogger(DefaultRestTransactionDriver.class);

    RestTransactionApi transactionApi;

    Set<RestQueryApi> queryApis = new LinkedHashSet<>();

    Set<RestCommandApi> commandApis = new LinkedHashSet<>();
    
    WebClient transactionClient;

    
    public DefaultRestTransactionDriver() {
    }

    
    @Override
    public String getSid() {
        // FIXME dstanesc -- Auto-generated method stub
        return null;
    }


    @Override
    public RestTransactionDriver init() {
        this.transactionClient = WebClient.create(transactionApi.toUrl());
        return this;
    }
    
    public RestTransactionDriver registerTransactionApi(RestTransactionApi transactionApi) {
        this.transactionApi = transactionApi;
        return this;
    }

    @Override
    public RestTransactionDriver registerQueryApi(RestQueryApi projectionListener) {
        queryApis.add(projectionListener);
        return this;
    }

    @Override
    public RestTransactionDriver registerCommandApi(RestCommandApi api) {
        commandApis.add(api);
        return this;
    }
    

    public Set<RestQueryApi> getQueryApis() {
        return queryApis;
    }

    public Set<RestCommandApi> getCommandApis() {
        return commandApis;
    }

    @Override
    public DomainTransaction createDomainTransaction() {

        return createDomainTransaction(Isolation.READ_SNAPSHOT);
    }

    @Override
    public DomainTransaction createDomainTransaction(Isolation isolation) {

        return new DefaultDomainTransaction(this).setIsolation(isolation).next();
    }


    @Override
    public SystemTransaction nextTransactionInternal() {

        try {
            
            String apiUrl = "/transaction/get";

            Request request = Request.Get(transactionApi.toUrl() + apiUrl);

            String jsonTransaction = performRequest(request);

            LOGGER.info("Acquired unique sid, xid {}", jsonTransaction);

            ObjectMapper mapper = new ObjectMapper();

            SystemTransaction systemTransaction = mapper.readValue(jsonTransaction, SystemTransaction.class);
            
            return systemTransaction;
            
        } catch (JsonParseException e) {
            
           throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startTransactionInternal(long xid, int isolation) {

        try {
            LOGGER.info("Start transaction {}", xid);

            String apiUrl = "/transaction/" + xid + "/" + isolation + "/start";

            Request request = Request.Post(transactionApi.toUrl() + apiUrl);

            performMappedRequest(request);
            
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void commitTransactionInternal(long xid, long xct) {

        try {
            LOGGER.info("Commit transaction xid={}, xct={}", xid, xct);

            String apiUrl = "/transaction/" + xid + "/" + xct + "/commit";

            Request request = Request.Patch(transactionApi.toUrl() + apiUrl);

            performMappedRequest(request);
            
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void abortTransactionInternal(long xid) {

        try {
            LOGGER.info("Rollback transaction {}", xid);

            String apiUrl = "/transaction/" + xid + "/abort";

            Request request = Request.Patch(transactionApi.toUrl() + apiUrl);

            performMappedRequest(request);
            
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int statusTransactionInternal(long xid)  {

        try {
            String apiUrl = "/transaction/" + xid + "/status";

            Request request = Request.Get(transactionApi.toUrl() + apiUrl);

            int status = Integer.parseInt(performRequest(request));

            LOGGER.info("Retrieved transaction status {} -> {}", xid, status);

            return status;
            
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SystemSnapshot snapshotTransactionInternal(long xid) {

        try {
            String apiUrl = "/transaction/" + xid + "/snapshot";

            Request request = Request.Get(transactionApi.toUrl() + apiUrl);

            String jsonSnapshot = performRequest(request);

            LOGGER.info("Retrieved transaction snapshot {} -> {}", xid, jsonSnapshot);

            ObjectMapper mapper = new ObjectMapper();

            SystemSnapshot snapshot = mapper.readValue(jsonSnapshot, SystemSnapshot.class);

            return snapshot;
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        List<RestQueryApi> apis = queryApis.stream().filter(api -> api.hasQuerySupport(queryType)).collect(Collectors.toList());
        
        if (!apis.isEmpty()) {
            
            for (RestQueryApi api : apis) {
                
                onQuery(xid, isolation, systemSnapshot, subscribe, domainQuery, resultType, resultConsumer, errorConsumer, onComplete, api);
            }
            
        } else
            throw new UnsupportedOperationException("Unsupported query " + domainQuery);
    }

    
    @Override
    public <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery,
                                                   Class<R> resultType, Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer,
                                                   Runnable onComplete, RestQueryApi restApi) {

        Class<? extends DomainQuery> queryType = domainQuery.getClass();

        domainQuery.setQueryKey(UUID.randomUUID().toString());
        domainQuery.setQueryXid(xid);
        domainQuery.setSystemSnapshot(systemSnapshot);
        domainQuery.setQuerySubscription(subscribe);
        domainQuery.setIsolation(isolation);

        if (restApi.hasQuerySupport(queryType)) {

            RequestMapping requestMapping = restApi.getQuerySupportApiUrl(domainQuery.getClass());

            WebClient projectionClient = WebClient.create(restApi.toUrl());

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
    
    
    @Override
    public <C extends DomainCommand> void sendCommand(C domainCommand) {

        Class<? extends DomainCommand> commandType = domainCommand.getClass();

        Optional<RestCommandApi> apiOptional = commandApis.stream().filter(api -> api.hasCommandSupport(commandType)).findFirst();

        if (apiOptional.isPresent()) {

            RestCommandApi remoteApi = apiOptional.get();

            RequestMapping requestMapping = remoteApi.getCommandSupportApiUrl(domainCommand.getClass());

            RequestMethod requestMethod = requestMapping.getMethod();

            String url = remoteApi.toUrl() + requestMapping.getUrl();

            final Request request;

            switch (requestMethod) {
                case POST:
                    request = Request.Post(url);
                    break;
                case PATCH:
                    request = Request.Patch(url);
                    break;
                case PUT:
                    request = Request.Put(url);
                    break;
                default:
                    throw new UnsupportedOperationException("Method not handled " + requestMethod);
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try {

                String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(domainCommand);

                String status = request.connectTimeout(100000).socketTimeout(100000).addHeader("Content-Type", "application/json")
                    .bodyString(payload, ContentType.APPLICATION_JSON).execute().returnContent().asString();

                LOGGER.info("Send command returned : " + status);
            } catch (IOException e) {

                throw new RuntimeException(e);
            }

        } else
            throw new UnsupportedOperationException("Unsupported command " + domainCommand);
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
