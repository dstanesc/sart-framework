package org.sartframework.kafka.serializers.serde;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.sartframework.aggregate.DomainAggregate;
import org.sartframework.aggregate.TransactionAggregate;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.error.DomainError;
import org.sartframework.error.transaction.TransactionError;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.QueryEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.QueryResult;
import org.sartframework.session.RunningTransactions;

public class SartSerdes {

    public static <T extends DomainAggregate> Serde<T> DomainAggregateSerde() {
        return new SartGenericSerde<T>();
    }

    public static <T extends DomainCommand> Serde<T> DomainCommandSerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends DomainEvent<?>> Serde<T> DomainEventSerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends DomainError> Serde<T> DomainErrorSerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends DomainQuery> Serde<T> DomainQuerySerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends QueryResult> Serde<T> QueryResultSerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends QueryEvent> Serde<T> QueryEventSerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends TransactionAggregate> Serde<T> TransactionAggregateSerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends TransactionCommand> Serde<T> TransactionCommandSerde() {
        return new SartGenericSerde<T>();
    }

    public static <T extends TransactionEvent> Serde<T> TransactionEventSerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends TransactionError> Serde<T> TransactionErrorSerde() {
        return new SartGenericSerde<T>();
    }
    
    public static <T extends RunningTransactions> Serde<T> RunningTransactionsSerde() {
        return new SartGenericSerde<T>();
    }

    public static Serde<String> String() {
        return Serdes.String();
    }

    public static Serde<Long> Long() {
        return Serdes.Long();
    }
}
