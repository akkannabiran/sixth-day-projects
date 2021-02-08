package com.sixthday.kafka.connect.jdbc.builder;

import com.sixthday.kafka.connect.jdbc.exception.InvalidConfigurationException;
import com.sixthday.kafka.connect.jdbc.json.JsonTableDef;

public class InsertQueryBuilder extends QueryBuilder {

    @Override
    void validate(JsonTableDef jsonTableDef) {
        jsonTableDef.getJsonColumnDefs().forEach(jsonColumnDef -> {
            if (jsonColumnDef.isInsertExcludeColumn() && !jsonColumnDef.isAutoIncrement() && !jsonColumnDef.isNullable()) {
                throw new InvalidConfigurationException("The column '" + jsonColumnDef.getColumnName() + "' is configured " +
                        "to exclude for insert. However, the column doesn't accept null values & no auto increment set");
            }
        });
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
                ")";
    }
}