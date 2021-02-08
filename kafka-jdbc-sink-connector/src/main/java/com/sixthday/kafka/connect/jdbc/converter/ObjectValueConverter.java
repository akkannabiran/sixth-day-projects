package com.sixthday.kafka.connect.jdbc.converter;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import com.sixthday.kafka.connect.jdbc.database.PostgreSqlDatabaseDialect;
import com.sixthday.kafka.connect.jdbc.exception.ValidationException;
import com.jayway.jsonpath.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectValueConverter implements ValueConverter<Object> {
    private final Logger LOGGER = LoggerFactory.getLogger(PostgreSqlDatabaseDialect.class);

    @Override
    public Object validateAndConvert(Object value, DBColumnDef dbColumnDef) throws ValidationException {
        if ("bit".equals(dbColumnDef.getTypeName()) && value.toString().length() > dbColumnDef.getMaxLength()) {
            LOGGER.warn("Datatype '" + dbColumnDef.getTypeName() + "' limit crossed for columnName=" +
                    dbColumnDef.getColumnName() +
                    ", columnLimit=" +
                    dbColumnDef.getMaxLength() +
                    ", value=" + value.toString() + ". Continuing to update/insert");
            return value;
        } else if ("json".equals(dbColumnDef.getTypeName())) {
            if (value.toString().length() > dbColumnDef.getMaxLength())
                throw new ValidationException("Datatype '" + dbColumnDef.getTypeName() + "' limit crossed for columnName=" +
                        dbColumnDef.getColumnName() +
                        ", columnLimit=" +
                        dbColumnDef.getMaxLength() +
                        ", value=" + value.toString() + ". Continuing to update/insert");
            return Configuration.defaultConfiguration().jsonProvider().toJson(value);
        }
        return null;
    }
}
