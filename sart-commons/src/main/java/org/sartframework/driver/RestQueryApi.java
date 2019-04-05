package org.sartframework.driver;

import org.sartframework.query.DomainQuery;

public interface RestQueryApi extends RestApi {

    void registerQuerySupport(Class<? extends DomainQuery> queryType, RequestMapping requestMapping) ;
    
    boolean hasQuerySupport(Class<? extends DomainQuery> queryType) ;
    
    RequestMapping getQuerySupportApiUrl(Class<? extends DomainQuery> queryType);
}
