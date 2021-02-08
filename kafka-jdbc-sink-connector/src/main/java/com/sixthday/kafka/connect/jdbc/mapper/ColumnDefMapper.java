package com.sixthday.kafka.connect.jdbc.mapper;

import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;
import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import com.sixthday.kafka.connect.jdbc.json.JsonColumnDef;
import com.sixthday.kafka.connect.jdbc.util.SinkUtil;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class ColumnDefMapper {

    protected JsonColumnDef map(CustomJDBCSinkConfig customJdbcSinkConfig, DBColumnDef dbColumnDef, String tableName) {
        JsonColumnDef jsonColumnDef = new JsonColumnDef();
        jsonColumnDef.setInsertExcludeColumn(insertExclude(customJdbcSinkConfig, tableName, dbColumnDef.getColumnName()));
        jsonColumnDef.setJsonElementName(columnMappings(customJdbcSinkConfig, dbColumnDef, tableName));
        jsonColumnDef.setColumnName(dbColumnDef.getColumnName());
        jsonColumnDef.setDataType(dbColumnDef.getDataType());
        jsonColumnDef.setMaxLength(dbColumnDef.getMaxLength());
        jsonColumnDef.setNullable(dbColumnDef.isNullable());
        jsonColumnDef.setTypeName(dbColumnDef.getTypeName());
        jsonColumnDef.setAutoIncrement(dbColumnDef.isAutoIncrement());

        return jsonColumnDef;
    }

    private boolean insertExclude(CustomJDBCSinkConfig customJdbcSinkConfig, String tableName, String columnName) {
        return SinkUtil.stringToList(customJdbcSinkConfig.insertExcludeColumn(tableName))
                .contains(columnName);
    }

    private String columnMappings(CustomJDBCSinkConfig customJdbcSinkConfig, DBColumnDef dbColumnDef, String tableName) {
        Map<String, String> columnMappings = SinkUtil.stringToMap(customJdbcSinkConfig.columnMapping(tableName));
        return columnMappings.get(dbColumnDef.getColumnName()) != null ?
                columnMappings.get(dbColumnDef.getColumnName()) : dbColumnDef.getColumnName();
    }
}
