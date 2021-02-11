package com.sixthday.navigation;

import com.sixthday.navigation.domain.CategoryNode;

import java.util.*;

public class CategoryNodeBuilder {

    private String catmanId = "DEFAULT_CATMAN_ID";
    private String id = "DEFAULT_CATEGORY_ID";
    private int level = 0;
    private String name = "DEFAULT_DISPLAY_NAME";
    private String url = "http://www.nm.com/url";
    private Map<String, Object> attributes = new HashMap<>();
    private List<CategoryNode> categories = new ArrayList<>();

    public CategoryNode build() {
        CategoryNode categoryNode = new CategoryNode();

        categoryNode.setCatmanId(catmanId);
        categoryNode.setId(id);
        categoryNode.setLevel(level);
        categoryNode.setName(name);
        categoryNode.setUrl(url);
        categoryNode.setAttributes(attributes);
        categoryNode.setCategories(categories);

        return categoryNode;
    }

    public CategoryNodeBuilder withCatmanId(String catmanId) {
        this.catmanId = catmanId;
        return this;
    }

    public CategoryNodeBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public CategoryNodeBuilder withLevel(int level) {
        this.level = level;
        return this;
    }

    public CategoryNodeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CategoryNodeBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public CategoryNodeBuilder withAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public CategoryNodeBuilder withCategories(List<CategoryNode> categories) {
        this.categories = categories;
        return this;
    }
}