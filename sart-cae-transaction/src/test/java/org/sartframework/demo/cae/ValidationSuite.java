package org.sartframework.demo.cae;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//requires following services : SimulationApplicationBootstrap, ConflictResolutionApplicationBootstrap, InputDeckProjectionBootstrap

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
    
    SerializerPolymorphTest.class, 
    TransactionTest.class, 
    ScalabilityTest.class, 
    VersionTest.class, 
    LatencyTest.class, 
    BatchScalabilityTest.class, 
    IsolationTest.class,
    DurationTest.class,
    WorkflowTest.class,
    AttachDetailsTest.class,
    DomainErrorTest.class,
    SystemFaultTest.class
})

public class ValidationSuite {}
