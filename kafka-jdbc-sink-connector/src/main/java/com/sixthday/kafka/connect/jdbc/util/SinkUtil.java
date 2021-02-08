package com.sixthday.kafka.connect.jdbc.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class SinkUtil {
    public List<String> stringToList(String commaSeparatedString) {
        return Arrays
                .stream(commaSeparatedString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public Map<String, String> stringToMap(String commaSeparatedKeyAndValueString) {
        List<String> keyValuePairs = Arrays
                .stream(commaSeparatedKeyAndValueString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        Map<String, String> output = new HashMap<>();
        keyValuePairs.forEach(keyValuePair -> {
            String[] keyAndValue = keyValuePair.split("=");
            output.put(keyAndValue[0].trim(), (keyAndValue.length == 1) ? keyAndValue[0].trim() : keyAndValue[1].trim());
        });
        return output;
    }
}
