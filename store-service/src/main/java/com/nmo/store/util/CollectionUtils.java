package com.sixthday.store.util;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionUtils {
    public static List<String> disjuntion(List<String> collection1, List<String> collection2) {
        return collection1.stream()
                .filter(item -> !collection2.contains(item))
                .collect(Collectors.toList());
    }
}
