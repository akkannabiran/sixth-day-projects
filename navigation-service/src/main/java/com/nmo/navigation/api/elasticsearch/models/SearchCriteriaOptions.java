package com.sixthday.navigation.api.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchCriteriaOptions implements Serializable {
    private static final long serialVersionUID = -8574874351736804899L;

    private List<String> promotions = new ArrayList<>();
    private List<Map<String, String>> hierarchy = new ArrayList<>();
    private List<Map<String, List<String>>> attributes = new ArrayList<>();

    public boolean hasEmptyHierarchyAndAttributes() {
        return hierarchy.isEmpty() && attributes.isEmpty();
    }
}
