package com.sixthday.kafka.connect.jdbc.database;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DBColumnDef {
    private boolean nullable;
    private boolean autoIncrement;
    private int dataType;
    private int maxLength;
    private String typeName;
    private String columnName;
}
