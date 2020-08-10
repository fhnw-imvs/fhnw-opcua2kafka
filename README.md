# opcua2kafka
opcua2kafka is a simple Apache Kafka adapter for OPC UA Servers. 
This application registers on the specified OPC UA Nodes and sends their values on a value change as messages into a Kafka Cluster.  
Additionally, it can poll nodes in a configurable cycle to generate messages periodically. 
opcua2kafka uses Avro schemas, therefore the setup needs to include a Kafka Schema Registry.

opcua2kafka is configured through environment variables.
 
## Configuration
Following configuration properties can be configured through environment variables:

| Environment Variable           | Type                        | Default                                           | Description                                                                                                                                                   |
|--------------------------------|-----------------------------|---------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| OPCUA2KAFKA_LOG_LEVEL          | String                      | INFO                                              | The application log level.                                                                                                                                    |
| OPCUA_SERVER                   | String                      | opc.tcp://127.0.0.1:48400/ua/                     | Specifies the OPC UA server to connect.                                                                                                                       |
| OPCUA_NODES                    | Comma separated string list | <Empty List>                                      | Specifies all nodes which should be subscribed. All value changes are published to the Kafka topic.                                                           |
| OPCUA_POLLING_ENABLED          | Boolean                     | true                                              | Specifies if the polling feature is enabled. If true, then all polling node values are polled periodically and their values are published to the Kafka topic. |
| OPCUA_POLLING_CYCLE            | Number                      | 10000                                             | Polling Cycle in Milliseconds. Does not have any effect when polling is disabled.                                                                             |
| OPCUA_POLLING_NODES            | Comma separated string list | <Empty List>                                      | Specifies the polling nodes. Those nodes are also monitored for value changes. That means they do not have to be included in OPCUA_NODES.                |
| KAFKA_BOOTSTRAP_SERVERS        | Comma separated string list | localhost:9092                                    | Specifies the Kafka bootstrap servers.                                                                                                                        |
| KAFKA_SCHEMA_REGISTRY_ADDRESS  | Comma separated string list | http://localhost:8081                             | Specifies the Kafka Schema Registry address.                                                                                                                  |
| KAFKA_TOPIC_NAME               | String                      | test                                              | Kafka Topic to publish the messages to.                                                                                                                       |
| KAFKA_TOPIC_PARTITION_COUNT    | Number                      | 10                                                | If the topic does not already exists, how many partitions the newly created topic should have.                                                                |
| KAFKA_TOPIC_REPLICATION_FACTOR | Number                      | 1                                                 | If the topic does not already exists, how often the partitions should be replicated accross the Kafka brokers.                                                |
| AGGREGATE_ID                   | String                      | test                                              | Specifies the messages aggregate id.                                                                                                                          |
 
## Docker
For this project a Dockerfile is provided in the root directory.
To build the Docker image locally, first you have to build the application with `mvn install`.
Afterwards you can execute following command:
```
 docker build -t opcua2kafka:<IMAGE_TAG> .
```

Then run the container with the default configuration as follows:

```
 docker run opcua2kafka:<IMAGE_TAG>
```

Running with the above command, the default log level `INFO` is applied. If you want to use a different log level following command is suitable:

```
 docker run \
 -e OPCUA2KAFKA_LOG_LEVEL='<DESIRED_LOG_LEVEL>' \
 opcua2kafka:<IMAGE_TAG>
```

### Docker Directories
When running the application inside a container you should map following directories:

| Path                    | Description                                                                              |
|-------------------------|------------------------------------------------------------------------------------------|
| /opt/opcua2kafka/logs | Directory containing all written logs by the opcua2kafka application |

#### Log Level
The opcua2kafka log level can be configured through the environment variable called `OPCUA2KAFKA_LOG_LEVEL`.
As default the log level `INFO` is used. Supported log levels are:
* TRACE
* DEBUG
* INFO
* WARN
* ERROR 

Using IntelliJ, the log level can be set under the running configuration of the project in the field *VM options* using the `-D` format:

`-DOPCUA2KAFKA_LOG_LEVEL=<DESIRED_LOG_LEVEL>`

e.g.
`-DOPCUA2KAFKA_LOG_LEVEL=DEBUG`

### AVRO Schema
opcua2kafka uses for both `Key` and `Value` Avro schemas. 
The used schemas are placed under `src/main/resources/avro` .

# License
This project is licensed under the Apache 2.0 license, see [LICENSE](LICENSE).  