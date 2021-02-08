package com.sixthday.kafka.connect.jdbc.database;

import com.sixthday.kafka.connect.jdbc.converter.ValueConverter;
import com.sixthday.kafka.connect.jdbc.exception.NotImplementedException;
import com.sixthday.kafka.connect.jdbc.exception.ValidationException;
import com.sixthday.kafka.connect.jdbc.json.JsonColumnDef;
import com.sixthday.kafka.connect.jdbc.json.JsonTableDef;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class TableWriter {
    private final Logger LOGGER = LoggerFactory.getLogger(TableWriter.class);

    public void process(Object object, JsonTableDef jsonTableDef) throws SQLException {
        Object tablePayload = JsonPath.read(object, jsonTableDef.getPayload());
        for (JsonColumnDef columnValueProcessor : jsonTableDef.getJsonColumnDefs()) {
            process(tablePayload, columnValueProcessor, jsonTableDef.getPreparedStatementBinder());
        }
    }

    public void process(Object tablePayload, JsonColumnDef jsonColumnDef, PreparedStatementBinder preparedStatementBinder) throws SQLException, ValidationException, NotImplementedException {
        Object output = read(tablePayload, jsonColumnDef.getJsonElementName());
        switch (jsonColumnDef.getTypeName()) {
            case "int2":
            case "int4":
            case "serial":
            case "smallserial":
                preparedStatementBinder.bindInt(jsonColumnDef.getColumnName(),
                        ValueConverter.INT.convert(output, jsonColumnDef));
                break;
            case "int8":
            case "bigserial":
                preparedStatementBinder.bindLong(jsonColumnDef.getColumnName(),
                        ValueConverter.LONG.convert(output, jsonColumnDef));
                break;
            case "varchar":
            case "jsonb":
            case "name":
            case "text":
                preparedStatementBinder.bindString(jsonColumnDef.getColumnName(),
                        ValueConverter.STRING.convert(output, jsonColumnDef));
                break;
            case "bit":
            case "json":
                preparedStatementBinder.bindObject(jsonColumnDef.getColumnName(),
                        ValueConverter.OBJECT.convert(output, jsonColumnDef));
                break;
            case "bool":
                preparedStatementBinder.bindBoolean(jsonColumnDef.getColumnName(),
                        ValueConverter.BOOL.convert(output, jsonColumnDef));
                break;
            case "date":
                preparedStatementBinder.bindDate(jsonColumnDef.getColumnName(),
                        ValueConverter.DATE.convert(output, jsonColumnDef));
                break;
            case "float8":
                preparedStatementBinder.bindDouble(jsonColumnDef.getColumnName(),
                        ValueConverter.DOUBLE.convert(output, jsonColumnDef));
                break;
            case "numeric":
                preparedStatementBinder.bindDigDecimal(jsonColumnDef.getColumnName(),
                        ValueConverter.BIG_DECIMAL.convert(output, jsonColumnDef));
                break;
            case "float4":
                preparedStatementBinder.bindFloat(jsonColumnDef.getColumnName(),
                        ValueConverter.FLOAT.convert(output, jsonColumnDef));
                break;
            case "timetz":
            case "time":
                preparedStatementBinder.bindTime(jsonColumnDef.getColumnName(),
                        ValueConverter.TIME.convert(output, jsonColumnDef));
                break;
            case "timestamptz":
            case "timestamp":
                preparedStatementBinder.bindTimestamp(jsonColumnDef.getColumnName(),
                        ValueConverter.TIMESTAMP.convert(output, jsonColumnDef));
                break;
            default:
                throw new NotImplementedException("Datatype '" + jsonColumnDef.getTypeName() + "' is not supported right now!");
        }
    }

    private <T> T read(Object payload, String elementName) {
        try {
            return JsonPath.read(payload, elementName);
        } catch (Exception e) {
            LOGGER.debug("Unable to lookup/parse the element '{}' from payload '{}'. Exception '{}'", elementName, payload, e.getMessage());
            return null;
        }
    }

    public void addBatch(JsonTableDef value) throws SQLException {
        value.getPreparedStatementBinder().addBatch();
    }

    public void clearParameters(JsonTableDef value) throws SQLException {
        value.getPreparedStatementBinder().clearParameters();
    }

    public void executeBatch(JsonTableDef value, AtomicInteger validSinkRecord) throws SQLException {
        int[] affectedRecords = value.getPreparedStatementBinder().executeBatch();
        LOGGER.info("Table '{}' has been committed with {} record(s) and {} row(s) got affected.",
                value.getTableName(), validSinkRecord.get(), affectedRecords.length);
    }
}
