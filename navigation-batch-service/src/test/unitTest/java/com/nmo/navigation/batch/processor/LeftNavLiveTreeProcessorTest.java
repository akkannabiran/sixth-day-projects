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
public class LeftNavLiveTreeProcessorTest {
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
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("live")).thenReturn("L");
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("stage")).thenReturn("stage");
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("marketing")).thenReturn("marketing");
        when(navigationBatchServiceConfig.getLeftNavBatchConfig().getWriteBatchSize()).thenReturn(5);
        when(navigationBatchServiceConfig.getLeftNavConfig().getLeftNavRefreshablePath()).thenReturn("/category/{refreshableCatId}/r_navaux.html");
        when(leftNavRepository.getPathsByReferenceId(anyString())).thenReturn(Collections.emptySet());

        categoryDocumentMap = getCategoryDocumentsForLiveTree();
        leftNavTreeProcessor.setCategoryDocumentMap(categoryDocumentMap);
    }

    @Test
    public void shouldBuildBasicLeftNavDocuments() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L3221"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(2));

        assertThat(leftNavDocuments.get(0).getId(), is("L_L3_L32_L322_L3221"));
        assertThat(leftNavDocuments.get(0).getCategoryId(), is("L3221"));
        assertThat(leftNavDocuments.get(0).getName(), is("L3221"));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("L322", "L321", "L32", "L31", "L323", "L3221", "L3222"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav(), containsInAnyOrder(Collections.emptyList()));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is(nullValue()));

        assertThat(leftNavDocuments.get(0).getLeftNav().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("L31"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getPath(), is("L_L3_L31"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getName(), is("L31"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getUrl(), is("/L/L3/L31/L31_L3_L/c.cat?navpath=L_L3_L31&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getId(), is("L32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getPath(), is("L_L3_L32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getName(), is("L32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getUrl(), is("/L/L3/L32/L32_L3_L/c.cat?navpath=L_L3_L32&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().size(), is(3));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("L321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getPath(), is("L_L3_L32_L321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getName(), is("L321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getUrl(), is("/L3/L32/L321/L321_L32_L3/c.cat?navpath=L_L3_L32_L321&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getId(), is("L322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getPath(), is("L_L3_L32_L322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getName(), is("L322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getUrl(), is("/L3/L32/L322/L322_L32_L3/c.cat?navpath=L_L3_L32_L322&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().size(), is(2));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(0).getId(), is("L3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(0).getPath(), is("L_L3_L32_L322_L3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(0).getName(), is("L3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(0).getUrl(), is("/L32/L322/L3221/L3221_L322_L32/c.cat?navpath=L_L3_L32_L322_L3221&source=leftNav"));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(1).getId(), is("L3222"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(1).getPath(), is("L_L3_L32_L322_L3222"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(1).getName(), is("L3222"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().get(1).getUrl(), is("/L32/L322/L3222/L3222_L322_L32/c.cat?navpath=L_L3_L32_L322_L3222&source=leftNav"));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getId(), is("L323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getPath(), is("L_L3_L32_L323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getName(), is("L323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getUrl(), is("/L3/L32/L323/L323_L32_L3/c.cat?navpath=L_L3_L32_L323&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getUrl(), is("/L3/L32/L323/L323_L32_L3/c.cat?navpath=L_L3_L32_L323&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(1).getId(), is("L_L1_L13_L3221"));
        assertThat(leftNavDocuments.get(1).getCategoryId(), is("L3221"));
        assertThat(leftNavDocuments.get(1).getName(), is("L3221"));
        assertThat(leftNavDocuments.get(1).getReferenceIds(), containsInAnyOrder("L11", "L12", "L13", "L3221"));
        assertThat(leftNavDocuments.get(1).getBoutiqueLeftNav(), containsInAnyOrder(Collections.emptyList()));
        assertThat(leftNavDocuments.get(1).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(1).getRefreshablePath(), is(nullValue()));

        assertThat(leftNavDocuments.get(1).getLeftNav().size(), is(3));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getId(), is("L11"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getPath(), is("L_L1_L11"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getName(), is("L11"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getUrl(), is("/L/L1/L11/L11_L1_L/c.cat?navpath=L_L1_L11&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(1).getId(), is("L12"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(1).getPath(), is("L_L1_L12"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(1).getName(), is("L12"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(1).getUrl(), is("/L/L1/L12/L12_L1_L/c.cat?navpath=L_L1_L12&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(1).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getId(), is("L13"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getPath(), is("L_L1_L13"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getName(), is("L13"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getUrl(), is("/L/L1/L13/L13_L1_L/c.cat?navpath=L_L1_L13&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getCategories().size(), is(1));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getCategories().get(0).getId(), is("L3221"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getCategories().get(0).getPath(), is("L_L1_L13_L3221"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getCategories().get(0).getName(), is("L3221"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getCategories().get(0).getUrl(), is("/L1/L13/L3221/L3221_L13_L1/c.cat?navpath=L_L1_L13_L3221&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(2).getCategories().get(0).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesChildCatOrder_whenItsAvailable() {
        CategoryDocument categoryDocument = categoryDocumentMap.get("L211");
        ContextualProperty contextualProperty = new ContextualProperty();
        contextualProperty.setParentId("L21");
        contextualProperty.setChildCategoryOrder(Arrays.asList("L2112", "L2111"));
        categoryDocument.setContextualProperties(Collections.singletonList(contextualProperty));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getPath(), is("L_L2_L21_L211_L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getPath(), is("L_L2_L21_L211_L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("L2111"));
    }

    @Test
    public void shouldBuildLeftNavDocument_byRemovingChildCatIdFromChildCatOrderIsNotAvailableAsDefaultChildren() {
        CategoryDocument categoryDocument = categoryDocumentMap.get("L211");
        ContextualProperty contextualProperty = new ContextualProperty();
        contextualProperty.setParentId("L21");
        contextualProperty.setChildCategoryOrder(Arrays.asList("L2112", "L2111", "L3221"));
        categoryDocument.setContextualProperties(Collections.singletonList(contextualProperty));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(2));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingDefaultChildrenOrder_whenContextualPropertiesChildCatOrderIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getPath(), is("L_L2_L21_L211_L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getPath(), is("L_L2_L21_L211_L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("L2112"));
    }

    @Test
    public void shouldBuildLeftNavDocument_includeChildrenFromDefaultCategories_whenTheyAreMissingInContextualPropertiesChildCategories() {
        CategoryDocument categoryDocument = categoryDocumentMap.get("L211");
        ContextualProperty contextualProperty = new ContextualProperty();
        categoryDocument.setChildren(Arrays.asList("L3221", "L2111", "L2112"));
        contextualProperty.setParentId("L21");
        contextualProperty.setChildCategoryOrder(Arrays.asList("L2112", "L2111"));
        categoryDocument.setContextualProperties(Collections.singletonList(contextualProperty));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getId(), is("L3221"));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesDesktopDisplayName_whenItsAvailable() {
        CategoryDocument categoryDocumentL2111 = categoryDocumentMap.get("L2111");
        ContextualProperty contextualPropertyL2111 = new ContextualProperty();
        contextualPropertyL2111.setParentId("L211");
        contextualPropertyL2111.setDesktopAlternateName("L2111_Alternate_Name");
        categoryDocumentL2111.setContextualProperties(Collections.singletonList(contextualPropertyL2111));

        CategoryDocument categoryDocumentL2112 = categoryDocumentMap.get("L2112");
        ContextualProperty contextualPropertyL2112 = new ContextualProperty();
        contextualPropertyL2112.setParentId("L211");
        contextualPropertyL2112.setDesktopAlternateName("L2112_Alternate_Name");
        categoryDocumentL2112.setContextualProperties(Collections.singletonList(contextualPropertyL2112));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getName(), is("L2111_Alternate_Name"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getUrl(), is("/L21/L211/L2111-Alternate-Name/L2111_L211_L21/c.cat?navpath=L_L2_L21_L211_L2111&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getName(), is("L2112_Alternate_Name"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getUrl(), is("/L21/L211/L2112-Alternate-Name/L2112_L211_L21/c.cat?navpath=L_L2_L21_L211_L2112&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingDefaultDisplayName_whenContextualPropertiesDesktopDisplayNameIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getName(), is("L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getName(), is("L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesRedText_whenItsAvailable() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("L211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("L21");
        contextualPropertyL211.setRedText(true);
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        CategoryDocument categoryDocumentL2111 = categoryDocumentMap.get("L2111");
        ContextualProperty contextualPropertyL2111 = new ContextualProperty();
        contextualPropertyL2111.setParentId("L211");
        contextualPropertyL2111.setRedText(true);
        categoryDocumentL2111.setContextualProperties(Collections.singletonList(contextualPropertyL2111));

        CategoryDocument categoryDocumentL2112 = categoryDocumentMap.get("L2112");
        ContextualProperty contextualPropertyL2112 = new ContextualProperty();
        contextualPropertyL2112.setParentId("L211");
        contextualPropertyL2112.setRedText(true);
        categoryDocumentL2112.setContextualProperties(Collections.singletonList(contextualPropertyL2112));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isRedText(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isRedText(), is(true));
    }

    @Test

    public void shouldBuildLeftNavDocument_setRedTextToFalse_whenContextualPropertiesRedTextIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("L2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isRedText(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_expandChildren_whenExpandChildrenIsTrue() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("L3212");
        categoryDocumentL3212.setExpandCategory(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("L31", "L32", "L321", "L322", "L323", "L3211", "L3212", "L32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("L321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getId(), is("L3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getCategories().get(0).getId(), is("L32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getCategories().get(0).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontExpandChildren_whenExpandChildrenIsFalse() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("L321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getId(), is("L3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_expandChildren_whenExpandChildrenIsTrueAndDontShowChildrenSetToTrue() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("L3212");
        categoryDocumentL3212.setExpandCategory(true);
        categoryDocumentL3212.setDontShowChildren(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("L321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getId(), is("L3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getCategories().get(0).getId(), is("L32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getCategories().get(0).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontExpandNode_whenNodeExpandChildrenIsTrueButTheNodeNotInPath() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("L3212");
        categoryDocumentL3212.setExpandCategory(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L3211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(0).getId(), is("L3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getId(), is("L3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().get(1).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_includeDriveToPath_whenDriveToSubCategoryPathIsAvailable() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("L211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("L21");
        contextualPropertyL211.setDriveToSubcategoryId("L2112");
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is("L_L2_L21_L211_L2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("L211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_includeDriveToPath_whenDriveToSubCategoriesIsNotDefaultChildren() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("L211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("L21");
        contextualPropertyL211.setDriveToSubcategoryId("L2112:L21121");
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is("L_L2_L21_L211_L2112_L21121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("L211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontIncludeDriveToPath_whenDriveToSubCategoryIsNotDefaultChildren() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("L211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("L21");
        contextualPropertyL211.setDriveToSubcategoryId("L3221");
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("L211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontIncludeDriveToPath_whenDriveToSubCategoriesIsNotDefaultChildren() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("L211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("L21");
        contextualPropertyL211.setDriveToSubcategoryId("L2112:L3221");
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("L211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_useDontShowChildren_whenItsTrue() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("L322");
        categoryDocumentL322.setDontShowChildren(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("L31"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getId(), is("L32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().size(), is(3));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("L321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getId(), is("L322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getId(), is("L323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getCategories().size(), is(0));

    }

    public void shouldBuildLeftNavDocument_includeExcludeCountries_whenItsAvailable() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("L322");
        categoryDocumentL322.setExcludedCountries(Arrays.asList("US", "IN"));

        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("L321");
        categoryDocumentL321.setExcludedCountries(Arrays.asList("AU", "AE"));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("L31"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getId(), is("L32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().size(), is(3));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("L321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getExcludedCountries(), containsInAnyOrder("AU", "AE"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getId(), is("L322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getExcludedCountries(), containsInAnyOrder("US", "IN"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getId(), is("L323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getExcludedCountries(), is(nullValue()));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingLeftNavImageAvailable_whenItsTrue() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("L322");
        categoryDocumentL322.setLeftNavImageAvailable(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is("/category/L322/r_navaux.html"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingLeftNavImageAvailableAndOverrides_whenItsTrue() {
        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("L321");
        categoryDocumentL321.setLeftNavImageAvailableOverride("override");
        categoryDocumentL321.setLeftNavImageAvailable(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is("/category/override/r_navaux.html"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_whenOneOrMoreChildrenMissing() {
        when(categoryRepository.getCategoryDocument(anyString())).thenReturn(null);

        categoryDocumentMap.remove("L322");

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L32"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().size(), is(2));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("L321"));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(1).getCategories().get(1).getId(), is("L323"));
        assertThat(allLeftNavDocuments.get(0).get(0).getReferenceIds(), containsInAnyOrder("L322", "L321", "L32", "L31", "L323"));
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagHiddenTrueForSelectedNode() {
        categoryDocumentMap.get("L3212").setHidden(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagNoResultsTrueForSelectedNode() {
        categoryDocumentMap.get("L3212").setNoResults(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagDeletedTrueForSelectedNode() {
        categoryDocumentMap.get("L3212").setDeleted(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldRebuildLeftNavDocument_whenRetryNodeHasMissingKey() {
        CategoryDocument L323 = categoryDocumentMap.get("L323");
        categoryDocumentMap.remove("L323");
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L32"), false);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L321"), false);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L322"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(1));
        assertThat(leftNavTreeProcessor.getRetryNodes().get("L323"), is(containsInAnyOrder("L32", "L321", "L322")));

        categoryDocumentMap.put("L323", L323);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("L323"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(0));

        verify(leftNavBatchWriter, times(4)).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(4));
        assertThat(allLeftNavDocuments.get(3).size(), is(4));
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenRootCategoryDoesntStartsWithLiveOrStageOrMarketing() {
        categoryDocumentMap.put("Unknown", CategoryDocument.builder().id("Unknown").parents(new HashMap<>()).build());
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("Unknown"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());
    }

    private Map<String, CategoryDocument> getCategoryDocumentsForLiveTree() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();

        categoryDocumentMap.put("L", CategoryDocument.builder().id("L").name("L").children(Arrays.asList("L1", "L2", "l3")).build());

        Map<String, Integer> l1 = new HashMap<>();
        l1.put("L", 0);
        categoryDocumentMap.put("L1", CategoryDocument.builder().id("L1").name("L1").children(Arrays.asList("L11", "L12", "L13")).parents(l1).build());

        Map<String, Integer> l2 = new HashMap<>();
        l2.put("L", 0);
        categoryDocumentMap.put("L2", CategoryDocument.builder().id("L2").name("L2").children(Collections.singletonList("L21")).parents(l2).build());

        Map<String, Integer> l21 = new HashMap<>();
        l21.put("L2", 0);
        categoryDocumentMap.put("L21", CategoryDocument.builder().id("L21").name("L21").children(Collections.singletonList("L211")).parents(l21).build());

        Map<String, Integer> m211 = new HashMap<>();
        m211.put("L21", 0);
        categoryDocumentMap.put("L211", CategoryDocument.builder().id("L211").name("L211").children(Arrays.asList("L2111", "L2112")).parents(m211).build());

        Map<String, Integer> m2111 = new HashMap<>();
        m2111.put("L211", 0);
        categoryDocumentMap.put("L2111", CategoryDocument.builder().id("L2111").name("L2111").parents(m2111).build());

        Map<String, Integer> m2112 = new HashMap<>();
        m2112.put("L211", 0);
        categoryDocumentMap.put("L2112", CategoryDocument.builder().id("L2112").name("L2112").parents(m2112).children(Collections.singletonList("L21121")).build());

        Map<String, Integer> l21121 = new HashMap<>();
        l21121.put("L2112", 0);
        categoryDocumentMap.put("L21121", CategoryDocument.builder().id("L21121").name("L21121").parents(l21121).build());

        Map<String, Integer> l3 = new HashMap<>();
        l3.put("L", 0);
        categoryDocumentMap.put("L3", CategoryDocument.builder().id("L3").name("L3").children(Arrays.asList("L31", "L32")).parents(l3).build());

        Map<String, Integer> l11 = new HashMap<>();
        l11.put("L1", 0);
        categoryDocumentMap.put("L11", CategoryDocument.builder().id("L11").name("L11").parents(l11).build());

        Map<String, Integer> l12 = new HashMap<>();
        l12.put("L1", 0);
        categoryDocumentMap.put("L12", CategoryDocument.builder().id("L12").name("L12").parents(l12).build());

        Map<String, Integer> l13 = new HashMap<>();
        l13.put("L1", 0);
        categoryDocumentMap.put("L13", CategoryDocument.builder().id("L13").name("L13").children(Collections.singletonList("L3221")).parents(l13).build());

        Map<String, Integer> l31 = new HashMap<>();
        l31.put("L3", 0);
        categoryDocumentMap.put("L31", CategoryDocument.builder().id("L31").name("L31").parents(l31).build());

        Map<String, Integer> l32 = new HashMap<>();
        l32.put("L3", 0);
        categoryDocumentMap.put("L32", CategoryDocument.builder().id("L32").name("L32").children(Arrays.asList("L321", "L322", "L323")).parents(l32).build());

        Map<String, Integer> l321 = new HashMap<>();
        l321.put("L32", 0);
        categoryDocumentMap.put("L321", CategoryDocument.builder().id("L321").name("L321").children(Arrays.asList("L3211", "L3212")).parents(l321).build());

        Map<String, Integer> l3211 = new HashMap<>();
        l3211.put("L321", 0);
        categoryDocumentMap.put("L3211", CategoryDocument.builder().id("L3211").name("L3211").parents(l3211).build());

        Map<String, Integer> l3212 = new HashMap<>();
        l3212.put("L321", 0);
        categoryDocumentMap.put("L3212", CategoryDocument.builder().id("L3212").name("L3212").children(Collections.singletonList("L32121")).parents(l3212).build());

        Map<String, Integer> l32121 = new HashMap<>();
        l32121.put("L3212", 0);
        categoryDocumentMap.put("L32121", CategoryDocument.builder().id("L32121").name("L32121").parents(l32121).build());

        Map<String, Integer> l322 = new HashMap<>();
        l322.put("L32", 0);
        categoryDocumentMap.put("L322", CategoryDocument.builder().id("L322").name("L322").children(Arrays.asList("L3221", "L3222")).parents(l322).build());

        Map<String, Integer> l323 = new HashMap<>();
        l323.put("L32", 0);
        categoryDocumentMap.put("L323", CategoryDocument.builder().id("L323").name("L323").parents(l323).build());

        Map<String, Integer> l3221 = new HashMap<>();
        l3221.put("L322", 0);
        l3221.put("L13", 0);
        categoryDocumentMap.put("L3221", CategoryDocument.builder().id("L3221").name("L3221").parents(l3221).build());

        Map<String, Integer> l3222 = new HashMap<>();
        l3222.put("L322", 0);
        categoryDocumentMap.put("L3222", CategoryDocument.builder().id("L3222").name("L3222").parents(l3222).build());

        return categoryDocumentMap;
    }
}