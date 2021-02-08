package com.sixthday.kafka.connect.jdbc.task;

import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;
import com.sixthday.kafka.connect.jdbc.database.DBTableDef;
import com.sixthday.kafka.connect.jdbc.database.DatabaseDialect;
import com.sixthday.kafka.connect.jdbc.database.DatabaseWriter;
import com.sixthday.kafka.connect.jdbc.database.PostgreSqlDatabaseDialect;
import com.sixthday.kafka.connect.jdbc.error.handler.ErrorRecordHandler;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CustomJDBCSinkTask extends SinkTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomJDBCSinkTask.class);

    private DatabaseDialect databaseDialect;
    private DatabaseWriter databaseWriter;
    private CustomJDBCSinkConfig customJdbcSinkConfig;

    @Override
    @SneakyThrows
    public void start(Map<String, String> map) {
        LOGGER.info("Starting JDBC Sink task(s).");
        map.put("task.id", "connect-producer-dlt-" + hashCode());
        customJdbcSinkConfig = new CustomJDBCSinkConfig(map);
        databaseDialect = new PostgreSqlDatabaseDialect(customJdbcSinkConfig);
        Map<String, DBTableDef> dbTableDefs = databaseDialect.tableDefinitions(customJdbcSinkConfig.getList(CustomJDBCSinkConfig.TABLES));
        databaseWriter = new DatabaseWriter(customJdbcSinkConfig, dbTableDefs, databaseDialect);
    }

    @Override
    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    @SneakyThrows
    @Override
    public void put(Collection<SinkRecord> sinkRecords) {
        if (sinkRecords == null || sinkRecords.isEmpty()) {
            return;
        }
        final SinkRecord sinkRecord = sinkRecords.iterator().next();
        LOGGER.info("Received {} record(s). First record coordinates are topic={}, partition={}, and offset={}",
                sinkRecords.size(), sinkRecord.topic(), sinkRecord.kafkaPartition(), sinkRecord.kafkaOffset());
        try {
            final AtomicInteger batchSize = new AtomicInteger();
            final Collection<List<SinkRecord>> sinkRecordSlices = sinkRecords.stream()
                    .collect(Collectors.groupingBy(it -> batchSize.getAndIncrement() / customJdbcSinkConfig.getInt(CustomJDBCSinkConfig.BATCH_SIZE)))
                    .values();

            databaseWriter.writeBatches(sinkRecordSlices);
        } catch (Exception e) {
            if (customJdbcSinkConfig.isErrorsLogEnabled())
                LOGGER.error("Exception caught while calling write(). Error '{}'", e.getMessage());
            throw new ConnectException(e);
        } finally {
            databaseDialect.close();
        }
    }

    @Override
    public void flush(Map<TopicPartition, OffsetAndMetadata> map) {
    }

    @Override
    @SneakyThrows
    public void stop() {
        databaseDialect.close();
        if (ErrorRecordHandler.kafkaProducer != null) {
            ErrorRecordHandler.kafkaProducer.flush();
            ErrorRecordHandler.kafkaProducer.close();
        }
    }
}
