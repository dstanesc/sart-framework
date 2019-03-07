package org.sartframework.demo.cae;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//requires following services : SimulationApplicationBootstrap, ConflictResolutionApplicationBootstrap, InputDeckProjectionBootstrap

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
    
    SerializeTest.class, 
    TransactionTest.class, 
    ScalabilityTest.class, 
    VersionTest.class, 
    LatencyTest.class, 
    BatchScalabilityTest.class, 
    IsolationTest.class,
    DurationTest.class
})

public class ValidationSuite {}
