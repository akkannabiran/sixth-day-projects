package com.sixthday.kafka.connect.jdbc.converter;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import com.sixthday.kafka.connect.jdbc.exception.ValidationException;
import com.jayway.jsonpath.Configuration;

public class StringValueConverter implements ValueConverter<String> {

    @Override
    public String validateAndConvert(Object value, DBColumnDef dbColumnDef) throws ValidationException {
        String strVal = value.toString();
        if (("varchar".equals(dbColumnDef.getTypeName())
                || "name".equals(dbColumnDef.getTypeName())
                || "text".equals(dbColumnDef.getTypeName()))
                && strVal.length() < dbColumnDef.getMaxLength()) {
            return strVal;
        } else if ("jsonb".equals(dbColumnDef.getTypeName()) && strVal.length() < dbColumnDef.getMaxLength()) {
            return Configuration.defaultConfiguration().jsonProvider().toJson(value);
        }
        throw new ValidationException("Datatype '" +
                dbColumnDef.getTypeName() + "' limit crossed for columnName=" +
                dbColumnDef.getColumnName() +
                ", columnLimit=" +
                dbColumnDef.getMaxLength() +
                ", valueLimit=" +
                strVal.length() +
                ", value=" +
                strVal);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
