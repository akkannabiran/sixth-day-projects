package com.sixthday.kafka.connect.jdbc.json;

import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;
import com.sixthday.kafka.connect.jdbc.database.DBTableDef;
import com.sixthday.kafka.connect.jdbc.database.DatabaseDialect;
import com.sixthday.kafka.connect.jdbc.database.TableWriter;
import com.sixthday.kafka.connect.jdbc.error.model.BufferedRecord;
import com.sixthday.kafka.connect.jdbc.mapper.TableDefMapper;
import com.sixthday.kafka.connect.jdbc.task.CustomJDBCSinkTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonTablesDef {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomJDBCSinkTask.class);

    @Getter
    private final AtomicInteger validSinkRecord = new AtomicInteger();
    private final List<JsonTableDef> jsonTableDefs = new LinkedList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TableWriter tableWriter = new TableWriter();

    public JsonTablesDef build(CustomJDBCSinkConfig customJdbcSinkConfig, Map<String, DBTableDef> dbTableDefs) {
        dbTableDefs.forEach((tableName, dbTableDef) ->
                jsonTableDefs.add(TableDefMapper.map(customJdbcSinkConfig, dbTableDef)));
        return this;
    }

    public void process(BufferedRecord bufferedRecord, DatabaseDialect databaseDialect) {
        try {
            Object object = Configuration.defaultConfiguration()
                    .jsonProvider().parse(objectMapper.writeValueAsString(bufferedRecord.getSinkRecord().value()));
            for (JsonTableDef jsonTableDef : jsonTableDefs) {
                if (jsonTableDef.getPreparedStatementBinder() == null
                        || jsonTableDef.getPreparedStatementBinder().getPreparedStatement() == null
                        || databaseDialect.getConnection() == null) {
                    jsonTableDef.setPreparedStatementBinder(
                            databaseDialect.createPreparedStatement(databaseDialect.getConnection(), jsonTableDef.getQuery()));
                }
                tableWriter.process(object, jsonTableDef);
            }
            addBatch();
        } catch (Exception e) {
            bufferedRecord.setErrorProne(true);
            bufferedRecord.setException(e);
            clearParameters();
        }
    }

    private void addBatch() throws SQLException {
        for (JsonTableDef jsonTableDef : jsonTableDefs) {
            tableWriter.addBatch(jsonTableDef);
        }
        validSinkRecord.incrementAndGet();
        clearParameters();
    }

    private void clearParameters() {
        for (JsonTableDef jsonTableDef : jsonTableDefs) {
            try {
                tableWriter.clearParameters(jsonTableDef);
            } catch (SQLException e) {
                LOGGER.error("Unable to clear prepared statement parameters {}", e.getMessage());
            }
        }
    }

    public void executeBatch() throws SQLException {
        for (JsonTableDef jsonTableDef : jsonTableDefs) {
            tableWriter.executeBatch(jsonTableDef, validSinkRecord);
        }
        clearParameters();
    }

    public void close() {
        for (JsonTableDef jsonTableDef : jsonTableDefs) {
            jsonTableDef.getPreparedStatementBinder().close();
        }
    }
}

