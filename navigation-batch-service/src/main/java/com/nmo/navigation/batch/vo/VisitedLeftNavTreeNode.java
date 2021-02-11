package com.sixthday.navigation.batch.vo;

import lombok.Getter;

import java.util.Set;

@Getter
public class VisitedLeftNavTreeNode {

    private Set<String> visitedPaths;
    private LeftNavTreeNode leftNavTreeNode;

    public VisitedLeftNavTreeNode(Set<String> visitedPaths, LeftNavTreeNode leftNavTreeNode) {
        this.visitedPaths = visitedPaths;
        this.leftNavTreeNode = leftNavTreeNode;

    }
}
