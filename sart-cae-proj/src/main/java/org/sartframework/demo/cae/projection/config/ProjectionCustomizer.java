package org.sartframework.demo.cae.projection.config;

import org.sartframework.config.SartConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class ProjectionCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    final SartConfiguration sartConfiguration;
    
    @Autowired
    public ProjectionCustomizer(SartConfiguration sartConfiguration) {
        super();
        this.sartConfiguration = sartConfiguration;
    }

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        
        factory.setPort(8082); // get it from config ?
    }

}
