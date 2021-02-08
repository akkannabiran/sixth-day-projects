package com.sixthday.kafka.connect.jdbc.json;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JsonColumnDef extends DBColumnDef {
    private boolean insertExcludeColumn;
    private String jsonElementName;
}
