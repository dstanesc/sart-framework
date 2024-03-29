package org.sartframework.driver;

import java.util.function.Consumer;

import org.sartframework.query.DomainQuery;
import org.sartframework.session.SystemSnapshot;
import org.sartframework.transaction.AbstractDetail;
import org.sartframework.transaction.DetailFactory;

public interface RestTransactionDriver extends SiteTransactionDriver {
    
    @Override
    RestTransactionDriver init();

    RestTransactionDriver registerTransactionApi(RestTransactionApi api);

    RestTransactionDriver registerQueryApi(RestQueryApi api);
    
    RestTransactionDriver registerCommandApi(RestCommandApi api);
    
    <T extends AbstractDetail> RestTransactionDriver registerDetailFactory(DetailFactory<T>  detailFactory);

    <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer, Runnable onComplete, RestQueryApi queryApi);

}