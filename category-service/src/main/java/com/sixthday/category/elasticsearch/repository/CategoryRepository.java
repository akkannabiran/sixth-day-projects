package com.sixthday.category.elasticsearch.repository;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class CategoryRepository {

    private ESCategoryDocumentRepository esCategoryDocumentRepository;

    @Autowired
    public CategoryRepository(ESCategoryDocumentRepository esCategoryDocumentRepository) {
        this.esCategoryDocumentRepository = esCategoryDocumentRepository;
    }

    public CategoryDocument getCategoryDocument(final String categoryId) {
        return esCategoryDocumentRepository.getCategoryDocument(categoryId);
    }

    public List<CategoryDocument> getCategoryDocuments(final Set<String> categoryIds) {
        return esCategoryDocumentRepository.getCategoryDocuments(categoryIds);
    }
}
