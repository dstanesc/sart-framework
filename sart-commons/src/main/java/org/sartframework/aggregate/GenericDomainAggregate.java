package org.sartframework.aggregate;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.Id;

import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.AbortTransactionCommand;
import org.sartframework.command.transaction.LogProgressCommand;
import org.sartframework.event.AggregateCreatedEvent;
import org.sartframework.event.AggregateDestructedEvent;
import org.sartframework.event.AggregateDestructionReversedEvent;
import org.sartframework.event.AggregateFieldDecrementedEvent;
import org.sartframework.event.AggregateFieldElementAddedEvent;
import org.sartframework.event.AggregateFieldElementRemoveReversedEvent;
import org.sartframework.event.AggregateFieldElementRemovedEvent;
import org.sartframework.event.AggregateFieldIncrementedEvent;
import org.sartframework.event.AggregateFieldUpdatedEvent;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.transaction.ConflictResolvedData;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class GenericDomainAggregate implements DomainAggregate {

    final static Logger LOGGER = LoggerFactory.getLogger(GenericDomainAggregate.class);

    @Id
    private String aggregateKey;

    private long aggregateVersion;

    private long xmin;

    private long xmax = XMAX_NOT_SET;

    protected SortedMap<Long, Queue<DomainEvent<? extends DomainCommand>>> eventsByVersion = new TreeMap<Long, Queue<DomainEvent<? extends DomainCommand>>>();

    protected SortedMap<Long, Queue<DomainEvent<? extends DomainCommand>>> eventsByTransaction = new TreeMap<Long, Queue<DomainEvent<? extends DomainCommand>>>();

    public GenericDomainAggregate() {
    }

    public GenericDomainAggregate(String aggregateKey, int aggregateVersion, long xmin) {
        super();
        this.aggregateKey = aggregateKey;
        this.aggregateVersion = aggregateVersion;
        this.xmin = xmin;
    }

    public void setAggregateVersion(int agregateVersion) {

        this.aggregateVersion = agregateVersion;
    }

    @Override
    public long getAggregateVersion() {

        return aggregateVersion;
    }

    public void setAggregateKey(String aggregateKey) {

        this.aggregateKey = aggregateKey;
    }

    @Override
    public String getAggregateKey() {

        return aggregateKey;
    }

    public long getXmin() {

        return xmin;
    }

    public void setXmin(long xmin) {

        this.xmin = xmin;
    }

    public long getXmax() {

        return xmax;
    }

    public void setXmax(long xmax) {

        this.xmax = xmax;
    }

    @Override
    public ChangeSet getChanges(long fromVersion, long toVersion) {

        SortedMap<Long, Queue<DomainEvent<? extends DomainCommand>>> view = this.eventsByVersion.subMap(fromVersion, toVersion + 1);

        return new GenericChangeSet(view);
    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateCreatedEvent<C> aggregateCreatedEvent, Deferrable deferrable) {

        // no conflict possible, aggregate key expected unique or violation

        setAggregateKey(aggregateCreatedEvent.getAggregateKey());

        deferrable.execute();

        return saveInternal(aggregateCreatedEvent);
    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateDestructedEvent<C> aggregateDeletedEvent, Deferrable deferrable) {

        boolean applyChanges = resolveUpdateConflicts(aggregateDeletedEvent, new ConflictResolver_LastWriterWins());

        if (applyChanges) {

            setXmax(aggregateDeletedEvent.getXid());

            deferrable.execute();

            return saveInternal(aggregateDeletedEvent);

        } else
            return getAggregateVersion();
    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateDestructionReversedEvent<C> aggregateDeleteReversedEvent, Deferrable deferrable) {

        boolean applyChanges = resolveUpdateConflicts(aggregateDeleteReversedEvent, new ConflictResolver_LastWriterWins());

        if (applyChanges) {

            setXmax(XMAX_NOT_SET);

            deferrable.execute();

            return saveInternal(aggregateDeleteReversedEvent);

        } else
            return getAggregateVersion();
    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateFieldIncrementedEvent<C> aggregateFieldIncrementedEvent, Deferrable deferrable) {

        // no conflict possible, commutative operation including decrement

        return saveInternal(aggregateFieldIncrementedEvent);
    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateFieldDecrementedEvent<C> dggregateFieldDecrementedEvent, Deferrable deferrable) {

        // no conflict possible, commutative operation including increment
        deferrable.execute();

        return saveInternal(dggregateFieldDecrementedEvent);
    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateFieldElementAddedEvent<C> aggregateFieldElementAddedEvent, Deferrable deferrable) {

        // no conflict possible, grow only set, the compensation is not removal
        // but element flagging via xmax

        deferrable.execute();

        return saveInternal(aggregateFieldElementAddedEvent);
    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateFieldElementRemovedEvent<C> aggregateFieldElementRemovedEvent, Deferrable deferrable) {

        // Conflict possible, updating xmax of the entry
        boolean applyChanges = resolveUpdateConflicts(aggregateFieldElementRemovedEvent, new ConflictResolver_LastWriterWins());

        if (applyChanges) {

            deferrable.execute();

            return saveInternal(aggregateFieldElementRemovedEvent);

        } else
            return getAggregateVersion();
    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateFieldElementRemoveReversedEvent<C> aggregateFieldElementRemoveReversedEvent,
                                                      Deferrable deferrable) {

        // Conflict possible, updating xmax of the entry
        boolean applyChanges = resolveUpdateConflicts(aggregateFieldElementRemoveReversedEvent, new ConflictResolver_LastWriterWins());

        if (applyChanges) {

            deferrable.execute();

            return saveInternal(aggregateFieldElementRemoveReversedEvent);

        } else
            return getAggregateVersion();

    }

    @Override
    public <C extends DomainCommand> Long handleEvent(AggregateFieldUpdatedEvent<C> aggregateFieldUpdatedEvent, Deferrable deferrable) {

        boolean applyChanges = resolveUpdateConflicts(aggregateFieldUpdatedEvent, new ConflictResolver_LastWriterWins());

        if (applyChanges) {

            deferrable.execute();

            return saveInternal(aggregateFieldUpdatedEvent);

        } else
            return getAggregateVersion();

    }

    private <C extends DomainCommand> boolean resolveUpdateConflicts(DomainEvent<C> domainEvent, ConflictResolver conflictResolver) {

        long originalVersion = domainEvent.getSourceAggregateVersion();

        String changeKey = domainEvent.getChangeKey();

        long currentVersion = getAggregateVersion();

        boolean applyChanges = true;

        if (originalVersion < currentVersion) {

            ChangeSet changes = getChanges(originalVersion, currentVersion);

            if (changes.containsChanges(changeKey)) {

                List<DomainEvent<? extends DomainCommand>> conflictingChanges = changes.getChanges(changeKey);

                for (DomainEvent<? extends DomainCommand> conflictingChange : conflictingChanges) {

                    Conflict conflict = new GenericConflict(domainEvent, conflictingChange);

                    boolean resolved = conflictResolver.resolve(conflict, this);

                    dispatchConflictResolvedEvent(domainEvent, changeKey, conflictingChange, resolved);

                    applyChanges &= resolved;
                }
            }
        }

        return applyChanges;
    }

    private <C extends DomainCommand> void dispatchConflictResolvedEvent(DomainEvent<C> domainEvent, String changeKey,
                                                                         DomainEvent<? extends DomainCommand> conflictingChange, boolean resolved) {
        long winnerVersion;
        long otherVersion;
        long winnerXid;
        long otherXid;
        String winnerEvent;
        String otherEvent;

        if (resolved) {
            winnerVersion = domainEvent.getSourceAggregateVersion();
            otherVersion = conflictingChange.getSourceAggregateVersion();
            winnerXid = domainEvent.getXid();
            otherXid = conflictingChange.getXid();
            winnerEvent = encodeJson(domainEvent);
            otherEvent = encodeJson(conflictingChange);
        } else {
            otherVersion = domainEvent.getSourceAggregateVersion();
            winnerVersion = conflictingChange.getSourceAggregateVersion();
            otherXid = domainEvent.getXid();
            winnerXid = conflictingChange.getXid();
            otherEvent = encodeJson(domainEvent);
            winnerEvent = encodeJson(conflictingChange);
        }

        // it should be a command ?
        ConflictResolvedData conflictResolvedEventLoser = new ConflictResolvedData(otherXid, aggregateKey, changeKey, winnerVersion, otherVersion,
            winnerXid, otherXid, winnerEvent, otherEvent);

        ConflictResolvedData conflictResolvedEventWinner = new ConflictResolvedData(winnerXid, aggregateKey, changeKey, winnerVersion, otherVersion,
            winnerXid, otherXid, winnerEvent, otherEvent);

        publish(new ConflictResolvedEvent(conflictResolvedEventLoser));
        publish(new ConflictResolvedEvent(conflictResolvedEventWinner));
    }

    private Long saveInternal(DomainEvent<?> domainEvent) {

        // domainEvent.setProcessedTime(Calendar.getInstance().getTimeInMillis());

        Queue<DomainEvent<? extends DomainCommand>> transactionEvents = this.eventsByTransaction.get(domainEvent.getXid());

        if (transactionEvents == null) {
            transactionEvents = new ArrayDeque<>();
            eventsByTransaction.put(domainEvent.getXid(), transactionEvents);
            aggregateVersion++;
        }

        transactionEvents.add(domainEvent);

        Queue<DomainEvent<? extends DomainCommand>> aggregateVersionEvents = this.eventsByVersion.get(aggregateVersion);

        if (aggregateVersionEvents == null) {
            aggregateVersionEvents = new ArrayDeque<>();
            eventsByVersion.put(aggregateVersion, aggregateVersionEvents);
        }

        aggregateVersionEvents.add(domainEvent);

        setXmin(domainEvent.getXid());

        return aggregateVersion;
    }

    protected void dispatch(DomainEvent<? extends DomainCommand> domainEvent) {

        LOGGER.info("dispatch events xid={}, xcs={}, domainEvent={}", domainEvent.getXid(), domainEvent.getXcs(), domainEvent);

        try {

            long resultingAggregateVersion = handle(domainEvent);

            domainEvent.setTargetAggregateVersion(resultingAggregateVersion);

            publish(domainEvent);

            publish(new LogProgressCommand( domainEvent.getXid(), domainEvent.getXcs(), domainEvent));

        } catch (HandlerNotFound e) {

            LOGGER.error("Handler missing in aggregate. Use @DomainEventHandler annotation on {}#methodName({} domainEvent)", e.getHandlingClass(), e.getArgumentType());
            LOGGER.error("Handler missing in aggregate.", e);
        }
    }

    protected void fail(long xid) {

        LOGGER.info("Failing TX {} ", xid);

        publish(new AbortTransactionCommand(xid));
    }

    public abstract long handle(DomainEvent<? extends DomainCommand> atomicEvent);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregateKey == null) ? 0 : aggregateKey.hashCode());
        result = prime * result + (int) (aggregateVersion ^ (aggregateVersion >>> 32));
        result = prime * result + (int) (xmax ^ (xmax >>> 32));
        result = prime * result + (int) (xmin ^ (xmin >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GenericDomainAggregate other = (GenericDomainAggregate) obj;
        if (aggregateKey == null) {
            if (other.aggregateKey != null)
                return false;
        } else if (!aggregateKey.equals(other.aggregateKey))
            return false;
        if (aggregateVersion != other.aggregateVersion)
            return false;
        if (xmax != other.xmax)
            return false;
        if (xmin != other.xmin)
            return false;
        return true;
    }

    @Override
    public String toString() {

        return encodeJson(this);
    }

    private String encodeJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            return jsonString;
        } catch (JsonProcessingException e) {
            return "Could not stringify";
        }
    }
}
