package org.sartframework.driver;

public interface LocalDriver extends TransactionDriver {

    public LocalDriver registerTransactionApi(RemoteTransactionApi transactionListener);

    public LocalDriver registerProjectionApi(QueryLocalApi projectionListener) ;

    public LocalDriver registerCommandApi(CommandLocalApi api);
}
