package com.sixthday.navigation.batch.vo;

import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryDocuments {
    private String scrollId;
    private List<CategoryDocument> categoryDocumentList;

}
