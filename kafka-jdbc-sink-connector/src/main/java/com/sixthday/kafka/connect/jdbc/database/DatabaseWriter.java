package com.sixthday.kafka.connect.jdbc.database;

import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;
import com.sixthday.kafka.connect.jdbc.error.handler.ErrorRecordHandler;
import com.sixthday.kafka.connect.jdbc.error.model.BufferedRecord;
import com.sixthday.kafka.connect.jdbc.json.JsonTablesDef;
import com.sixthday.kafka.connect.jdbc.task.CustomJDBCSinkTask;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DatabaseWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomJDBCSinkTask.class);

    private final DatabaseDialect databaseDialect;
    private final JsonTablesDef jsonTablesDef;
    private final ErrorRecordHandler errorRecordHandler;

    public DatabaseWriter(CustomJDBCSinkConfig customJdbcSinkConfig, Map<String, DBTableDef> dbTableDefs, DatabaseDialect databaseDialect) {
        this.databaseDialect = databaseDialect;
        this.jsonTablesDef = new JsonTablesDef().build(customJdbcSinkConfig, dbTableDefs);
        this.errorRecordHandler = new ErrorRecordHandler(customJdbcSinkConfig);
    }

    public void writeBatches(Collection<List<SinkRecord>> sinkRecordSlices) {
        sinkRecordSlices.forEach(this::writeBatch);
    }

    private void writeBatch(List<SinkRecord> sinkRecordsSlice) {
        LOGGER.info("Initializing batch processing!");
        List<BufferedRecord> bufferedRecords = new LinkedList<>();
        sinkRecordsSlice.forEach(sinkRecord -> {
            BufferedRecord bufferedRecord = BufferedRecord.builder().sinkRecord(sinkRecord).build();
            bufferedRecords.add(bufferedRecord);
            jsonTablesDef.process(bufferedRecord, databaseDialect);
        });
        try {
            jsonTablesDef.executeBatch();
            databaseDialect.getConnection().commit();
            jsonTablesDef.close();
        } catch (Exception e) {
            try {
                databaseDialect.getConnection().rollback();
            } catch (SQLException e1) {
                LOGGER.error("Rollback failed {}", e1.getMessage());
            }
            jsonTablesDef.close();
            fallBackWrite(bufferedRecords);
            return;
        } finally {
            bufferedRecords.parallelStream().filter(BufferedRecord::isErrorProne).forEach(bufferedRecord ->
                    errorRecordHandler.handleErrorSinkRecord(bufferedRecord.getSinkRecord(), bufferedRecord.getException()));
            LOGGER.info("Batch processing has been completed successfully!");
        }
    }

    private void fallBackWrite(List<BufferedRecord> bufferedRecords) {
        LOGGER.error("Batch processing has been failed due to invalid record(s). Trying individual message processing!");
        bufferedRecords.forEach(bufferedRecord -> {
            if (!bufferedRecord.isErrorProne()) {
                try {
                    jsonTablesDef.process(bufferedRecord, databaseDialect);
                    jsonTablesDef.executeBatch();
                    databaseDialect.getConnection().commit();
                } catch (Exception e) {
                    bufferedRecord.setErrorProne(true);
                    bufferedRecord.setException(e);
                    try {
                        databaseDialect.getConnection().rollback();
                    } catch (SQLException e1) {
                        LOGGER.error("Rollback failed {}", e1.getMessage());
                    }
                    jsonTablesDef.close();
                }
            }
        });
        jsonTablesDef.close();
        LOGGER.info("Individual message processing has been completed successfully!");
    }
}
