## Quick Start


#### Install SART
```bash
git clone https://github.com/dstanesc/sart-framework.git
```

#### Start Zookeeper
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```

#### Start Kafka
```bash
bin/kafka-server-start.sh config/server.properties
```

#### Start Demo

##### Start Services

Terminal 1

```bash
./gradlew :sart-cae-transaction:bootRun

```

Terminal 2

```bash
./gradlew :sart-cae-proj:bootRun

```
Terminal 3

```bash
./gradlew :sart-cae-conflict:bootRun
```

##### Monitor Dashboard

```bash
http://localhost:8080/sart.html
```

##### Execute Tests
```bash
./gradlew :sart-cae-transaction:test --tests org.sartframework.demo.cae.ValidationSuite  
```

## Documentation

A paper on SART framework motivation and architecture can be found in the [doc/paper](doc/paper/sart-paper.md) folder.

A step-by-step tutorial on designing SART applications is available in the [doc/guide](doc/guide/sart-application-guide.md) folder.

