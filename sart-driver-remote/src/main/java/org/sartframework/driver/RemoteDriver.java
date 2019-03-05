package org.sartframework.driver;

public interface RemoteDriver {

    RemoteDriver registerTransactionApi(RemoteApi api);

    RemoteDriver registerProjectionApi(RemoteApi api);

    TransactionDriver registerCommandApi(RemoteApi api);

}