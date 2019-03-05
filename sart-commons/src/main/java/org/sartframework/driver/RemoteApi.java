package org.sartframework.driver;

import java.util.HashMap;
import java.util.Map;

import org.sartframework.command.DomainCommand;
import org.sartframework.query.DomainQuery;

public abstract class RemoteApi {

    String name = "localhost";

    Integer port = 8080;
    
    Map<Class<? extends DomainQuery>, RequestMapping> supportedQueries = new HashMap<>();
    
    Map<Class<? extends DomainCommand>, RequestMapping> supportedCommands = new HashMap<>();

    public String getName() {
        return name;
    }

    public RemoteApi setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public RemoteApi setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String toUrl() {

        return "http://" + getName() + ":" + getPort();
    }
    
    public void registerQuerySupport(Class<? extends DomainQuery> queryType, RequestMapping requestMapping) {
        supportedQueries.put(queryType, requestMapping);
    }
    
    public boolean hasQuerySupport(Class<? extends DomainQuery> queryType) {
        return supportedQueries.containsKey(queryType);
    }
    
    public RequestMapping getQuerySupportApiUrl(Class<? extends DomainQuery> queryType) {
        return supportedQueries.get(queryType);
    }
    
    
    public void registerCommandSupport(Class<? extends DomainCommand> commandType, RequestMapping requestMapping) {
        supportedCommands.put(commandType, requestMapping);
    }
    
    public boolean hasCommandSupport(Class<? extends DomainCommand> commandType) {
        return supportedCommands.containsKey(commandType);
    }
    
    public RequestMapping getCommandSupportApiUrl(Class<? extends DomainCommand> commandType) {
        return supportedCommands.get(commandType);
    }
    
}