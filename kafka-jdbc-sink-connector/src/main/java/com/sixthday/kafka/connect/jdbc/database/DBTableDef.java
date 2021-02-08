package com.sixthday.kafka.connect.jdbc.database;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DBTableDef {
    private String tableName;
    private String schemaName;
    private List<DBColumnDef> DBColumnDefs;
    private PreparedStatementBinder preparedStatementBinder;
}
