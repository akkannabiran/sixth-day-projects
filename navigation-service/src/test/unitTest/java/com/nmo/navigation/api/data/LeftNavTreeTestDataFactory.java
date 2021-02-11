package com.sixthday.navigation.api.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.mappers.LeftNavNodeMapper;
import com.sixthday.navigation.api.models.LeftNavCategoryBuilder;
import com.sixthday.navigation.api.models.LeftNavTree;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Collections;

public class LeftNavTreeTestDataFactory {
    public static String NAV_PATH = "cat1";

    public static String NAV_PATH_WITH_MULTIPLE_CATEGORIES = "cat1_cat2";

    public static String LEFTNAV_INDEX = "leftnav_index";

    public static String DOCUMENT_TYPE = "_doc";
    public static String LEFTNAV_REFRESHABLE_PATH = "refreshablePath";
    public static String LEFTNAV_DOCUMENT_AS_STRING = getLeftNavTreeString();

    @SneakyThrows
    public static String getLeftNavTreeString() {
        return new ObjectMapper().writeValueAsString(getTestLeftNavTree());
    }

    public static LeftNavTree getTestLeftNavTree() {
        LeftNavTreeNode leftNavTreeNode = new LeftNavCategoryBuilder().withId("cat1")
                .withName("name1")
                .withCategories(
                        Arrays.asList(new LeftNavCategoryBuilder().withId("child1").build(),
                                new LeftNavCategoryBuilder().withId("child2").build()))
                .build();
        LeftNavNodeMapper leftNavNodeMapper = new LeftNavNodeMapper();

        LeftNavTree leftNavTree = new LeftNavTree("cat1", "name1", Arrays.asList(leftNavNodeMapper.map(leftNavTreeNode)), Collections.emptyList(), LEFTNAV_REFRESHABLE_PATH);
        return leftNavTree;
    }

    public static LeftNavDocument getTestLeftNavDocument() {
        LeftNavDocument leftNavDoc = new LeftNavDocument("cat1", "name1", "cat1_child1",
                Arrays.asList(new LeftNavCategoryBuilder().withId("cat1")
                        .withName("name1")
                        .withCategories(
                                Arrays.asList(new LeftNavCategoryBuilder().withId("child1").build(),
                                        new LeftNavCategoryBuilder().withId("child2").build()))
                        .build()), null, LEFTNAV_REFRESHABLE_PATH);
        return leftNavDoc;
    }

    public static LeftNavDocument getLeftNavDocumentWithDriveTo() {
        LeftNavDocument leftNavDoc = new LeftNavDocument("cat1_child1", "name1", "",
                Arrays.asList(new LeftNavCategoryBuilder().withId("cat1")
                        .withName("name1").withPath("cat1")
                        .withCategories(
                                Arrays.asList(new LeftNavCategoryBuilder().withId("child1").withPath("child1Path").build(),
                                        new LeftNavCategoryBuilder().withId("child2").withPath("child2Path").build()))
                        .build()), null, LEFTNAV_REFRESHABLE_PATH);
        return leftNavDoc;
    }

    public static LeftNavTree getLeftNavTreeWithDriveTo() {
        LeftNavTreeNode leftNavTreeNode = new LeftNavCategoryBuilder().withId("cat1_child1")
                .withName("name1").withPath("cat1")
                .withCategories(
                        Arrays.asList(new LeftNavCategoryBuilder().withId("child1").withPath("child1path").build(),
                                new LeftNavCategoryBuilder().withId("child2").withPath("child2Path").build()))
                .build();
        LeftNavNodeMapper leftNavNodeMapper = new LeftNavNodeMapper();

        LeftNavTree leftNavTree = new LeftNavTree("cat1_child1", "name1", Arrays.asList(leftNavNodeMapper.map(leftNavTreeNode)), Collections.emptyList(), LEFTNAV_REFRESHABLE_PATH);
        return leftNavTree;
    }
}
