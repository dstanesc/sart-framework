package org.sartframework.driver;

import java.util.HashMap;
import java.util.Map;

import org.sartframework.projection.ProjectionConfiguration;
import org.sartframework.query.DomainQuery;

public class LocalTopicQueryApi implements TopicQueryApi {

    Map<Class<? extends DomainQuery>, ProjectionConfiguration> supportedQueries = new HashMap<>();

    
    @Override
    public void registerQuerySupport(Class<? extends DomainQuery> queryType, ProjectionConfiguration projectionConfiguration) {
        supportedQueries.put(queryType, projectionConfiguration);
    }
    
    @Override
    public boolean hasQuerySupport(Class<? extends DomainQuery> queryType) {
        return supportedQueries.containsKey(queryType);
    }
    
    @Override
    public ProjectionConfiguration getQuerySupportProjection(Class<? extends DomainQuery> queryType) {
        return supportedQueries.get(queryType);
    }
}
