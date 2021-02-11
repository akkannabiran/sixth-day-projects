package com.sixthday.navigation.api.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchCriteria implements Serializable {
    private static final long serialVersionUID = 5730324522553858022L;

    private SearchCriteriaOptions include;
    private SearchCriteriaOptions exclude;

    public boolean hasEmptyIncludeAndExcludeOptions() {
        return (include == null || include.hasEmptyHierarchyAndAttributes()) && (exclude == null || exclude.hasEmptyHierarchyAndAttributes());
    }
}
