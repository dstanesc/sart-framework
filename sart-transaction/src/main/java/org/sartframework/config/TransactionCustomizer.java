package org.sartframework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TransactionCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    final SartConfiguration sartConfiguration;
    
    @Autowired
    public TransactionCustomizer(SartConfiguration sartConfiguration) {
        super();
        this.sartConfiguration = sartConfiguration;
    }



    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        
        factory.setPort(sartConfiguration.getTransactionListener().getPort());
    }

}
