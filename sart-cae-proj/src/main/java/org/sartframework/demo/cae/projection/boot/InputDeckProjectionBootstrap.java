package org.sartframework.demo.cae.projection.boot;

import org.sartframework.projection.kafka.services.DomainProjectionManagementService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//https://github.com/m1l4n54v1c/meetup

@SpringBootApplication
@ComponentScan({"org.sartframework" })
@EnableJpaRepositories({"org.sartframework" })
@EntityScan({"org.sartframework" })
@PropertySource(value = { "classpath:sart-kafka-config.properties","classpath:sart-config.properties" })
public class InputDeckProjectionBootstrap implements DisposableBean {

    @Autowired
    DomainProjectionManagementService<?> domainProjectionManagementService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(InputDeckProjectionBootstrap.class, args);
        InputDeckProjectionBootstrap boostrap = context.getBean(InputDeckProjectionBootstrap.class);
        boostrap.start();
    }

    public void start() {
        domainProjectionManagementService.start();
    }

    @Override
    public void destroy() throws Exception {
        domainProjectionManagementService.stop();
    }
}
