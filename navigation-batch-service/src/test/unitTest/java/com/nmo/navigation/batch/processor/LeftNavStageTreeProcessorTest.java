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
public class LeftNavStageTreeProcessorTest {
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
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("stage")).thenReturn("S");
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("marketing")).thenReturn("marketing");
        when(navigationBatchServiceConfig.getLeftNavConfig().getLeftNavRefreshablePath()).thenReturn("/category/{refreshableCatId}/r_navaux.html");
        when(leftNavRepository.getPathsByReferenceId(anyString())).thenReturn(Collections.emptySet());
        when(navigationBatchServiceConfig.getLeftNavBatchConfig().getWriteBatchSize()).thenReturn(5);

        categoryDocumentMap = getCategoryDocumentsForStageTree();
        leftNavTreeProcessor.setCategoryDocumentMap(categoryDocumentMap);
    }

    @Test
    public void shouldBuildBasicLeftNavDocuments() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S3221"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();

        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(2));

        assertThat(leftNavDocuments.get(0).getId(), is("S_S1_S13_S3221"));
        assertThat(leftNavDocuments.get(0).getCategoryId(), is("S3221"));
        assertThat(leftNavDocuments.get(0).getName(), is("S3221"));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("S3", "S2", "S1", "S11", "S12", "S13", "S3221"));
        assertThat(leftNavDocuments.get(0).getBoutiqueLeftNav(), containsInAnyOrder(Collections.emptyList()));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is(nullValue()));

        assertThat(leftNavDocuments.get(0).getLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("S"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getPath(), is("S"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getName(), is("S"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getUrl(), is("/S/S/c.cat?navpath=S&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(3));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getId(), is("S1"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getPath(), is("S_S1"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getName(), is("S1"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getUrl(), is("/S/S1/S1_S/c.cat?navpath=S_S1&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(3));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getId(), is("S11"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getPath(), is("S_S1_S11"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getName(), is("S11"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getUrl(), is("/S/S1/S11/S11_S1_S/c.cat?navpath=S_S1_S11&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getId(), is("S12"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getPath(), is("S_S1_S12"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getName(), is("S12"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getUrl(), is("/S/S1/S12/S12_S1_S/c.cat?navpath=S_S1_S12&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(1).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getId(), is("S13"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getPath(), is("S_S1_S13"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getName(), is("S13"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getUrl(), is("/S/S1/S13/S13_S1_S/c.cat?navpath=S_S1_S13&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getCategories().size(), is(1));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getCategories().get(0).getId(), is("S3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getCategories().get(0).getPath(), is("S_S1_S13_S3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getCategories().get(0).getName(), is("S3221"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getCategories().get(0).getUrl(), is("/S1/S13/S3221/S3221_S13_S1/c.cat?navpath=S_S1_S13_S3221&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(0).getCategories().get(2).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getId(), is("S2"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getPath(), is("S_S2"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getName(), is("S2"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getUrl(), is("/S/S2/S2_S/c.cat?navpath=S_S2&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getId(), is("S3"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getPath(), is("S_S3"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getName(), is("S3"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getUrl(), is("/S/S3/S3_S/c.cat?navpath=S_S3&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(1).getId(), is("S_S3_S32_S322_S3221"));
        assertThat(leftNavDocuments.get(1).getCategoryId(), is("S3221"));
        assertThat(leftNavDocuments.get(1).getName(), is("S3221"));
        assertThat(leftNavDocuments.get(1).getReferenceIds(), containsInAnyOrder("S1", "S2", "S3", "S322", "S321", "S32", "S31", "S323", "S3221", "S3222"));
        assertThat(leftNavDocuments.get(1).getBoutiqueLeftNav(), containsInAnyOrder(Collections.emptyList()));
        assertThat(leftNavDocuments.get(1).getDriveToPath(), is(""));
        assertThat(leftNavDocuments.get(1).getRefreshablePath(), is(nullValue()));

        assertThat(leftNavDocuments.get(1).getLeftNav().size(), is(1));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getId(), is("S"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getPath(), is("S"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getName(), is("S"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getUrl(), is("/S/S/c.cat?navpath=S&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().size(), is(3));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(0).getId(), is("S1"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(0).getPath(), is("S_S1"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(0).getName(), is("S1"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(0).getUrl(), is("/S/S1/S1_S/c.cat?navpath=S_S1&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(1).getId(), is("S2"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(1).getPath(), is("S_S2"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(1).getName(), is("S2"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(1).getUrl(), is("/S/S2/S2_S/c.cat?navpath=S_S2&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(1).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getId(), is("S3"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getPath(), is("S_S3"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getName(), is("S3"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getUrl(), is("/S/S3/S3_S/c.cat?navpath=S_S3&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().size(), is(2));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).getId(), is("S31"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).getPath(), is("S_S3_S31"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).getName(), is("S31"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).getUrl(), is("/S/S3/S31/S31_S3_S/c.cat?navpath=S_S3_S31&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getId(), is("S32"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getPath(), is("S_S3_S32"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getName(), is("S32"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getUrl(), is("/S/S3/S32/S32_S3_S/c.cat?navpath=S_S3_S32&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().size(), is(3));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getId(), is("S321"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getPath(), is("S_S3_S32_S321"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getName(), is("S321"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getUrl(), is("/S3/S32/S321/S321_S32_S3/c.cat?navpath=S_S3_S32_S321&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().size(), is(0));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getId(), is("S322"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getPath(), is("S_S3_S32_S322"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getName(), is("S322"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getUrl(), is("/S3/S32/S322/S322_S32_S3/c.cat?navpath=S_S3_S32_S322&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().size(), is(2));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(0).getId(), is("S3221"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(0).getPath(), is("S_S3_S32_S322_S3221"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(0).getName(), is("S3221"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(0).getUrl(), is("/S32/S322/S3221/S3221_S322_S32/c.cat?navpath=S_S3_S32_S322_S3221&source=leftNav"));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(1).getId(), is("S3222"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(1).getPath(), is("S_S3_S32_S322_S3222"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(1).getName(), is("S3222"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(1).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().get(1).getUrl(), is("/S32/S322/S3222/S3222_S322_S32/c.cat?navpath=S_S3_S32_S322_S3222&source=leftNav"));

        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).getId(), is("S323"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).getPath(), is("S_S3_S32_S323"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).getName(), is("S323"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).isRedText(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).isSelected(), is(false));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).getUrl(), is("/S3/S32/S323/S323_S32_S3/c.cat?navpath=S_S3_S32_S323&source=leftNav"));
        assertThat(leftNavDocuments.get(1).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesChildCatOrder_whenItsAvailable() {
        CategoryDocument categoryDocument = categoryDocumentMap.get("S211");
        ContextualProperty contextualProperty = new ContextualProperty();
        contextualProperty.setParentId("S21");
        contextualProperty.setChildCategoryOrder(Arrays.asList("S2112", "S2111"));
        categoryDocument.setContextualProperties(Collections.singletonList(contextualProperty));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getPath(), is("S_S2_S21_S211_S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getId(), is("S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getPath(), is("S_S2_S21_S211_S2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getId(), is("S2111"));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingDefaultChildrenOrder_whenContextualPropertiesChildCatOrderIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getPath(), is("S_S2_S21_S211_S2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getId(), is("S2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getPath(), is("S_S2_S21_S211_S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getId(), is("S2112"));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesDesktopDisplayName_whenItsAvailable() {
        CategoryDocument categoryDocumentL2111 = categoryDocumentMap.get("S2111");
        ContextualProperty contextualPropertyL2111 = new ContextualProperty();
        contextualPropertyL2111.setParentId("S211");
        contextualPropertyL2111.setDesktopAlternateName("S2111_Alternate_Name");
        categoryDocumentL2111.setContextualProperties(Collections.singletonList(contextualPropertyL2111));

        CategoryDocument categoryDocumentL2112 = categoryDocumentMap.get("S2112");
        ContextualProperty contextualPropertyL2112 = new ContextualProperty();
        contextualPropertyL2112.setParentId("S211");
        contextualPropertyL2112.setDesktopAlternateName("S2112_Alternate_Name");
        categoryDocumentL2112.setContextualProperties(Collections.singletonList(contextualPropertyL2112));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getId(), is("S2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getName(), is("S2111_Alternate_Name"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getUrl(), is("/S21/S211/S2111-Alternate-Name/S2111_S211_S21/c.cat?navpath=S_S2_S21_S211_S2111&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getId(), is("S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getName(), is("S2112_Alternate_Name"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getUrl(), is("/S21/S211/S2112-Alternate-Name/S2112_S211_S21/c.cat?navpath=S_S2_S21_S211_S2112&source=leftNav"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingDefaultDisplayName_whenContextualPropertiesDesktopDisplayNameIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getId(), is("S2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getName(), is("S2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getId(), is("S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getName(), is("S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingContextualPropertiesRedText_whenItsAvailable() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("S211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("S21");
        contextualPropertyL211.setRedText(true);
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        CategoryDocument categoryDocumentL2111 = categoryDocumentMap.get("S2111");
        ContextualProperty contextualPropertyL2111 = new ContextualProperty();
        contextualPropertyL2111.setParentId("S211");
        contextualPropertyL2111.setRedText(true);
        categoryDocumentL2111.setContextualProperties(Collections.singletonList(contextualPropertyL2111));

        CategoryDocument categoryDocumentL2112 = categoryDocumentMap.get("S2112");
        ContextualProperty contextualPropertyL2112 = new ContextualProperty();
        contextualPropertyL2112.setParentId("S211");
        contextualPropertyL2112.setRedText(true);
        categoryDocumentL2112.setContextualProperties(Collections.singletonList(contextualPropertyL2112));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getId(), is("S2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).isRedText(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getId(), is("S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).isRedText(), is(true));
    }

    @Test

    public void shouldBuildLeftNavDocument_setRedTextToFalse_whenContextualPropertiesRedTextIsUnavailable() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S2111"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).getId(), is("S2111"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(0).isRedText(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).getId(), is("S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).isSelected(), is(false));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getCategories().get(1).isRedText(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_expandChildren_whenExpandChildrenIsTrue() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("S3212");
        categoryDocumentL3212.setExpandCategory(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getReferenceIds(), containsInAnyOrder("S1", "S2", "S3", "S31", "S32", "S321", "S322", "S323", "S3211", "S3212", "S32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getId(), is("S321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getId(), is("S3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getCategories().get(0).getId(), is("S32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getCategories().get(0).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontExpandChildren_whenExpandChildrenIsFalse() {
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getId(), is("S321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getId(), is("S3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_expandChildren_whenExpandChildrenIsTrueAndDontShowChildrenSetToTrue() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("S3212");
        categoryDocumentL3212.setExpandCategory(true);
        categoryDocumentL3212.setDontShowChildren(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getId(), is("S321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getId(), is("S3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getCategories().get(0).getId(), is("S32121"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getCategories().get(0).isSelected(), is(false));
    }

    @Test
    public void shouldBuildLeftNavDocument_dontExpandNode_whenNodeExpandChildrenIsTrueButTheNodeNotInPath() {
        CategoryDocument categoryDocumentL3212 = categoryDocumentMap.get("S3212");
        categoryDocumentL3212.setExpandCategory(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S3211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(0).getId(), is("S3211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getId(), is("S3212"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().get(1).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_includeDriveToPath_whenDriveToSubCategoryPathIsAvailable() {
        CategoryDocument categoryDocumentL211 = categoryDocumentMap.get("S211");
        ContextualProperty contextualPropertyL211 = new ContextualProperty();
        contextualPropertyL211.setParentId("S21");
        contextualPropertyL211.setDriveToSubcategoryId("S2112");
        categoryDocumentL211.setContextualProperties(Collections.singletonList(contextualPropertyL211));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S211"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);

        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getDriveToPath(), is("S_S2_S21_S211_S2112"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).getId(), is("S211"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getCategories().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_useDontShowChildren_whenItsTrue() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("S322");
        categoryDocumentL322.setDontShowChildren(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).getId(), is("S31"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getId(), is("S32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().size(), is(3));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getId(), is("S321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getId(), is("S322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).getId(), is("S323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(2).getCategories().size(), is(0));

    }

    public void shouldBuildLeftNavDocument_includeExcludeCountries_whenItsAvailable() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("S322");
        categoryDocumentL322.setExcludedCountries(Arrays.asList("US", "IN"));

        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("S321");
        categoryDocumentL321.setExcludedCountries(Arrays.asList("AU", "AE"));

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getId(), is("S31"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getId(), is("S32"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().size(), is(3));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getId(), is("S321"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getCategories().size(), is(0));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(0).getExcludedCountries(), containsInAnyOrder("AU", "AE"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getId(), is("S322"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getCategories().size(), is(2));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).isSelected(), is(true));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(1).getExcludedCountries(), containsInAnyOrder("US", "IN"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getId(), is("S323"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getExcludedCountries(), is(nullValue()));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(1).getCategories().get(2).getCategories().size(), is(0));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingLeftNavImageAvailable_whenItsTrue() {
        CategoryDocument categoryDocumentL322 = categoryDocumentMap.get("S322");
        categoryDocumentL322.setLeftNavImageAvailable(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S322"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is("/category/S322/r_navaux.html"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_usingLeftNavImageAvailableAndOverrides_whenItsTrue() {
        CategoryDocument categoryDocumentL321 = categoryDocumentMap.get("S321");
        categoryDocumentL321.setLeftNavImageAvailableOverride("override");
        categoryDocumentL321.setLeftNavImageAvailable(true);

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S321"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        List<LeftNavDocument> leftNavDocuments = allLeftNavDocuments.get(0);
        assertThat(leftNavDocuments.size(), is(1));
        assertThat(leftNavDocuments.get(0).getRefreshablePath(), is("/category/override/r_navaux.html"));
        assertThat(leftNavDocuments.get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).isSelected(), is(true));
    }

    @Test
    public void shouldBuildLeftNavDocument_whenOneOrMoreChildrenMissing() {
        when(categoryRepository.getCategoryDocument(anyString())).thenReturn(null);

        categoryDocumentMap.remove("S322");

        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S32"), false);

        verify(leftNavBatchWriter).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(1));

        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().size(), is(1));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(0).getId(), is("S321"));
        assertThat(allLeftNavDocuments.get(0).get(0).getLeftNav().get(0).getCategories().get(2).getCategories().get(1).getCategories().get(1).getId(), is("S323"));
        assertThat(allLeftNavDocuments.get(0).get(0).getReferenceIds(), containsInAnyOrder("S322", "S321", "S32", "S31", "S323", "S3", "S2", "S1"));
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagHiddenTrueForSelectedNode() {
        categoryDocumentMap.get("S3212").setHidden(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagNoResultsTrueForSelectedNode() {
        categoryDocumentMap.get("S3212").setNoResults(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldNotBuildLeftNavDocument_whenFlagDeletedTrueForSelectedNode() {
        categoryDocumentMap.get("S3212").setDeleted(true);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S3212"), false);

        verify(leftNavBatchWriter, times(0)).saveLeftNavDocuments(Collections.emptyList());
    }

    @Test
    public void shouldRebuildLeftNavDocument_whenRetryNodeHasMissingKey() {
        CategoryDocument L323 = categoryDocumentMap.get("S323");
        categoryDocumentMap.remove("S323");
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S32"), false);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S321"), false);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S322"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(1));
        assertThat(leftNavTreeProcessor.getRetryNodes().get("S323"), is(containsInAnyOrder("S32", "S321", "S322")));

        categoryDocumentMap.put("S323", L323);
        leftNavTreeProcessor.startByEvent(categoryDocumentMap.get("S323"), false);

        assertThat(leftNavTreeProcessor.getRetryNodes().size(), is(0));

        verify(leftNavBatchWriter, times(4)).saveLeftNavDocuments(leftNavDocumentsCaptor.capture());

        List<List<LeftNavDocument>> allLeftNavDocuments = leftNavDocumentsCaptor.getAllValues();
        assertThat(allLeftNavDocuments.size(), is(4));
        assertThat(allLeftNavDocuments.get(3).size(), is(4));
    }

    private Map<String, CategoryDocument> getCategoryDocumentsForStageTree() {
        Map<String, CategoryDocument> categoryDocumentMap = new HashMap<>();

        categoryDocumentMap.put("S", CategoryDocument.builder().id("S").name("S").children(Arrays.asList("S1", "S2", "S3")).build());

        Map<String, Integer> l1 = new HashMap<>();
        l1.put("S", 0);
        categoryDocumentMap.put("S1", CategoryDocument.builder().id("S1").name("S1").children(Arrays.asList("S11", "S12", "S13")).parents(l1).build());

        Map<String, Integer> l2 = new HashMap<>();
        l2.put("S", 0);
        categoryDocumentMap.put("S2", CategoryDocument.builder().id("S2").name("S2").children(Collections.singletonList("S21")).parents(l2).build());

        Map<String, Integer> l21 = new HashMap<>();
        l21.put("S2", 0);
        categoryDocumentMap.put("S21", CategoryDocument.builder().id("S21").name("S21").children(Collections.singletonList("S211")).parents(l21).build());

        Map<String, Integer> m211 = new HashMap<>();
        m211.put("S21", 0);
        categoryDocumentMap.put("S211", CategoryDocument.builder().id("S211").name("S211").children(Arrays.asList("S2111", "S2112")).parents(m211).build());

        Map<String, Integer> m2111 = new HashMap<>();
        m2111.put("S211", 0);
        categoryDocumentMap.put("S2111", CategoryDocument.builder().id("S2111").name("S2111").parents(m2111).build());

        Map<String, Integer> m2112 = new HashMap<>();
        m2112.put("S211", 0);
        categoryDocumentMap.put("S2112", CategoryDocument.builder().id("S2112").name("S2112").parents(m2112).build());

        Map<String, Integer> l21121 = new HashMap<>();
        l21121.put("S2112", 0);
        categoryDocumentMap.put("S21121", CategoryDocument.builder().id("S21121").name("S21121").parents(l21121).build());

        Map<String, Integer> l3 = new HashMap<>();
        l3.put("S", 0);
        categoryDocumentMap.put("S3", CategoryDocument.builder().id("S3").name("S3").children(Arrays.asList("S31", "S32")).parents(l3).build());

        Map<String, Integer> l11 = new HashMap<>();
        l11.put("S1", 0);
        categoryDocumentMap.put("S11", CategoryDocument.builder().id("S11").name("S11").parents(l11).build());

        Map<String, Integer> l12 = new HashMap<>();
        l12.put("S1", 0);
        categoryDocumentMap.put("S12", CategoryDocument.builder().id("S12").name("S12").parents(l12).build());

        Map<String, Integer> l13 = new HashMap<>();
        l13.put("S1", 0);
        categoryDocumentMap.put("S13", CategoryDocument.builder().id("S13").name("S13").children(Collections.singletonList("S3221")).parents(l13).build());

        Map<String, Integer> l31 = new HashMap<>();
        l31.put("S3", 0);
        categoryDocumentMap.put("S31", CategoryDocument.builder().id("S31").name("S31").parents(l31).build());

        Map<String, Integer> l32 = new HashMap<>();
        l32.put("S3", 0);
        categoryDocumentMap.put("S32", CategoryDocument.builder().id("S32").name("S32").children(Arrays.asList("S321", "S322", "S323")).parents(l32).build());

        Map<String, Integer> l321 = new HashMap<>();
        l321.put("S32", 0);
        categoryDocumentMap.put("S321", CategoryDocument.builder().id("S321").name("S321").children(Arrays.asList("S3211", "S3212")).parents(l321).build());

        Map<String, Integer> l3211 = new HashMap<>();
        l3211.put("S321", 0);
        categoryDocumentMap.put("S3211", CategoryDocument.builder().id("S3211").name("S3211").parents(l3211).build());

        Map<String, Integer> l3212 = new HashMap<>();
        l3212.put("S321", 0);
        categoryDocumentMap.put("S3212", CategoryDocument.builder().id("S3212").name("S3212").children(Collections.singletonList("S32121")).parents(l3212).build());

        Map<String, Integer> l32121 = new HashMap<>();
        l32121.put("S3212", 0);
        categoryDocumentMap.put("S32121", CategoryDocument.builder().id("S32121").name("S32121").parents(l32121).build());

        Map<String, Integer> l322 = new HashMap<>();
        l322.put("S32", 0);
        categoryDocumentMap.put("S322", CategoryDocument.builder().id("S322").name("S322").children(Arrays.asList("S3221", "S3222")).parents(l322).build());

        Map<String, Integer> l323 = new HashMap<>();
        l323.put("S32", 0);
        categoryDocumentMap.put("S323", CategoryDocument.builder().id("S323").name("S323").parents(l323).build());

        Map<String, Integer> l3221 = new HashMap<>();
        l3221.put("S322", 0);
        l3221.put("S13", 0);
        categoryDocumentMap.put("S3221", CategoryDocument.builder().id("S3221").name("S3221").parents(l3221).build());

        Map<String, Integer> l3222 = new HashMap<>();
        l3222.put("S322", 0);
        categoryDocumentMap.put("S3222", CategoryDocument.builder().id("S3222").name("S3222").parents(l3222).build());

        return categoryDocumentMap;
    }
}