package com.sixthday.kafka.connect.jdbc.builder;

import com.sixthday.kafka.connect.jdbc.exception.InvalidConfigurationException;
import com.sixthday.kafka.connect.jdbc.json.JsonTableDef;
import com.sixthday.kafka.connect.jdbc.util.SinkUtil;
import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;

import java.util.Map;
import java.util.StringJoiner;

public class UpdateQueryBuilder extends QueryBuilder {

    @Override
    void validate(JsonTableDef jsonTableDef) {
        if ("".equals(jsonTableDef.getUpdateByKeys())) {
            throw new InvalidConfigurationException("Use '" + CustomJDBCSinkConfig.TABLES_PREFIX + "<table_name>" + CustomJDBCSinkConfig.MODE_UPDATE_KEYS
                    + "' to configure the unique primary/composite key or index for DML mode upsert");
        }
    }

    String getUpdateColumns(JsonTableDef jsonTableDef) {
        StringJoiner stringJoiner = new StringJoiner(COMMA + WHITESPACE, NO_WHITESPACE, NO_WHITESPACE);
        if ("".equals(jsonTableDef.getUpdateColumns())) {
            jsonTableDef.getJsonColumnDefs().forEach(jsonColumnDef -> {
                String columnName = jsonColumnDef.getColumnName();
                stringJoiner.add(DOUBLE_QUOTE + columnName + DOUBLE_QUOTE + EQUAL + AT + columnName
                        + specialTypeCasting(jsonColumnDef.getTypeName()));
            });
        } else {
            Map<String, String> columnMappings = SinkUtil.stringToMap(jsonTableDef.getUpdateColumns());
            jsonTableDef.getJsonColumnDefs().forEach(jsonColumnDef -> {
                if (columnMappings.containsKey(jsonColumnDef.getColumnName())) {
                    String columnName = jsonColumnDef.getColumnName();
                    stringJoiner.add(DOUBLE_QUOTE + columnName + DOUBLE_QUOTE + EQUAL + AT + columnMappings.getOrDefault(columnName, columnName)
                            + specialTypeCasting(jsonColumnDef.getTypeName()));
                }
            });
        }
        return stringJoiner.toString();
    }

    String updateByKeys(String keys) {
        StringJoiner stringJoiner = new StringJoiner(DOUBLE_QUOTE + COMMA + WHITESPACE + DOUBLE_QUOTE,
                NO_WHITESPACE + DOUBLE_QUOTE,
                NO_WHITESPACE + DOUBLE_QUOTE);
        SinkUtil.stringToList(keys).forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    @Override
    String build(JsonTableDef jsonTableDef) {
        return "INSERT INTO " + DOUBLE_QUOTE +
                jsonTableDef.getSchemaName() + DOUBLE_QUOTE +
                "." + DOUBLE_QUOTE +
                jsonTableDef.getTableName() + DOUBLE_QUOTE +
                "(" +
                getColumns(jsonTableDef) +
                ")" +
                " VALUES(" +
                getValues(jsonTableDef) +
                ")" +
                " ON CONFLICT " +
                "(" +
                updateByKeys(jsonTableDef.getUpdateByKeys()) +
                ")" +
                " DO UPDATE SET " +
                getUpdateColumns(jsonTableDef) +
                ((jsonTableDef.getUpdateClause().length() > 0) ?
                        " WHERE " + jsonTableDef.getUpdateClause()
                        : "");
    }
}