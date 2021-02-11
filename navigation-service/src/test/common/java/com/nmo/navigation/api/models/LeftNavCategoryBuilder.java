package com.sixthday.navigation.api.models;

import com.sixthday.navigation.batch.vo.LeftNavTreeNode;

import java.util.List;

public class LeftNavCategoryBuilder {
    String name = "TEST Category";
    String url = "testurl";
    int level = 0;
    private String id = "CATEGORY_ID";
    private List<LeftNavTreeNode> categories = null;
    private String path = "path";
    private boolean redText = false;
    private boolean selected = false;

    private List<String> excludedCountries = null;

    public LeftNavTreeNode build() {
        return new LeftNavTreeNode(id, name, url, level, categories, path, redText, selected, excludedCountries);
    }

    public LeftNavCategoryBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public LeftNavCategoryBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public LeftNavCategoryBuilder withCategories(List<LeftNavTreeNode> categories) {
        this.categories = categories;
        return this;
    }

    public LeftNavCategoryBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public LeftNavCategoryBuilder withLevel(int level) {
        this.level = level;
        return this;
    }

    public LeftNavCategoryBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public LeftNavCategoryBuilder withRedText(boolean redText) {
        this.redText = redText;
        return this;
    }

    public LeftNavCategoryBuilder withSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public LeftNavCategoryBuilder withExcludedCountries(List<String> excludedCountries) {
        this.excludedCountries = excludedCountries;
        return this;
    }
}
