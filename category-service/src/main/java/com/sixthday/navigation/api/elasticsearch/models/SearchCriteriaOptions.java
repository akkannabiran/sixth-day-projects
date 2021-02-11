package com.sixthday.navigation.api.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchCriteriaOptions implements Serializable {
    private static final long serialVersionUID = -8574874351736804899L;
    @Builder.Default
    private List<String> promotions = new ArrayList<>();
    @Builder.Default
    private List<Map<String, String>> hierarchy = new ArrayList<>();
    @Builder.Default
    private List<Map<String, List<String>>> attributes = new ArrayList<>();

    public boolean hasEmptyHierarchyAndAttributes() {
        return hierarchy.isEmpty() && attributes.isEmpty();
    }

}
