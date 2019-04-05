package org.sartframework.driver;

import org.sartframework.projection.ProjectionConfiguration;
import org.sartframework.query.DomainQuery;

public interface TopicQueryApi {

    void registerQuerySupport(Class<? extends DomainQuery> queryType, ProjectionConfiguration projectionConfiguration);

    boolean hasQuerySupport(Class<? extends DomainQuery> queryType);

    ProjectionConfiguration getQuerySupportProjection(Class<? extends DomainQuery> queryType);

}