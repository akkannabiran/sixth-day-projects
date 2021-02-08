package com.sixthday.kafka.connect.jdbc.builder;

import com.sixthday.kafka.connect.jdbc.exception.InvalidConfigurationException;
import com.sixthday.kafka.connect.jdbc.json.JsonTableDef;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public abstract class QueryBuilder {
    public static final String WHITESPACE = " ";
    public static final String NO_WHITESPACE = "";
    public static final String COMMA = ",";
    public static final String AT = "@";
    public static final String EQUAL = "=";
    public static final String DOUBLE_QUOTE = "\"";

    public static Map<String, String> TYPE_CASTS = new HashMap<>();

    static {
        TYPE_CASTS.put("json", "::json");
        TYPE_CASTS.put("jsonb", "::jsonb");
        TYPE_CASTS.put("bit", "::bit");
        TYPE_CASTS.put("name", "::name");
        TYPE_CASTS.put("text", "::text");
    }

    public static String query(JsonTableDef jsonTableDef) {
        if ("upsert".equals(jsonTableDef.getMode())) {
            UpdateQueryBuilder updateQueryBuilder = new UpdateQueryBuilder();
            updateQueryBuilder.validate(jsonTableDef);
            return updateQueryBuilder.build(jsonTableDef);
        } else if ("insert".equals(jsonTableDef.getMode())) {
            InsertQueryBuilder insertQueryBuilder = new InsertQueryBuilder();
            insertQueryBuilder.validate(jsonTableDef);
            return insertQueryBuilder.build(jsonTableDef);
        }
        throw new InvalidConfigurationException("Invalid DML operation mode");
    }

    abstract void validate(JsonTableDef jsonTableDef);

    abstract String build(JsonTableDef jsonTableDef);

    String specialTypeCasting(String typeName) {
        String output = TYPE_CASTS.get(typeName);
        return output != null ? output : "";
    }

    String getColumns(JsonTableDef jsonTableDef) {
        StringJoiner stringJoiner = new StringJoiner(DOUBLE_QUOTE + COMMA + WHITESPACE + DOUBLE_QUOTE,
                NO_WHITESPACE + DOUBLE_QUOTE,
                NO_WHITESPACE + DOUBLE_QUOTE);
        jsonTableDef.getJsonColumnDefs().forEach(jsonColumnDef -> {
            if (!jsonColumnDef.isInsertExcludeColumn()) {
                stringJoiner.add(jsonColumnDef.getColumnName());
            }
        });
        return stringJoiner.toString();
    }

    String getValues(JsonTableDef jsonTableDef) {
        StringJoiner stringJoiner = new StringJoiner(COMMA + WHITESPACE + AT, AT, NO_WHITESPACE);
        jsonTableDef.getJsonColumnDefs().forEach(jsonColumnDef -> {
            if (!jsonColumnDef.isInsertExcludeColumn()) {
                stringJoiner.add(jsonColumnDef.getColumnName()
                        + specialTypeCasting(jsonColumnDef.getTypeName()));
            }
        });
        return stringJoiner.toString();
    }
}