package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.batch.vo.CategoryDocuments;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryDocumentReaderTest {
    private int numberOfDocuments = 1;

    @Mock
    private CategoryRepository categoryRepo;

    @InjectMocks
    private CategoryDocumentReader categoryDocumentReader;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Before
    public void setUp() {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setId("catId1");

        List<CategoryDocument> expectedCategoryDocumentList = Collections.singletonList(categoryDocument);

        CategoryDocuments categoryDocuments = new CategoryDocuments("1234", expectedCategoryDocumentList);

        when(categoryRepo.getCategoryDocuments(anyInt())).thenReturn(categoryDocuments);
        when(categoryRepo.getCategoryDocuments("1234")).thenReturn(new CategoryDocuments("end", new ArrayList<>()));
        when(categoryRepo.getCategoryDocuments("123456")).thenReturn(categoryDocuments);

        when(navigationBatchServiceConfig.getLeftNavBatchConfig().getNumberOfDocuments()).thenReturn(numberOfDocuments);
    }

    @Test
    @SneakyThrows
    public void shouldFetchCategoryDocsGivenUpdatedTimeAndDocCount() {
        CategoryDocuments actualCategoryDocuments = categoryDocumentReader.read(numberOfDocuments);
        List<CategoryDocument> actualCategoryDocumentList = actualCategoryDocuments.getCategoryDocumentList();
        assertThat(actualCategoryDocumentList.get(0).getId(), is("catId1"));
    }

    @Test
    @SneakyThrows
    public void shouldFetchCategoryDocumentWhenScrollIdIsGiven() {
        CategoryDocuments actualCategoryDocuments = categoryDocumentReader.read("123456");
        List<CategoryDocument> actualCategoryDocumentList = actualCategoryDocuments.getCategoryDocumentList();
        assertThat(actualCategoryDocumentList.get(0).getId(), is("catId1"));
    }

    @Test
    public void shouldFetchAllCategoryDocuments() {
        Map<String, CategoryDocument> allCategories = categoryDocumentReader.getAllCategories();
        assertThat(allCategories.size(), is(1));
    }
}

