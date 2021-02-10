# Kafka JDBC Sink Connector

## Purpose

This is a custom sink connector developed using Apache Connect API framework to support JSON Schemaless payload.

This connector supports below DML operations

* INSERT
* UPSERT

& data types,

* INT2
* INT8
* BIGSERIAL
* VARCHAR
* BIT
* BOOL
* DATE
* FLOAT8
* FLOAT4
* INT4
* JSON
* JSONB
* NAME
* NUMERIC
* SERIAL
* SMALLSERIAL
* TEXT
* TIMETZ
* TIME
* TIMESTAMPTZ
* TIMESTAMP

## Configuration details

Name of the connector, unique value within the cluster.

```
"name": "<sample-connector>"
```

Error handling behaviour.

```
"errors.deadletterqueue.context.headers.enable": <true/false>,
"errors.deadletterqueue.topic.name": "<dlt-topic-name>",
"errors.deadletterqueue.topic.replication.factor": <1>,
"errors.log.enable": <true/false>,
"errors.log.include.messages": <true/false>,
"errors.retry.delay.max.ms": <60000>,
"errors.retry.timeout": <0>,
"errors.tolerance": "<none/all>",
```

Database transaction commit batch size.

```
"batch.size": <1000>,
```

Connector class name

```
"connector.class": "com.sixthday.kafka.connect.jdbc.CustomJDBCSinkConnector",
```

Database configurations

```
"database.catalog": "<database-name>",
"database.password": "<database-password>",
"database.port": <5432>,
"database.schema": "<database-schema-name>",
"database.host": "<database-host>",
"database.username": "<database-password>",
```

Schema configuration for key

```
"key.converter.schemas.enable": false,
"key.converter": "org.apache.kafka.connect.json.JsonConverter",
```

Database login timeout

```
"login.timeout.seconds": 15,
```

Database retries

```
"max.retries": 5,
```

Kafka producer configurations

```
"producer.bootstrap.servers": "localhost:9092",
"producer.key.serializer": "org.apache.kafka.common.serialization.StringSerializer",
"producer.value.serializer": "com.sixthday.kafka.connect.jdbc.serialization.JsonSerialization",
```

Database retry backoff time

```
"retry.backoff.ms": 10000,
```

Table(s) details

```
"tables.subgroup.mode.update.columns": "street_address, county=district",
"tables.subgroup.mode.update.clause": "employee.age > 18",
"tables.subgroup.columns.mapping": "family_name=$.last_name",
"tables.subgroup.mode.update.keys": "id",
"tables.subgroup.mode.insert.exclude.columns": "password",
"tables.subgroup.mode": "upsert",
"tables.subgroup.payload": "$",
"tables": "employee",
```

Number Of task(s)

```
"tasks.max": 1,
```

Kafka topic(s)

```
"topics": "sink_topic",
```

Transformation

```
"transforms": "timestamp",
"transforms.timestamp.field": "mydate",
"transforms.timestamp.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
"transforms.timestamp.format": "yyyy-MM-dd",
"transforms.timestamp.target.type": "Date",
```

Schema configuration for value

```
"value.converter.schemas.enable": false,
"value.converter": "org.apache.kafka.connect.json.JsonConverter"
```

## Configuration Reference

src/main/resources/jdbc-sink-connector.json
