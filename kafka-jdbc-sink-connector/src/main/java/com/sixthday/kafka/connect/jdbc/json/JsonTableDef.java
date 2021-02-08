package com.sixthday.kafka.connect.jdbc.json;

import com.sixthday.kafka.connect.jdbc.database.DBTableDef;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JsonTableDef extends DBTableDef {
    private String mode;
    private String payload;
    private String updateClause;
    private String updateByKeys;
    private String updateColumns;
    private String query;
    private List<JsonColumnDef> jsonColumnDefs;
}
