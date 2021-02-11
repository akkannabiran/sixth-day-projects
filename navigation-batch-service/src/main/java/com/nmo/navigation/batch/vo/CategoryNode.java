package com.sixthday.navigation.batch.vo;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryNode {
    private String catmanId;
    private String id;
    private int level;
    private String name;
    private String url;
    private Map<String, Object> attributes;
    private List<CategoryNode> categories;
}
