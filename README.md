# remote-partitioning-kafka
a sample spring batch application with remote partitioning with Kafka as the middleware

since Kafka is used as the middleware, you need to have Kafka running. for development purpose you can use docker-compose below
```
version: '2'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - 22181:2181
  
  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - 29092:29092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

Also you need to have MySQL up and running, you can use docker:
```
docker container run -d -p 23306:3306 --name mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=test mysql:8
```

let's say we want to have one manager node with three workers, we need to create Kafka topics as below:
```
kafka-topics --bootstrap-server 127.0.0.1:29092 --topic requestForWorkers --create --partitions 3 --replication-factor 1
kafka-topics --bootstrap-server 127.0.0.1:29092 --topic repliesFromWorkers --create --partitions 1 --replication-factor 1
```

now we need to run one manager node and three workers. the only difference is the environment properties, you can checkout the `main` branch for running manager node and `worker` branch for worker nodes


as soon as you start the manager, database schema will be initialized with Customer table. you can run a Job that simply connects to this table, reads the customer based on the given ID range, and simply prints them in the console.
for running a Job you need to make a REST call to the manager node:

```
curl --location --request POST 'localhost:28080/job?minId=10&maxId=28' \
--header 'Content-Type: application/json' \
--data-raw ''
```
`minId` and `maxId` define the ID range
