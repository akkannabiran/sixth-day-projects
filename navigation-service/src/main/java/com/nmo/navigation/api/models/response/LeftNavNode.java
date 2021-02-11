package com.sixthday.navigation.api.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeftNavNode {
    private final String id;
    private final String name;
    private final String url;
    private final int level;
    private final List<LeftNavNode> categories;
    private final String path;
    private final boolean redText;
    private final boolean selected;
}