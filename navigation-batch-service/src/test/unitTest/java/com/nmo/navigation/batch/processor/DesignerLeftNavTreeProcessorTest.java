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
public class DesignerLeftNavTreeProcessorTest {
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
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("live")).thenReturn("D");
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("stage")).thenReturn("staging");
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("marketing")).thenReturn("marketing");
        when(navigationBatchServiceConfig.getLeftNavBatchConfig().getWriteBatchSize()).thenReturn(5);
        when(navigationBatchServiceConfig.getLeftNavConfig().getLeftNavRefreshablePath()).thenReturn("/category/{refreshableCatId}/r_navaux.html");
        when(navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId()).thenReturn("cat000730");
        when(leftNavRepository.getPathsByReferenceId(anyString())).thenReturn(Collections.emptySet());

        categoryDocumentMap = getCategoryDocumentsForLiveTree();
        leftNavTreeProcessor.setCategoryDocumentMap(categoryDocumentMap);
    }

    @Test
    public void shouldBuildBasicDesignerLeftNavDocuments() {
        categoryDocumentMap.get("D32").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D32"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();

        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));

        assertThat(leftNavDocuments.get(0).getId(), is("D_D3_D32"));
        assertThat(leftNavDocuments.get(0).getCategoryId(), is("D32"));
        assertThat(leftNavDocuments.get(0).getName(), is("D32"));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("D322", "D321", "D323"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is(nullValue()));

        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).getId(), is("cat000730"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).getName(), is("Shop All Designers"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).getPath(), is(nullValue()));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).isSelected(), is(false));

        assertThat(leftNavDocuments.get(0).getLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getPath(), is("D_D3_D32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getName(), is("D32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getUrl(), is("/D/cat000730/D32/D32_cat000730_D/c.cat?navpath=D_cat000730_D32&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(3));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("D_D3_D32_D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getName(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getUrl(), is("/D3/D32/D321/D321_D32_D3/c.cat?navpath=D_D3_D32_D321&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("D_D3_D32_D322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getName(), is("D322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getUrl(), is("/D3/D32/D322/D322_D32_D3/c.cat?navpath=D_D3_D32_D322&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getId(), is("D323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getPath(), is("D_D3_D32_D323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getName(), is("D323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getUrl(), is("/D3/D32/D323/D323_D32_D3/c.cat?navpath=D_D3_D32_D323&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildBasicDesignerLeftNavDocument_whenOneCategoryOnThePathIsBoutique() {
        categoryDocumentMap.get("D32").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();

        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));

        assertThat(leftNavDocuments.get(0).getId(), is("D_D3_D32_D321"));
        assertThat(leftNavDocuments.get(0).getCategoryId(), is("D321"));
        assertThat(leftNavDocuments.get(0).getName(), is("D321"));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("D322", "D321", "D323", "D3212", "D3211"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is(nullValue()));

        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).getId(), is("cat000730"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).getName(), is("Shop All Designers"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).getPath(), is(nullValue()));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().get(0).isSelected(), is(false));

        assertThat(leftNavDocuments.get(0).getLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getPath(), is("D_D3_D32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getName(), is("D32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getUrl(), is("/D/cat000730/D32/D32_cat000730_D/c.cat?navpath=D_cat000730_D32&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(3));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("D_D3_D32_D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getName(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getUrl(), is("/D3/D32/D321/D321_D32_D3/c.cat?navpath=D_D3_D32_D321&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(2));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("D3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getPath(), is("D_D3_D32_D321_D3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getName(), is("D3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getUrl(), is("/D32/D321/D3211/D3211_D321_D32/c.cat?navpath=D_D3_D32_D321_D3211&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("D3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getPath(), is("D_D3_D32_D321_D3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getName(), is("D3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getUrl(), is("/D32/D321/D3212/D3212_D321_D32/c.cat?navpath=D_D3_D32_D321_D3212&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("D_D3_D32_D322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getName(), is("D322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getUrl(), is("/D3/D32/D322/D322_D32_D3/c.cat?navpath=D_D3_D32_D322&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getId(), is("D323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getPath(), is("D_D3_D32_D323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getName(), is("D323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getUrl(), is("/D3/D32/D323/D323_D32_D3/c.cat?navpath=D_D3_D32_D323&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesChildCatOrder_whenItsAvailable() {
        CategoryDocument categoryDocument = categoryDocumentMap.get("D211");
        ContextualProperty contextualProperty = new ContextualProperty();
        contextualProperty.setParentId("D21");
        contextualProperty.setChildCategoryOrder(Arrays.asList("D2112", "D2111"));
        categoryDocument.setContextualProperties(Collections.singletonList(contextualProperty));

        categoryDocumentMap.get("D211").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("D_D2_D21_D211_D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("D_D2_D21_D211_D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D2111"));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingDefaultChildrenOrder_whenContextualPropertiesChildCatOrderIsUnavailable() {
        categoryDocumentMap.get("D211").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("D_D2_D21_D211_D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("D_D2_D21_D211_D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D2112"));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesDesktopDisplayName_whenItsAvailable() {
        CategoryDocument categoryDocumentL2111 = categoryDocumentMap.get("D2111");
        ContextualProperty contextualPropertyL2111 = new ContextualProperty();
        contextualPropertyL2111.setParentId("D211");
        contextualPropertyL2111.setDesktopAlternateName("D2111_Alternate_Name");
        categoryDocumentL2111.setContextualProperties(Collections.singletonList(contextualPropertyL2111));

        CategoryDocument categoryDocumentL2112 = categoryDocumentMap.get("D2112");
        ContextualProperty contextualPropertyL2112 = new ContextualProperty();
        contextualPropertyL2112.setParentId("D211");
        contextualPropertyL2112.setDesktopAlternateName("D2112_Alternate_Name");
        categoryDocumentL2112.setContextualProperties(Collections.singletonList(contextualPropertyL2112));

        categoryDocumentMap.get("D211").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getName(), is("D2111_Alternate_Name"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getUrl(), is("/D21/D211/D2111-Alternate-Name/D2111_D211_D21/c.cat?navpath=D_D2_D21_D211_D2111&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getName(), is("D2112_Alternate_Name"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getUrl(), is("/D21/D211/D2112-Alternate-Name/D2112_D211_D21/c.cat?navpath=D_D2_D21_D211_D2112&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingDefaultDisplayName_whenContextualPropertiesDesktopDisplayNameIsUnavailable() {
        categoryDocumentMap.get("D211").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getName(), is("D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getName(), is("D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesRedText_whenItsAvailable() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("D211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("D21");
        contextualPropertyL211.setRedText(true);
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        CategoryDocument categoryDocumentL2111 = categoryDocumentMap.get("D2111");
        ContextualProperty contextualPropertyL2111 = new ContextualProperty();
        contextualPropertyL2111.setParentId("D211");
        contextualPropertyL2111.setRedText(true);
        categoryDocumentL2111.setContextualProperties(Collections.singletonList(contextualPropertyL2111));

        CategoryDocument categoryDocumentL2112 = categoryDocumentMap.get("D2112");
        ContextualProperty contextualPropertyL2112 = new ContextualProperty();
        contextualPropertyL2112.setParentId("D211");
        contextualPropertyL2112.setRedText(true);
        categoryDocumentL2112.setContextualProperties(Collections.singletonList(contextualPropertyL2112));

        categoryDocumentMap.get("D211").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isRedText(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isRedText(), is(true));
    }

    @Test

    public void shouldBuildLeftNavDocument_setRedTextToFalse_whenContextualPropertiesRedTextIsUnavailable() {
        categoryDocumentMap.get("D211").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isRedText(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_expandChildren_whenExpandChildrenIsTrue() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("D3212");
        categoryDocumentL3212.setExpandCategory(true);

        categoryDocumentMap.get("D321").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("D3211", "D3212", "D32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getId(), is("D32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontExpandChildren_whenExpandChildrenIsFalse() {
        categoryDocumentMap.get("D321").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("D3211", "D3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_expandChildren_whenExpandChildrenIsTrueAndDontShowChildrenSetToTrue() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("D3212");
        categoryDocumentL3212.setExpandCategory(true);
        categoryDocumentL3212.setDontShowChildren(true);

        categoryDocumentMap.get("D321").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("D3211", "D3212", "D32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getId(), is("D32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_includeDriveToPath_whenDriveToSubCategoryPathIsAvailable() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("D211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("D21");
        contextualPropertyL211.setDriveToSubcategoryId("D2112");
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        categoryDocumentMap.get("D211").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is("D_D2_D21_D211_D2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_useDontShowChildren_whenItsTrue() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("D322");
        categoryDocumentL322.setDontShowChildren(true);
        categoryDocumentL322.setBoutique(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_includeExcludeCountries_whenItsAvailable() {
        CategoryDocument categoryDocumentL3222 = categoryDocumentMap.get("D3222");
        categoryDocumentL3222.setExcludedCountries(Arrays.asList("US", "IN"));

        CategoryDocument categoryDocumentL3221 = categoryDocumentMap.get("D3221");
        categoryDocumentL3221.setExcludedCountries(Arrays.asList("AU", "AE"));

        categoryDocumentMap.get("D322").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getExcludedCountries(), containsInAnyOrder("AU", "AE"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D3222"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getExcludedCountries(), containsInAnyOrder("US", "IN"));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingLeftNavImageAvailable_whenItsTrue() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("D322");
        categoryDocumentL322.setLeftNavImageAvailable(true);
        categoryDocumentL322.setBoutique(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is("/category/D322/r_navaux.html"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingLeftNavImageAvailableAndOverrides_whenItsTrue() {
        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("D321");
        categoryDocumentL321.setLeftNavImageAvailableOverride("override");
        categoryDocumentL321.setLeftNavImageAvailable(true);
        categoryDocumentL321.setBoutique(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is("/category/override/r_navaux.html"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontIncludeBoutiqueLeftNav_whenTemplateTypeIsChanelP3() {
        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("D321");
        categoryDocumentL321.setBoutique(true);
        categoryDocumentL321.setTemplateType("ChanelP3");

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_withNoUrl_whenDesignersCategoryIsNotAvailable() {
        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("D321");
        categoryDocumentMap.remove("cat000730");
        categoryDocumentL321.setBoutique(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("D321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getUrl(), is(nullValue()));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(true));
        assertThat(leftNavTreeProcessor.getRetryNodes().get("cat000730"), containsInAnyOrder("D321"));
    }

    @Test
    public void shouldBuildLeftNavDocument_whenOneOrMoreChildrenMissing() {
        when(categoryRepository.getCategoryDocument(anyString())).thenReturn(null);

        categoryDocumentMap.remove("D322");

        categoryDocumentMap.get("D32").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D32"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().size(), is(1));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(0).getCategories().size(), is(2));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("D321"));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("D323"));
        assertThat(allLeftNavDocuments.get(0).get(0).getReferenceIds(), containsInAnyOrder("D322", "D321", "D323"));
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagHiddenTrueForSelectedNode() {
        categoryDocumentMap.get("D3212").setHidden(true);
        categoryDocumentMap.get("D3212").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagNoResultsTrueForSelectedNode() {
        categoryDocumentMap.get("D3212").setNoResults(true);
        categoryDocumentMap.get("D3212").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagDeletedTrueForSelectedNode() {
        categoryDocumentMap.get("D3212").setDeleted(true);
        categoryDocumentMap.get("D3212").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldRebuildLeftNavDocument_whenRetryNodeHasMissingKey() {
        CategoryDocument L323 = categoryDocumentMap.get("D323");
        CategoryDocument cat000730 = categoryDocumentMap.get("cat000730");
        categoryDocumentMap.remove("D323");
        categoryDocumentMap.remove("cat000730");
        categoryDocumentMap.get("D32").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D32"), false);
        categoryDocumentMap.get("D321").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D321"), false);
        categoryDocumentMap.get("D322").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D322"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(2));
        assertThat(leftNavTreeProcessor.getRetryNodes().get("D323"), is(containsInAnyOrder("D32", "D321", "D322")));
        assertThat(leftNavTreeProcessor.getRetryNodes().get("cat000730"), is(containsInAnyOrder("D32", "D321", "D322")));

        categoryDocumentMap.put("D323", L323);
        categoryDocumentMap.get("D323").setBoutique(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("D323"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(1));

        categoryDocumentMap.put("cat000730", cat000730);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("cat000730"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(0));

        verify(leftNavBatchWriter, times(5)).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(5));
        assertThat(allLeftNavDocuments.get(4).size(), is(4));
        assertThat(allLeftNavDocuments.get(4).get(0).getLeftNav().get(0).getUrl(), is("/D/cat000730/D32/D32_cat000730_D/c.cat?navpath=D_cat000730_D32&source=leftNav"));
        assertThat(allLeftNavDocuments.get(4).get(1).getLeftNav().get(0).getUrl(), is("/D/cat000730/D32/D32_cat000730_D/c.cat?navpath=D_cat000730_D32&source=leftNav"));
        assertThat(allLeftNavDocuments.get(4).get(2).getLeftNav().get(0).getUrl(), is("/D/cat000730/D32/D32_cat000730_D/c.cat?navpath=D_cat000730_D32&source=leftNav"));
        assertThat(allLeftNavDocuments.get(4).get(3).getLeftNav().get(0).getUrl(), is("/D/cat000730/D32/D32_cat000730_D/c.cat?navpath=D_cat000730_D32&source=leftNav"));
    }

    private Map<String, CategoryDocument> getCategoryDocumentsForLiveTree() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();

        categoryDocumentMap.put("D", CategoryDocument.builder().id("D").name("D").children(Arrays.asList("D1", "D2", "D3")).build());

        Map<String, Integer> l1 = new HashMap<>();
        l1.put("D", 0);
        categoryDocumentMap.put("D1", CategoryDocument.builder().id("D1").name("D1").children(Arrays.asList("D11", "D12", "D13")).parents(l1).build());

        Map<String, Integer> l2 = new HashMap<>();
        l2.put("D", 0);
        categoryDocumentMap.put("D2", CategoryDocument.builder().id("D2").name("D2").children(Collections.singletonList("D21")).parents(l2).build());

        Map<String, Integer> l21 = new HashMap<>();
        l21.put("D2", 0);
        categoryDocumentMap.put("D21", CategoryDocument.builder().id("D21").name("D21").children(Collections.singletonList("D211")).parents(l21).build());

        Map<String, Integer> m211 = new HashMap<>();
        m211.put("D21", 0);
        categoryDocumentMap.put("D211", CategoryDocument.builder().id("D211").name("D211").children(Arrays.asList("D2111", "D2112")).parents(m211).build());

        Map<String, Integer> m2111 = new HashMap<>();
        m2111.put("D211", 0);
        categoryDocumentMap.put("D2111", CategoryDocument.builder().id("D2111").name("D2111").parents(m2111).build());

        Map<String, Integer> m2112 = new HashMap<>();
        m2112.put("D211", 0);
        categoryDocumentMap.put("D2112", CategoryDocument.builder().id("D2112").name("D2112").parents(m2112).build());

        Map<String, Integer> l21121 = new HashMap<>();
        l21121.put("D2112", 0);
        categoryDocumentMap.put("D21121", CategoryDocument.builder().id("D21121").name("D21121").parents(l21121).build());

        Map<String, Integer> l3 = new HashMap<>();
        l3.put("D", 0);
        categoryDocumentMap.put("D3", CategoryDocument.builder().id("D3").name("D3").children(Arrays.asList("D31", "D32")).parents(l3).build());

        Map<String, Integer> l11 = new HashMap<>();
        l11.put("D1", 0);
        categoryDocumentMap.put("D11", CategoryDocument.builder().id("D11").name("D11").parents(l11).build());

        Map<String, Integer> l12 = new HashMap<>();
        l12.put("D1", 0);
        categoryDocumentMap.put("D12", CategoryDocument.builder().id("D12").name("D12").parents(l12).build());

        Map<String, Integer> l13 = new HashMap<>();
        l13.put("D1", 0);
        categoryDocumentMap.put("D13", CategoryDocument.builder().id("D13").name("D13").children(Collections.singletonList("D3221")).parents(l13).build());

        Map<String, Integer> l31 = new HashMap<>();
        l31.put("D3", 0);
        categoryDocumentMap.put("D31", CategoryDocument.builder().id("D31").name("D31").parents(l31).build());

        Map<String, Integer> l32 = new HashMap<>();
        l32.put("D3", 0);
        categoryDocumentMap.put("D32", CategoryDocument.builder().id("D32").name("D32").children(Arrays.asList("D321", "D322", "D323")).parents(l32).build());

        Map<String, Integer> l321 = new HashMap<>();
        l321.put("D32", 0);
        categoryDocumentMap.put("D321", CategoryDocument.builder().id("D321").name("D321").children(Arrays.asList("D3211", "D3212")).parents(l321).build());

        Map<String, Integer> l3211 = new HashMap<>();
        l3211.put("D321", 0);
        categoryDocumentMap.put("D3211", CategoryDocument.builder().id("D3211").name("D3211").parents(l3211).build());

        Map<String, Integer> l3212 = new HashMap<>();
        l3212.put("D321", 0);
        categoryDocumentMap.put("D3212", CategoryDocument.builder().id("D3212").name("D3212").children(Collections.singletonList("D32121")).parents(l3212).build());

        Map<String, Integer> l32121 = new HashMap<>();
        l32121.put("D3212", 0);
        categoryDocumentMap.put("D32121", CategoryDocument.builder().id("D32121").name("D32121").parents(l32121).build());

        Map<String, Integer> l322 = new HashMap<>();
        l322.put("D32", 0);
        categoryDocumentMap.put("D322", CategoryDocument.builder().id("D322").name("D322").children(Arrays.asList("D3221", "D3222")).parents(l322).build());

        Map<String, Integer> l323 = new HashMap<>();
        l323.put("D32", 0);
        categoryDocumentMap.put("D323", CategoryDocument.builder().id("D323").name("D323").parents(l323).build());

        Map<String, Integer> l3221 = new HashMap<>();
        l3221.put("D322", 0);
        l3221.put("D13", 0);
        categoryDocumentMap.put("D3221", CategoryDocument.builder().id("D3221").name("D3221").parents(l3221).build());

        Map<String, Integer> l3222 = new HashMap<>();
        l3222.put("D322", 0);
        categoryDocumentMap.put("D3222", CategoryDocument.builder().id("D3222").name("D3222").parents(l3222).build());

        Map<String, Integer> cat000730 = new HashMap<>();
        cat000730.put("cat000730", 0);
        categoryDocumentMap.put("cat000730", CategoryDocument.builder().id("cat000730").name("cat000730").parents(new HashMap<>()).build());

        return categoryDocumentMap;
    }
}