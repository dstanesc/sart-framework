package org.sartframework.demo.cae.boot;

import org.sartframework.kafka.channels.KafkaWriters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan({ "org.sartframework.kafka" })
@PropertySource(value = { "classpath:sart-kafka-config.properties", "classpath:sart-config.properties" })
public class SimulationClientBootstrap implements CommandLineRunner {

    @Autowired
    private KafkaWriters writeChannels;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SimulationClientBootstrap.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
        //ConfigurableApplicationContext context = SpringApplication.run(SimulationClientBootstrap.class, args);
       // SimulationClientBootstrap boostrap = context.getBean(SimulationClientBootstrap.class);
 
    }

    
    
    @Override
    public void run(String... args) throws Exception {
       new MonitorShowcase(writeChannels).testInputDeckPerformanceMonitor();
        //new ScalabilityTest(writeChannels).testPerformance();
    }
}
