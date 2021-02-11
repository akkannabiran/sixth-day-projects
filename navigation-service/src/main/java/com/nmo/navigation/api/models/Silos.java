package com.sixthday.navigation.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sixthday.navigation.domain.CategoryNode;
import lombok.Data;

import java.util.List;

@Data
public class Silos {

    @JsonProperty("silos")
    private List<CategoryNode> silosTree;
}
