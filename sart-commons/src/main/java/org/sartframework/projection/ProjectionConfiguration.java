package org.sartframework.projection;

public interface ProjectionConfiguration {

    String getEventTopic();

    String getQueryTopic();

    String getQueryResultTopic();

    String getQueryEventTopic();

    String getResultListenerServiceName();

}
