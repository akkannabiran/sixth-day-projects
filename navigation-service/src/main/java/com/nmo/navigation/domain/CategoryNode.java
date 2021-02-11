package com.sixthday.navigation.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CategoryNode {
    private String catmanId;
    private String id;
    private int level;
    private String name;
    private String url;
    private Map<String, Object> attributes;
    private List<CategoryNode> categories;
}
