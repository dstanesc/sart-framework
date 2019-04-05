package org.sartframework.driver;

import java.util.function.Consumer;

import org.sartframework.query.DomainQuery;
import org.sartframework.session.SystemSnapshot;

public interface TopicTransactionDriver extends TransactionDriver {

    public TopicTransactionDriver registerTransactionApi(RestTransactionApi transactionListener);

    public TopicTransactionDriver registerProjectionApi(TopicQueryApi projectionListener) ;

    public TopicTransactionDriver registerCommandApi(LocalTopicCommandApi api);
    
    <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery,
                                            Class<R> resultType, Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer,
                                            Runnable onComplete, TopicQueryApi queryInternalApi);
}
