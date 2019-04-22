## Quick Start

#### Install Kafka

```bash
mkdir ~/kafka && cd ~/kafka

curl "http://www.apache.org/dist/kafka/2.2.0/kafka_2.11-2.2.0.tgz" -o ~/kafka/kafka.tgz

tar xvzf ~/kafka/kafka.tgz --strip 1
```

#### Start Kafka
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```

```bash
bin/kafka-server-start.sh config/server.properties
```

#### Install SART
```bash
mkdir ~/sart && cd ~/sart

git clone https://github.com/dstanesc/sart-framework.git
```

##### SART CAE Services

```bash
./gradlew :sart-cae-transaction:bootRun

```

```bash
./gradlew :sart-cae-proj:bootRun

```

```bash
./gradlew :sart-cae-conflict:bootRun
```

##### SART Monitor Dashboard

```bash
http://localhost:8083/sart.html
```

##### SART Tests
```bash
./gradlew :sart-cae-transaction:test --tests org.sartframework.demo.cae.ValidationSuite  
```

## More Documentation

A paper on SART framework motivation and architecture can be found in the [doc/paper](doc/paper/sart-paper.md) folder.

A step-by-step tutorial on designing SART applications is available in the [doc/guide](doc/guide/sart-application-guide.md) folder.

