package com.sixthday.category.api.models;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class CategoryDocumentInfo {
    private boolean useCategoryDocumentToBuildTheResponse;
    private String categoryId;
    private String parentCategoryId;
    private CategoryDocument categoryDocument;
    private CategoryDocument parentCategoryDocument;
    private boolean newAspectRatio;
}