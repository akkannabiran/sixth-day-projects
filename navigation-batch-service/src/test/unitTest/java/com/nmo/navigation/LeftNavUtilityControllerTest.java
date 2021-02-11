package com.sixthday.navigation;

import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.navigation.batch.designers.processor.DesignerIndexProcessor;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import com.sixthday.navigation.elasticsearch.repository.LeftNavRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LeftNavUtilityControllerTest {
    @InjectMocks
    private LeftNavUtilityController leftNavUtilityController;
    @Mock
    private LeftNavTreeProcessor leftNavTreeProcessor;
    @Mock
    private DesignerIndexProcessor designerIndexProcessor;
    @Mock
    private LeftNavRepository leftNavRepository;

    @Test
    public void testPendingNodes() {
        Map<String, Set<String>> pendingNodes = new HashMap<>();
        Set<String> nodes = new HashSet<>();
        nodes.add("cat2");
        nodes.add("cat3");
        pendingNodes.put("cat0", nodes);

        when(leftNavTreeProcessor.getRetryNodes()).thenReturn(pendingNodes);

        assertEquals(leftNavUtilityController.pendingNodes(), pendingNodes);
    }

    @Test
    public void testReprocessPendingNodes() {
        assertEquals(leftNavUtilityController.reprocessPendingNodes(), "Success");
    }

    @Test
    public void testBuildLeftNav() {
        assertEquals(leftNavUtilityController.buildLeftNav(), "Success");
    }

    @Test
    public void testBuildLeftNavForACategory() {
        assertEquals(leftNavUtilityController.buildLeftNavForACategory("cat0"), "Success");
    }

    @Test
    public void testBuildDesignerIndex() {
        assertEquals(leftNavUtilityController.buildDesignerIndex(), "Success");
    }

    @Test
    public void testGetDesignerIndex() {
        DesignerIndex designerIndex = DesignerIndex.builder().id("catD1").build();

        when(designerIndexProcessor.getDesignerIndex("catD1")).thenReturn(designerIndex);

        assertEquals(leftNavUtilityController.getDesignerIndex("catD1"), designerIndex);

    }

    @Test
    public void testGetCategory() {
        Optional<CategoryDocument> categoryDocument = Optional.of(CategoryDocument.builder().id("catC1").name("catC1Name").build());

        when(leftNavTreeProcessor.getCategoryDocument("catC1")).thenReturn(categoryDocument);

        assertEquals(leftNavUtilityController.getCategory("catC1"), categoryDocument);
    }

    @Test
    public void testGetLeftNavPath() {
        String leftNavId = "leftNavId";
        LeftNavDocument expected = new LeftNavDocument();
        expected.setId(leftNavId);
        when(leftNavRepository.getLeftNavById(anyString())).thenReturn(expected);
        LeftNavDocument actual = leftNavUtilityController.getLeftNavFromEC(leftNavId);
        assertNotNull(actual);
        assertThat(actual.getId(), is(leftNavId));
    }
}