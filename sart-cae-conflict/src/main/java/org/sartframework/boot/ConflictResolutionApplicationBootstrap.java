package org.sartframework.boot;


import org.sartframework.projection.kafka.services.TransactionProjectionManagementService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({"org.sartframework"})
@EnableJpaRepositories({"org.sartframework"})
@EntityScan({"org.sartframework"})
@PropertySource(value = { "classpath:sart-kafka-config.properties","classpath:sart-config.properties" })
public class ConflictResolutionApplicationBootstrap implements DisposableBean {

    
    @Autowired
    TransactionProjectionManagementService<?> transactionProjectionManagementService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ConflictResolutionApplicationBootstrap.class, args);
        ConflictResolutionApplicationBootstrap boostrap = context.getBean(ConflictResolutionApplicationBootstrap.class);
        boostrap.start();
    }

    public void start() {
        transactionProjectionManagementService.start();
    }

    @Override
    public void destroy() throws Exception {
        transactionProjectionManagementService.stop();
    }
}
