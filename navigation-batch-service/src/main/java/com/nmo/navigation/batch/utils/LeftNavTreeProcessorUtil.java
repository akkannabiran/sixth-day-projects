package com.sixthday.navigation.batch.utils;

import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import org.apache.commons.lang.StringUtils;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class LeftNavTreeProcessorUtil {

    private static final String CATEGORY_ID_DELIMITER = "_";
    private static final String PATH_DELIMITER = "/";
    private static final String HYPHEN = "-";

    private static String updateUrl(String navPath, Map<String, CategoryDocument> categoryDocumentMap) {
        List<String> categoryIds = Arrays.asList(navPath.split("_"));
        Collections.reverse(categoryIds);
        return new StringBuilder().append(categoryDocumentMap.get(categoryIds.get(0)).getCanonicalUrl()).append("?navpath=").append(navPath).append("&source=leftNav").toString();
    }

    public String getUrl(String navpath, CategoryDocument currentCategoryDocument, Map<String, CategoryDocument> categoryDocumentMap) {
        if (StringUtils.isEmpty(currentCategoryDocument.getCanonicalUrl())) {
            return buildUrl(navpath, categoryDocumentMap);
        } else {
            return updateUrl(navpath, categoryDocumentMap);
        }
    }

    private String buildUrl(String navPath, Map<String, CategoryDocument> categoryDocumentMap) {
        List<String> categoryIds = Arrays.asList(navPath.split("_"));
        Collections.reverse(categoryIds);
        int index = 0;

        StringJoiner urlSecondPart = new StringJoiner("_");
        StringBuilder urlFirstPart = new StringBuilder();

        for (String categoryId : categoryIds) {
            CategoryDocument categoryDocument = categoryDocumentMap.get(categoryId);
            int currentIndex = categoryIds.indexOf(categoryId);
            String parentId = categoryIds.get((currentIndex + 1) < categoryIds.size() ? (currentIndex + 1) : 0);
            ContextualProperty contextualProperty = categoryDocument.getApplicablePropertiesForCategory(parentId);
            urlFirstPart.insert(0, "/").insert(0, replaceSpecialCharacters(categoryDocument.getCategoryName(contextualProperty)));
            urlSecondPart.add(categoryDocument.getId());
            if (++index == 3)
                break;
        }

        return "/" + urlFirstPart.append(urlSecondPart.toString()).append("/c.cat?").append("navpath=").append(navPath).append("&source=leftNav").toString();
    }

    private String replaceSpecialCharacters(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }

        String outputName = Normalizer.normalize(name.trim(), Normalizer.Form.NFD);
        final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        outputName =
                pattern.matcher(outputName).replaceAll("")
                        .replaceAll("\u00E6", "a")
                        .replaceAll("(?<=[0-9])(?:( |)(oz|ounces|OZ|Ounces))", "oz")
                        .replaceAll("(?<=[0-9])(?:( |)(\u00B0|degrees|degree|deg))", "deg")
                        .replaceAll("(?<=[0-9])( |)(X|x)( |)(?=[0-9])", "x")
                        .replaceAll("(?<=[0-9])(?:( |)(')( |))(?=[0-9])", "ft")
                        .replaceAll("(?<=[0-9])(?:( |)(\"))", "in")
                        .replaceAll("(?<=[3])( |)(\\W+)( |)(?=[D|d])", "")
                        .replaceAll("(\\u2018\u2019)", "'")
                        .replaceAll("(?<=[a-zA-Z])\\.(?=[ a-zA-Z])", "")
                        .replaceAll(CATEGORY_ID_DELIMITER, HYPHEN)
                        .replaceAll("\\+", HYPHEN)
                        .replaceAll(PATH_DELIMITER, HYPHEN)
                        .replaceAll(" ", HYPHEN)
                        .replaceAll("[^a-zA-Z0-9-_$.]", "")
                        .replaceAll("[-]{2,}", HYPHEN)
                        .replaceAll("/-", PATH_DELIMITER);

        return outputName;
    }
}