package com.sixthday.navigation.api.utils;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.sixthday.navigation.config.Constants.TEST_CATEGORY_GROUP;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class BreadcrumbUtil {
    private static final String CATEGORY_ID_DELIMITER = "_";
    private static final String PATH_DELIMITER = "/";
    private static final String HYPHEN = "-";
    private static final String SOURCE_PARAMETER_NAME = "source";
    private static final String SOURCE_PARAMETER_VALUE = "leftNav";
    private static final String QUERY_CHARACTER = "?";
    private NavigationServiceConfig navigationServiceConfig;

    @Autowired
    public BreadcrumbUtil(final NavigationServiceConfig navigationServiceConfig) {
        this.navigationServiceConfig = navigationServiceConfig;
    }

    public List<String> convertToListAndRemoveRootCategory(final String categoryIds, String navKeyGroup) {
        List<String> list = new LinkedList<>(Arrays.asList(categoryIds.split("_")));
        list.remove(getRootCategoryId());
        if (TEST_CATEGORY_GROUP.equals(navKeyGroup)) {
          list = list.stream().map(this::getAlternateForDefault).collect(Collectors.toCollection(ArrayList::new));
        }
        return list;
    }

    public String getRootCategoryId() {
        return navigationServiceConfig.getCategoryConfig().getIdConfig().getLive();
    }
    
    public String getAlternateForDefault(String categoryId) {
      return navigationServiceConfig.getCategoryConfig().getAlternateDefaults().getOrDefault(categoryId, categoryId);
    }

    public String buildBreadcrumbUrl(List<CategoryDocument> categoryDocuments, final int index, Boolean hasLiveRootCatIdInNavpath) {
        if (categoryDocuments.isEmpty()) {
            return "";
        }
        StringBuilder url = new StringBuilder();
        url.append(categoryDocuments.get(index - 1).getCanonicalUrl());
        String categoryIds = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(
                        categoryDocuments.stream().limit(index).map(CategoryDocument::getId).collect(Collectors.toCollection(LinkedList::new)).descendingIterator(), Spliterator.ORDERED), false)
                .collect(Collectors.joining(CATEGORY_ID_DELIMITER));
        List<String> navpathIds = Arrays.asList(categoryIds.split(CATEGORY_ID_DELIMITER));
        Collections.reverse(navpathIds);
        String navpath = String.join(CATEGORY_ID_DELIMITER, navpathIds);
        url.append("?navpath=");
        if (hasLiveRootCatIdInNavpath) {
            url.append(getRootCategoryId()).append(CATEGORY_ID_DELIMITER);
        }
        url.append(navpath);
        return url.toString();
    }

    public String appendSourceParam(String url) {
        StringBuilder urlWithSourceParam = new StringBuilder();
        urlWithSourceParam.append(url);
        if (url.contains(QUERY_CHARACTER)) {
            List<String> splitUrls = Arrays.asList(url.split("\\" + QUERY_CHARACTER));
            if (splitUrls.size() > 1) {
                urlWithSourceParam.append("&" + SOURCE_PARAMETER_NAME + "=" + SOURCE_PARAMETER_VALUE);
            } else {
                urlWithSourceParam.append(SOURCE_PARAMETER_NAME + "=" + SOURCE_PARAMETER_VALUE);
            }
        } else {
            urlWithSourceParam.append(QUERY_CHARACTER + SOURCE_PARAMETER_NAME + "=" + SOURCE_PARAMETER_VALUE);
        }
        return urlWithSourceParam.toString();
    }

    protected String replaceSpecialCharacters(String name) {

        if (StringUtil.isNullOrEmpty(name)) {
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
