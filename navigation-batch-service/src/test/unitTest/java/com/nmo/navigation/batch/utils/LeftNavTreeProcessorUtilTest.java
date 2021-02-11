package com.sixthday.navigation.batch.utils;

import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LeftNavTreeProcessorUtilTest {

    @InjectMocks
    private LeftNavTreeProcessorUtil leftNavTreeProcessorUtil;

    @Test
    public void shouldBuildSemanticUrlByIncludingOnlyThreeLeafPaths() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();
        categoryDocumentMap.put("cat1", CategoryDocument.builder().id("cat1").name("cat1Name").build());
        categoryDocumentMap.put("cat2", CategoryDocument.builder().id("cat2").name("cat2Name").build());
        categoryDocumentMap.put("cat3", CategoryDocument.builder().id("cat3").name("cat3Name").build());
        categoryDocumentMap.put("cat4", CategoryDocument.builder().id("cat4").name("cat4Name").build());
        categoryDocumentMap.put("cat5", CategoryDocument.builder().id("cat5").name("cat5Name").build());

        CategoryDocument categoryDocument5 = categoryDocumentMap.get("cat5");

        String semanticUrl = leftNavTreeProcessorUtil.getUrl("cat1_cat2_cat3_cat4_cat5", categoryDocument5, categoryDocumentMap);
        assertThat(semanticUrl, is("/cat3Name/cat4Name/cat5Name/cat5_cat4_cat3/c.cat?navpath=cat1_cat2_cat3_cat4_cat5&source=leftNav"));
    }

    @Test
    public void shouldBuildSemanticUrlByIncludingAllNavPaths() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();
        categoryDocumentMap.put("cat1", CategoryDocument.builder().id("cat1").name("cat1Name").build());
        categoryDocumentMap.put("cat2", CategoryDocument.builder().id("cat2").name("cat2Name").build());

        CategoryDocument categoryDocument1 = categoryDocumentMap.get("cat1");

        String semanticUrl = leftNavTreeProcessorUtil.getUrl("cat1_cat2", categoryDocument1, categoryDocumentMap);
        assertThat(semanticUrl, is("/cat1Name/cat2Name/cat2_cat1/c.cat?navpath=cat1_cat2&source=leftNav"));
    }

    @Test
    public void shouldBuildSemanticUrlByUsingDesktopAlternateDisplayName() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();
        categoryDocumentMap.put("cat1", CategoryDocument.builder().id("cat1").name("cat1Name").build());
        categoryDocumentMap.put("cat2", CategoryDocument.builder().id("cat2").name("cat2Name").build());
        categoryDocumentMap.put("cat3", CategoryDocument.builder().id("cat3").name("cat3Name").contextualProperties(Collections.singletonList(ContextualProperty.builder().parentId("cat2").desktopAlternateName("alternate_DisplayName").build())).build());
        categoryDocumentMap.put("cat4", CategoryDocument.builder().id("cat4").name("cat4Name").build());
        categoryDocumentMap.put("cat5", CategoryDocument.builder().id("cat5").name("cat5Name").build());

        CategoryDocument categoryDocument5 = categoryDocumentMap.get("cat5");

        String semanticUrl = leftNavTreeProcessorUtil.getUrl("cat1_cat2_cat3_cat4_cat5", categoryDocument5, categoryDocumentMap);
        assertThat(semanticUrl, is("/alternate-DisplayName/cat4Name/cat5Name/cat5_cat4_cat3/c.cat?navpath=cat1_cat2_cat3_cat4_cat5&source=leftNav"));
    }

    @Test
    public void shouldUpdateSemanticUrlByUsingNavPath() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();
        categoryDocumentMap.put("cat1", CategoryDocument.builder().id("cat1").name("cat1Name").build());
        categoryDocumentMap.put("cat2", CategoryDocument.builder().id("cat2").name("cat2Name").build());
        categoryDocumentMap.put("cat3", CategoryDocument.builder().id("cat3").name("cat3Name").contextualProperties(Collections.singletonList(ContextualProperty.builder().parentId("cat2").desktopAlternateName("alternate_DisplayName").build())).build());
        categoryDocumentMap.put("cat4", CategoryDocument.builder().id("cat4").name("cat4Name").build());
        categoryDocumentMap.put("cat5", CategoryDocument.builder().canonicalUrl("/someurl/cat5/c.cat").id("cat5").name("cat5Name").build());

        CategoryDocument categoryDocument5 = categoryDocumentMap.get("cat5");

        String semanticUrl = leftNavTreeProcessorUtil.getUrl("cat1_cat2_cat3_cat4_cat5", categoryDocument5, categoryDocumentMap);
        assertThat(semanticUrl, is("/someurl/cat5/c.cat?navpath=cat1_cat2_cat3_cat4_cat5&source=leftNav"));
    }
    
    @Test
    public void shouldUpdateSemanticUrlByUsingNavPathWhenCategoryNameIsEmpty() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();
        categoryDocumentMap.put("cat1", CategoryDocument.builder().id("cat1").name("").build());
        categoryDocumentMap.put("cat2", CategoryDocument.builder().id("cat2").name("cat2Name").build());
        categoryDocumentMap.put("cat3", CategoryDocument.builder().id("cat3").name("cat3Name").contextualProperties(Collections.singletonList(ContextualProperty.builder().parentId("cat2").desktopAlternateName(null).build())).build());
        categoryDocumentMap.put("cat4", CategoryDocument.builder().id("cat4").name(null).build());
        categoryDocumentMap.put("cat5", CategoryDocument.builder().id("cat5").name("-").build());

        CategoryDocument categoryDocument5 = categoryDocumentMap.get("cat5");

        String semanticUrl = leftNavTreeProcessorUtil.getUrl("cat1_cat2_cat3_cat4_cat5", categoryDocument5, categoryDocumentMap);
        assertThat(semanticUrl, is("/cat3Name/null/-/cat5_cat4_cat3/c.cat?navpath=cat1_cat2_cat3_cat4_cat5&source=leftNav"));
    }
}


