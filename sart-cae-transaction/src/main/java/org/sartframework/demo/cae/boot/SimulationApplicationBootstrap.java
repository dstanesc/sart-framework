package org.sartframework.demo.cae.boot;

import org.sartframework.transaction.generator.TransactionSequence;
import org.sartframework.transaction.kafka.services.DomainCommandService;
import org.sartframework.transaction.kafka.services.TransactionCommandService;
import org.sartframework.transaction.kafka.services.TransactionLifecycleMonitorService;
import org.sartframework.transaction.kafka.services.TransactionSessionMonitorService;
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
public class SimulationApplicationBootstrap implements DisposableBean {
    
      
    @Autowired
    private TransactionCommandService transactionCommandService;

    @Autowired
    private DomainCommandService domainCommandService;
    
    @Autowired
    private TransactionLifecycleMonitorService transactionLifecycleMonitorService;
    
    @Autowired
    private TransactionSessionMonitorService transactionSessionMonitorService;

    
    @Autowired
    private TransactionSequence transactionSequence;


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SimulationApplicationBootstrap.class, args);
        SimulationApplicationBootstrap boostrap = context.getBean(SimulationApplicationBootstrap.class);
        boostrap.start();
    }

    public void start() {
        transactionSequence.start();
        transactionCommandService.start();
        domainCommandService.start();
        transactionLifecycleMonitorService.start();
        transactionSessionMonitorService.start();
    }

    @Override
    public void destroy() throws Exception {
        transactionSequence.stop();
        transactionCommandService.stop();
        domainCommandService.stop();
        transactionLifecycleMonitorService.stop();
        transactionSessionMonitorService.stop();
    }
}
