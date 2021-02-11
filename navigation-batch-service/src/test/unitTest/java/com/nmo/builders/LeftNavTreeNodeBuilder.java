package com.sixthday.builders;

import com.sixthday.navigation.batch.vo.LeftNavTreeNode;

import java.util.List;

import static java.util.Collections.emptyList;

public class LeftNavTreeNodeBuilder {

    private List<LeftNavTreeNode> categories = emptyList();
    private String id = "0";
    private String name = "aLeftNavTreeNode";
    private String url = "/url";
    private String path = "0";
    private boolean redText = false;
    private boolean selected = false;
    private List<String> excludedCountries = emptyList();

    public LeftNavTreeNodeBuilder() {

    }

    public LeftNavTreeNode build() {
        return new LeftNavTreeNode(this.id, this.name, this.url, this.categories, this.path, this.redText, this.selected, this.excludedCountries);
    }

    public LeftNavTreeNodeBuilder withId(String id) {
        this.id = id;
        return this;
    }
}
