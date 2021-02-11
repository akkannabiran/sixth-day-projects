package com.sixthday.navigation.api.services;

import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.mappers.BrandLinksMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.sixthday.navigation.api.data.CategoryTestDataFactory.brandLinksCategories;
import static com.sixthday.navigation.api.data.CategoryTestDataFactory.expectedBrandLinks;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrandLinksServiceTest {

    CategoryRepository categoryRepository;

    BrandLinksService brandLinksService;

    @Before
    public void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        brandLinksService = new BrandLinksService(categoryRepository, new BrandLinksMapper());
    }

    @Test
    public void shouldReturnListOfBrandLinks() {
        when(categoryRepository.getBrandLinks()).thenReturn(brandLinksCategories());

        assertEquals(expectedBrandLinks(), brandLinksService.getBrandLinks());

    }
}
