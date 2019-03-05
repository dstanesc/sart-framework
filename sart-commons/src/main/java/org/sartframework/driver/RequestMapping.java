package org.sartframework.driver;

public class RequestMapping {

    final RequestMethod requestMethod;
    
    final String url;

    public RequestMapping(RequestMethod requestMethod, String url) {
        super();
        this.requestMethod = requestMethod;
        this.url = url;
    }

    public RequestMethod getMethod() {
        return requestMethod;
    }

    public String getUrl() {
        return url;
    }
}
