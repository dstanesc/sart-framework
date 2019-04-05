package org.sartframework.driver;

import org.sartframework.command.DomainCommand;

public interface RestCommandApi extends RestApi {

    void registerCommandSupport(Class<? extends DomainCommand> commandType, RequestMapping requestMapping);

    boolean hasCommandSupport(Class<? extends DomainCommand> commandType);

    RequestMapping getCommandSupportApiUrl(Class<? extends DomainCommand> commandType);
}
