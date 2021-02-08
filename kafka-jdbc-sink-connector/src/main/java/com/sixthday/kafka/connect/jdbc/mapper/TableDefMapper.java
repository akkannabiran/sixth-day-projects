package com.sixthday.kafka.connect.jdbc.mapper;

import com.sixthday.kafka.connect.jdbc.builder.QueryBuilder;
import com.sixthday.kafka.connect.jdbc.config.CustomJDBCSinkConfig;
import com.sixthday.kafka.connect.jdbc.database.DBTableDef;
import com.sixthday.kafka.connect.jdbc.json.JsonColumnDef;
import com.sixthday.kafka.connect.jdbc.json.JsonTableDef;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class TableDefMapper {
    public JsonTableDef map(CustomJDBCSinkConfig customJdbcSinkConfig, DBTableDef dbTableDef) {
        List<JsonColumnDef> jsonColumnDefs = new ArrayList<>();
        dbTableDef.getDBColumnDefs().forEach(dbColumnDef ->
                jsonColumnDefs.add(ColumnDefMapper.map(customJdbcSinkConfig, dbColumnDef, dbTableDef.getTableName())));

        JsonTableDef jsonTableDef = new JsonTableDef();
        jsonTableDef.setJsonColumnDefs(jsonColumnDefs);
        jsonTableDef.setDBColumnDefs(dbTableDef.getDBColumnDefs());
        jsonTableDef.setMode(customJdbcSinkConfig.mode(dbTableDef.getTableName()));
        jsonTableDef.setPayload(customJdbcSinkConfig.payload(dbTableDef.getTableName()));
        jsonTableDef.setTableName(dbTableDef.getTableName());
        jsonTableDef.setSchemaName(dbTableDef.getSchemaName());
        jsonTableDef.setUpdateByKeys(customJdbcSinkConfig.updateKeys(dbTableDef.getTableName()));
        jsonTableDef.setUpdateClause(customJdbcSinkConfig.updateClause(dbTableDef.getTableName()));
        jsonTableDef.setUpdateColumns(customJdbcSinkConfig.updateColumns(dbTableDef.getTableName()));
        jsonTableDef.setQuery(QueryBuilder.query(jsonTableDef));
        return jsonTableDef;
    }
}
