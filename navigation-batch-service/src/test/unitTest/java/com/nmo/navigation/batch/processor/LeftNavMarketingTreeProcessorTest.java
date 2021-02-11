package com.sixthday.navigation.batch.processor;

import com.sixthday.navigation.batch.io.LeftNavBatchWriter;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.elasticsearch.repository.LeftNavRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LeftNavMarketingTreeProcessorTest {
    @InjectMocks
    private LeftNavTreeProcessor leftNavTreeProcessor;

    @Mock
    private LeftNavRepository leftNavRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LeftNavBatchWriter leftNavBatchWriter;

    @Captor
    private ArgumentCaptor<List<LeftNavDocument>> leftNavDocumentsCaptor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    private Map<String, CategoryDocument> categoryDocumentMap;

    @Before
    public void before() {
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("live")).thenReturn("live");
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("stage")).thenReturn("stage");
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("marketing")).thenReturn("M");
        when(navigationBatchServiceConfig.getLeftNavConfig().getLeftNavRefreshablePath()).thenReturn("/category/{refreshableCatId}/r_navaux.html");
        when(leftNavRepository.getPathsByReferenceId(anyString())).thenReturn(Collections.emptySet());
        when(navigationBatchServiceConfig.getLeftNavBatchConfig().getWriteBatchSize()).thenReturn(5);

        categoryDocumentMap = getCategoryDocumentsForStageTree();
        leftNavTreeProcessor.setCategoryDocumentMap(categoryDocumentMap);
    }

    @Test
    public void shouldBuildBasicLeftNavDocuments() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();

        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));

        assertThat(leftNavDocuments.get(0).getId(), is("M_M3_M32_M322"));
        assertThat(leftNavDocuments.get(0).getCategoryId(), is("M322"));
        assertThat(leftNavDocuments.get(0).getName(), is("M322"));
        // M322 is the selected node and reference ID would not include the selected node ID.
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("M3222", "M3221"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav(), containsInAnyOrder(Collections.emptyList()));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is(nullValue()));

        assertThat(leftNavDocuments.get(0).getLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("M322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getPath(), is("M_M3_M32_M322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getName(), is("M322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getUrl(), is("/M3/M32/M322/M322_M32_M3/c.cat?navpath=M_M3_M32_M322&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(2));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("M_M3_M32_M322_M3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getName(), is("M3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getUrl(), is("/M32/M322/M3221/M3221_M322_M32/c.cat?navpath=M_M3_M32_M322_M3221&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M3222"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("M_M3_M32_M322_M3222"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getName(), is("M3222"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getUrl(), is("/M32/M322/M3222/M3222_M322_M32/c.cat?navpath=M_M3_M32_M322_M3222&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesChildCatOrder_whenItsAvailable() {
        CategoryDocument categoryDocument = categoryDocumentMap.get("M211");
        ContextualProperty contextualProperty = new ContextualProperty();
        contextualProperty.setParentId("M21");
        contextualProperty.setChildCategoryOrder(Arrays.asList("M2112", "M2111"));
        categoryDocument.setContextualProperties(Collections.singletonList(contextualProperty));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("M_M2_M21_M211_M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("M_M2_M21_M211_M2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M2111"));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingDefaultChildrenOrder_whenContextualPropertiesChildCatOrderIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("M_M2_M21_M211_M2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("M_M2_M21_M211_M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M2112"));
    }

    @Test
    public void shouldBuildLeftNavDocumentForLevel2CategoryAndIncludeItsSubCategories() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M1"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("M_M1_M11"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M11"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("M_M1_M12"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M12"));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesDesktopDisplayName_whenItsAvailable() {
        CategoryDocument categoryDocumentL2111 = categoryDocumentMap.get("M2111");
        ContextualProperty contextualPropertyL2111 = new ContextualProperty();
        contextualPropertyL2111.setParentId("M211");
        contextualPropertyL2111.setDesktopAlternateName("M2111_Alternate_Name");
        categoryDocumentL2111.setContextualProperties(Collections.singletonList(contextualPropertyL2111));

        CategoryDocument categoryDocumentL2112 = categoryDocumentMap.get("M2112");
        ContextualProperty contextualPropertyL2112 = new ContextualProperty();
        contextualPropertyL2112.setParentId("M211");
        contextualPropertyL2112.setDesktopAlternateName("M2112_Alternate_Name");
        categoryDocumentL2112.setContextualProperties(Collections.singletonList(contextualPropertyL2112));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getName(), is("M2111_Alternate_Name"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getUrl(), is("/M21/M211/M2111-Alternate-Name/M2111_M211_M21/c.cat?navpath=M_M2_M21_M211_M2111&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getName(), is("M2112_Alternate_Name"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getUrl(), is("/M21/M211/M2112-Alternate-Name/M2112_M211_M21/c.cat?navpath=M_M2_M21_M211_M2112&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingDefaultDisplayName_whenContextualPropertiesDesktopDisplayNameIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getName(), is("M2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getName(), is("M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesRedText_whenItsAvailable() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("M211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("M21");
        contextualPropertyL211.setRedText(true);
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        CategoryDocument categoryDocumentL2111 = categoryDocumentMap.get("M2111");
        ContextualProperty contextualPropertyL2111 = new ContextualProperty();
        contextualPropertyL2111.setParentId("M211");
        contextualPropertyL2111.setRedText(true);
        categoryDocumentL2111.setContextualProperties(Collections.singletonList(contextualPropertyL2111));

        CategoryDocument categoryDocumentL2112 = categoryDocumentMap.get("M2112");
        ContextualProperty contextualPropertyL2112 = new ContextualProperty();
        contextualPropertyL2112.setParentId("M211");
        contextualPropertyL2112.setRedText(true);
        categoryDocumentL2112.setContextualProperties(Collections.singletonList(contextualPropertyL2112));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isRedText(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isRedText(), is(true));
    }

    @Test

    public void shouldBuildLeftNavDocument_MetRedTextToFalse_whenContextualPropertiesRedTextIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isRedText(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_expandChildren_whenExpandChildrenIsTrue() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("M3212");
        categoryDocumentL3212.setExpandCategory(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("M3211", "M3212", "M32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("M321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getId(), is("M32121"));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontExpandChildren_whenExpandChildrenIsFalse() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("M3211", "M3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("M321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_expandChildren_whenExpandChildrenIsTrueAndDontShowChildrenSetToTrue() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("M3212");
        categoryDocumentL3212.setExpandCategory(true);
        categoryDocumentL3212.setDontShowChildren(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("M321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getId(), is("M32121"));
    }

    @Test
    public void shouldBuildLeftNavDocument_includeDriveToPath_whenDriveToSubCategoryPathIsAvailable() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("M211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("M21");
        contextualPropertyL211.setDriveToSubcategoryId("M2112");
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is("M_M2_M21_M211_M2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("M211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_useDontShowChildren_whenItsTrue() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("M322");
        categoryDocumentL322.setDontShowChildren(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("M322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(0));
    }

    public void shouldBuildLeftNavDocument_includeExcludeCountries_whenItsAvailable() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("M322");
        categoryDocumentL322.setExcludedCountries(Arrays.asList("US", "IN"));

        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("M321");
        categoryDocumentL321.setExcludedCountries(Arrays.asList("AU", "AE"));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("M31"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getId(), is("M32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().size(), is(3));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("M321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getExcludedCountries(), containsInAnyOrder("AU", "AE"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getId(), is("M322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getExcludedCountries(), containsInAnyOrder("US", "IN"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getId(), is("M323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getExcludedCountries(), is(nullValue()));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingLeftNavImageAvailable_whenItsTrue() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("M322");
        categoryDocumentL322.setLeftNavImageAvailable(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is("/category/M322/r_navaux.html"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingLeftNavImageAvailableAndOverrides_whenItsTrue() {
        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("M321");
        categoryDocumentL321.setLeftNavImageAvailableOverride("override");
        categoryDocumentL321.setLeftNavImageAvailable(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is("/category/override/r_navaux.html"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_whenOneOrMoreChildrenMissing() {
        when(categoryRepository.getCategoryDocument(anyString())).thenReturn(null);

        categoryDocumentMap.remove("M322");

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M32"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().size(), is(1));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("M321"));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("M323"));
        assertThat(allLeftNavDocuments.get(0).get(0).getReferenceIds(), containsInAnyOrder("M322", "M321", "M323"));
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagHiddenTrueForSelectedNode() {
        categoryDocumentMap.get("M3212").setHidden(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagNoResultsTrueForSelectedNode() {
        categoryDocumentMap.get("M3212").setNoResults(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagDeletedTrueForSelectedNode() {
        categoryDocumentMap.get("M3212").setDeleted(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldRebuildLeftNavDocument_whenRetryNodeHasMissingKey() {
        CategoryDocument M323 = categoryDocumentMap.get("M323");
        categoryDocumentMap.remove("M323");
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M32"), false);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M321"), false);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M322"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(1));
        assertThat(leftNavTreeProcessor.getRetryNodes().get("M323"), is(containsInAnyOrder("M32")));

        categoryDocumentMap.put("M323", M323);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("M323"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(0));

        verify(leftNavBatchWriter, times(4)).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(4));
        assertThat(allLeftNavDocuments.get(0).size(), is(1));
        assertThat(allLeftNavDocuments.get(1).size(), is(1));
        assertThat(allLeftNavDocuments.get(2).size(), is(1));
        assertThat(allLeftNavDocuments.get(3).size(), is(2));
    }

    private Map<String, CategoryDocument> getCategoryDocumentsForStageTree() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();

        categoryDocumentMap.put("M", CategoryDocument.builder().id("M").name("M").children(Arrays.asList("M1", "M2", "M3")).build());

        Map<String, Integer> l1 = new HashMap<>();
        l1.put("M", 0);
        categoryDocumentMap.put("M1", CategoryDocument.builder().id("M1").name("M1").children(Arrays.asList("M11", "M12", "M13")).parents(l1).build());

        Map<String, Integer> l2 = new HashMap<>();
        l2.put("M", 0);
        categoryDocumentMap.put("M2", CategoryDocument.builder().id("M2").name("M2").children(Collections.singletonList("M21")).parents(l2).build());

        Map<String, Integer> l21 = new HashMap<>();
        l21.put("M2", 0);
        categoryDocumentMap.put("M21", CategoryDocument.builder().id("M21").name("M21").children(Collections.singletonList("M211")).parents(l21).build());

        Map<String, Integer> m211 = new HashMap<>();
        m211.put("M21", 0);
        categoryDocumentMap.put("M211", CategoryDocument.builder().id("M211").name("M211").children(Arrays.asList("M2111", "M2112")).parents(m211).build());

        Map<String, Integer> m2111 = new HashMap<>();
        m2111.put("M211", 0);
        categoryDocumentMap.put("M2111", CategoryDocument.builder().id("M2111").name("M2111").parents(m2111).build());

        Map<String, Integer> m2112 = new HashMap<>();
        m2112.put("M211", 0);
        categoryDocumentMap.put("M2112", CategoryDocument.builder().id("M2112").name("M2112").parents(m2112).build());

        Map<String, Integer> l21121 = new HashMap<>();
        l21121.put("M2112", 0);
        categoryDocumentMap.put("M21121", CategoryDocument.builder().id("M21121").name("M21121").parents(l21121).build());

        Map<String, Integer> l3 = new HashMap<>();
        l3.put("M", 0);
        categoryDocumentMap.put("M3", CategoryDocument.builder().id("M3").name("M3").children(Arrays.asList("M31", "M32")).parents(l3).build());

        Map<String, Integer> l11 = new HashMap<>();
        l11.put("M1", 0);
        categoryDocumentMap.put("M11", CategoryDocument.builder().id("M11").name("M11").parents(l11).build());

        Map<String, Integer> l12 = new HashMap<>();
        l12.put("M1", 0);
        categoryDocumentMap.put("M12", CategoryDocument.builder().id("M12").name("M12").parents(l12).build());

        Map<String, Integer> l13 = new HashMap<>();
        l13.put("M1", 0);
        categoryDocumentMap.put("M13", CategoryDocument.builder().id("M13").name("M13").children(Collections.singletonList("M3221")).parents(l13).build());

        Map<String, Integer> l31 = new HashMap<>();
        l31.put("M3", 0);
        categoryDocumentMap.put("M31", CategoryDocument.builder().id("M31").name("M31").parents(l31).build());

        Map<String, Integer> l32 = new HashMap<>();
        l32.put("M3", 0);
        categoryDocumentMap.put("M32", CategoryDocument.builder().id("M32").name("M32").children(Arrays.asList("M321", "M322", "M323")).parents(l32).build());

        Map<String, Integer> l321 = new HashMap<>();
        l321.put("M32", 0);
        categoryDocumentMap.put("M321", CategoryDocument.builder().id("M321").name("M321").children(Arrays.asList("M3211", "M3212")).parents(l321).build());

        Map<String, Integer> l3211 = new HashMap<>();
        l3211.put("M321", 0);
        categoryDocumentMap.put("M3211", CategoryDocument.builder().id("M3211").name("M3211").parents(l3211).build());

        Map<String, Integer> l3212 = new HashMap<>();
        l3212.put("M321", 0);
        categoryDocumentMap.put("M3212", CategoryDocument.builder().id("M3212").name("M3212").children(Collections.singletonList("M32121")).parents(l3212).build());

        Map<String, Integer> l32121 = new HashMap<>();
        l32121.put("M3212", 0);
        categoryDocumentMap.put("M32121", CategoryDocument.builder().id("M32121").name("M32121").parents(l32121).build());

        Map<String, Integer> l322 = new HashMap<>();
        l322.put("M32", 0);
        categoryDocumentMap.put("M322", CategoryDocument.builder().id("M322").name("M322").children(Arrays.asList("M3221", "M3222")).parents(l322).build());

        Map<String, Integer> l323 = new HashMap<>();
        l323.put("M32", 0);
        categoryDocumentMap.put("M323", CategoryDocument.builder().id("M323").name("M323").parents(l323).build());

        Map<String, Integer> l3221 = new HashMap<>();
        l3221.put("M322", 0);
        l3221.put("M13", 0);
        categoryDocumentMap.put("M3221", CategoryDocument.builder().id("M3221").name("M3221").parents(l3221).build());

        Map<String, Integer> l3222 = new HashMap<>();
        l3222.put("M322", 0);
        categoryDocumentMap.put("M3222", CategoryDocument.builder().id("M3222").name("M3222").parents(l3222).build());

        return categoryDocumentMap;
    }
}