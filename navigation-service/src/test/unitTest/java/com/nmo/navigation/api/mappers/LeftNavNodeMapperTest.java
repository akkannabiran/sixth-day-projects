package com.sixthday.navigation.api.mappers;

import com.sixthday.navigation.api.models.LeftNavCategoryBuilder;
import com.sixthday.navigation.api.models.response.LeftNavNode;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class LeftNavNodeMapperTest {
    private LeftNavNodeMapper leftNavNodeMapper = new LeftNavNodeMapper();

    @Test
    public void shouldReturnLeftNavNodesGivenLeftNavCategories() {
        LeftNavTreeNode childLeftNavTreeNode = new LeftNavCategoryBuilder().withId("child").build();
        LeftNavTreeNode parentLeftNavTreeNode = new LeftNavCategoryBuilder().withId("parent").withCategories(Arrays.asList(childLeftNavTreeNode)).build();
        List<LeftNavNode> leftNavNodes = leftNavNodeMapper.map(Arrays.asList(parentLeftNavTreeNode));

        assertEquals(1, leftNavNodes.size());
        assertEquals("parent", leftNavNodes.get(0).getId());
        assertEquals("child", leftNavNodes.get(0).getCategories().get(0).getId());
    }

    @Test
    public void shouldReturnEmptyListGivenNoLeftNavCategories() {
        List<LeftNavNode> leftNavNodes = leftNavNodeMapper.map(emptyList());
        assertEquals(0, leftNavNodes.size());
    }

}