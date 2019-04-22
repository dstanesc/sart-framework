package org.sartframework.driver;

import java.util.HashMap;
import java.util.Map;

import org.sartframework.command.DomainCommand;
import org.sartframework.query.DomainQuery;

public abstract class RestRemoteApi implements RestReadWriteApi {

    String sid;

    String serverName = "localhost";

    Integer serverPort = 8083;

    Map<Class<? extends DomainQuery>, RequestMapping> supportedQueries = new HashMap<>();

    Map<Class<? extends DomainCommand>, RequestMapping> supportedCommands = new HashMap<>();


    public String getSid() {
        return sid;
    }

    public RestRemoteApi setSid(String sid) {
        this.sid = sid;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public RestRemoteApi setServerName(String name) {
        this.serverName = name;
        return this;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public RestRemoteApi setServerPort(Integer port) {
        this.serverPort = port;
        return this;
    }

    @Override
    public String toUrl() {

        return "http://" + getServerName() + ":" + getServerPort();
    }

    @Override
    public void registerQuerySupport(Class<? extends DomainQuery> queryType, RequestMapping requestMapping) {
        supportedQueries.put(queryType, requestMapping);
    }

    @Override
    public boolean hasQuerySupport(Class<? extends DomainQuery> queryType) {
        return supportedQueries.containsKey(queryType);
    }

    @Override
    public RequestMapping getQuerySupportApiUrl(Class<? extends DomainQuery> queryType) {
        return supportedQueries.get(queryType);
    }

    @Override
    public void registerCommandSupport(Class<? extends DomainCommand> commandType, RequestMapping requestMapping) {
        supportedCommands.put(commandType, requestMapping);
    }

    @Override
    public boolean hasCommandSupport(Class<? extends DomainCommand> commandType) {
        return supportedCommands.containsKey(commandType);
    }

    @Override
    public RequestMapping getCommandSupportApiUrl(Class<? extends DomainCommand> commandType) {
        return supportedCommands.get(commandType);
    }

}