package com.sixthday.kafka.connect.jdbc.converter;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import com.sixthday.kafka.connect.jdbc.exception.ValidationException;

@FunctionalInterface
public interface ValueConverter<T> {
    BooleanValueConverter BOOL = new BooleanValueConverter();
    StringValueConverter STRING = new StringValueConverter();
    TimeValueConverter TIME = new TimeValueConverter();
    TimestampValueConverter TIMESTAMP = new TimestampValueConverter();
    DateValueConverter DATE = new DateValueConverter();
    ObjectValueConverter OBJECT = new ObjectValueConverter();
    NumberValueValidator.IntegerValueConverter INT = new NumberValueValidator.IntegerValueConverter();
    NumberValueValidator.LongValueConverter LONG = new NumberValueValidator.LongValueConverter();
    NumberValueValidator.DoubleValueConverter DOUBLE = new NumberValueValidator.DoubleValueConverter();
    NumberValueValidator.BigDecimalValueConverter BIG_DECIMAL = new NumberValueValidator.BigDecimalValueConverter();
    NumberValueValidator.FloatValueConverter FLOAT = new NumberValueValidator.FloatValueConverter();

    T validateAndConvert(Object value, DBColumnDef dbColumnDef) throws ValidationException;

    default T convert(Object value, DBColumnDef dbColumnDef) throws ValidationException {
        if (!dbColumnDef.isAutoIncrement() && !dbColumnDef.isNullable() && value == null) {
            throw new ValidationException("The column '" + dbColumnDef.getColumnName() + "' is mandatory. Use the configuration " +
                    "tables.<table_name>.mode.insert.exclude.columns to exclude.");
        } else if (value == null) {
            return null;
        }
        T t = validateAndConvert(value, dbColumnDef);
        if (t == null && !dbColumnDef.isAutoIncrement() && !dbColumnDef.isNullable()) {
            throw new ValidationException("The column '" + dbColumnDef.getColumnName() + "' is mandatory. The converter unable to " +
                    "convert into '" + dbColumnDef.getTypeName() + "' value '" + value + "'");
        }
        return t;
    }
}
