package org.sartframework.transaction;

import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.AttachTransactionDetailsCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.EventDescriptor;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionDetailsAttachedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.session.SystemSnapshot;
import org.sartframework.session.SystemTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class TransactionRestController {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionRestController.class);

    private final BusinessTransactionManager transactionManager;

    public TransactionRestController(BusinessTransactionManager txnManager) {
        super();
        this.transactionManager = txnManager;
    }

    @GetMapping("/transaction/get")
    public SystemTransaction getTransaction() {
        return transactionManager.nextTransaction();
    }

    @PostMapping("/transaction/{xid}/{isolation}/start")
    @ResponseStatus(value = HttpStatus.OK)
    public void startTransaction(@PathVariable long xid, @PathVariable int isolation) {
        transactionManager.startTransaction(xid, isolation);
    }

    @PatchMapping("/transaction/{xid}/{xct}/commit")
    @ResponseStatus(value = HttpStatus.OK)
    public void commitTransaction(@PathVariable long xid, @PathVariable long xct) {
        transactionManager.commitTransaction(xid, xct);
    }

    @PatchMapping("/transaction/{xid}/abort")
    @ResponseStatus(value = HttpStatus.OK)
    public void abortTransaction(@PathVariable long xid) {
        transactionManager.abortTransaction(xid);
    }
    
    @PatchMapping("/transaction/{xid}/attachDetails")
    @ResponseStatus(value = HttpStatus.OK)
    public void addResult(@RequestBody AttachTransactionDetailsCommand attachTransactionDetails) {
        transactionManager.publish(attachTransactionDetails);
    }

    @GetMapping("/transaction/{xid}/status")
    public int getTransactionStatus(@PathVariable long xid) {
        return transactionManager.status(xid);
    }

    @GetMapping("/transaction/{xid}/snapshot")
    public SystemSnapshot getSystemSnapshot(@PathVariable long xid) {
        return transactionManager.systemSnapshot(xid);
    }

    @GetMapping("/transaction/{xid}/startListener")
    public Mono<TransactionStartedEvent> startListener(@PathVariable long xid) {
        
        Mono<TransactionStartedEvent> startMono = transactionManager.startListener(xid)
            .doOnSubscribe(s -> LOGGER.info("Start listener subscribed {}", s))
            .doOnError(e -> LOGGER.error("Start listener error {}", e))
            .doOnSuccess(e -> LOGGER.info("dispatched start message {} ", e))
            .doOnTerminate(() -> LOGGER.info("Start listener terminated"))
            .doOnCancel(() -> LOGGER.info("Start listener cancelled"));

        return startMono;
    }
    
    @GetMapping("/transaction/{xid}/commitListener")
    public Mono<TransactionCommittedEvent> commitListener(@PathVariable long xid) {

        return transactionManager.commitListener(xid).doOnSuccess(e -> {
            LOGGER.info("dispatched commit message {} ", e);
        }).doOnTerminate(() -> LOGGER.info("Commit listener terminated")).doOnCancel(() -> LOGGER.info("Commit listener cancelled"));
    }

    @GetMapping("/transaction/{xid}/abortListener")
    public Mono<TransactionAbortedEvent> abortListener(@PathVariable long xid) {

        return transactionManager.abortListener(xid).doOnSuccess(e -> {
            LOGGER.info("dispatched abort message {} ", e);
        }).doOnTerminate(() -> LOGGER.info("Abort listener terminated")).doOnCancel(() -> LOGGER.info("Abort listener cancelled"));
    }
    

    @GetMapping("/transaction/{xid}/completeListener")
    public Mono<TransactionCompletedEvent> completeListener(@PathVariable long xid) {

        return transactionManager.completeListener(xid).doOnSuccess(e -> {
            LOGGER.info("dispatched complete message {} ", e);
        }).doOnTerminate(() -> LOGGER.info("Complete listener terminated"))
            .doOnCancel(() -> LOGGER.info("Complete listener cancelled"));
    }
    
    @GetMapping("/transaction/{xid}/detailsAttachedListener")
    public Flux<TransactionDetailsAttachedEvent> detailsAttachedListener(@PathVariable long xid) {

        return transactionManager.detailsAttachedListener(xid).doOnNext(e -> {
            LOGGER.info("Details attached message {} ", e);
        }).doOnTerminate(() -> LOGGER.info("Details attached listener terminated"))
            .doOnCancel(() -> LOGGER.info("Details attached listener cancelled"));
    }
    
    
    @GetMapping("/transaction/{xid}/conflictListener")
    public Flux<ConflictResolvedEvent> conflictListener(@PathVariable long xid) {

        return transactionManager.conflictListener(xid).doOnNext(e -> {
            LOGGER.info("Conflict resolved message {} ", e);
        }).doOnTerminate(() -> LOGGER.info("Conflict resolution listener terminated"))
            .doOnCancel(() -> LOGGER.info("Conflict resolution listener cancelled"));
    }

    @GetMapping(path = "/transaction/{xid}/{eventType}/progressListener")
    public Flux<DomainEvent<? extends DomainCommand>> progressByEventType(@PathVariable long xid, @PathVariable String eventType) {
        return transactionManager.transactionProgressEvents(xid).doOnNext(e -> {
            LOGGER.info("Progress message {} ", e);
        }).filter(e -> e.getClass().getSimpleName().equals(eventType)).doOnNext(e -> {
            LOGGER.info("Dispatch filtered by {} progress message {}", eventType, e);
        }).doOnTerminate(() -> LOGGER.info("Progress listener for {} terminated", eventType))
            .doOnCancel(() -> LOGGER.info("Progress listener for {} cancelled", eventType));
    }

    @GetMapping("/transaction/{xid}/{eventType}/compensateListener")
    public Flux<DomainEvent<? extends DomainCommand>> compensateByEventType(@PathVariable long xid, @PathVariable String eventType) {
        return transactionManager.transactionCompensationEvents(xid).doOnNext(e -> {
            LOGGER.info("Compensate message {} ", e);
        }).filter(e -> e.getClass().getSimpleName().equals(eventType)).doOnNext(e -> {
            LOGGER.info("Dispatch filtered by {} compensate message {}", eventType, e);
        }).doOnTerminate(() -> LOGGER.info("Compensate listener for {} terminated", eventType))
            .doOnCancel(() -> LOGGER.info("Compensate listener for {} cancelled", eventType));
    }

    @GetMapping("/transaction/events")
    public Flux<EventDescriptor> transactionEvents() {
        return transactionManager.eventDescriptor().doOnNext(e -> {
            LOGGER.info("Transaction event stream element {} ", e);
        }).doOnTerminate(() -> LOGGER.info("Transaction event stream listener terminated"))
            .doOnCancel(() -> LOGGER.info("Transaction event stream listener cancelled"));
    }

    
    
    
    // needed to include the raw json for winner/other events
    // class ConflictResolutionSerializer extends
    // JsonSerializer<ConflictResolvedLoggedEvent> {
    //
    // @Override
    // public void serialize(ConflictResolvedLoggedEvent value, JsonGenerator
    // gen, SerializerProvider serializers) throws IOException {
    //
    // gen.writeStartObject();
    // gen.writeStringField("aggregateKey", value.getAggregateKey());
    // gen.writeStringField("changeKey", value.getChangeKey());
    // gen.writeNumberField("winnerVersion", value.getWinnerVersion());
    // gen.writeNumberField("otherVersion", value.getOtherVersion());
    // gen.writeNumberField("winnerXid", value.getWinnerXid());
    // gen.writeNumberField("otherXid", value.getOtherXid());
    // gen.writeStringField("changeKey", value.getChangeKey());
    // gen.writeFieldName("winnerEvent");
    // gen.writeRawValue(value.getWinnerEvent());
    // gen.writeFieldName("otherEvent");
    // gen.writeRawValue(value.getOtherEvent());
    // gen.writeEndObject();
    // }
    // }
    //
    // @Service
    // class ConflictResolutionEntityJsonModule extends SimpleModule {
    // public ConflictResolutionEntityJsonModule() {
    // super();
    // this.addSerializer(ConflictResolvedLoggedEvent.class, new
    // ConflictResolutionSerializer());
    // }
    // }
}
