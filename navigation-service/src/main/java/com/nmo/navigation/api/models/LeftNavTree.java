package com.sixthday.navigation.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sixthday.navigation.api.models.response.LeftNavNode;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LeftNavTree {
    private final String id;
    private final String name;
    private final List<LeftNavNode> leftNav;
    private final List<LeftNavNode> boutiqueLeftNav;
    private final String refreshablePath;
}
