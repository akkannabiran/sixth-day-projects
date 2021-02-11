package com.sixthday.navigation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sixthday.navigation.batch.vo.CategoryNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Silos {

    @JsonProperty("silos")
    private List<CategoryNode> silosTree;
}
