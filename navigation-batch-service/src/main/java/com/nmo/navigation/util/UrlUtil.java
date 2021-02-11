package com.sixthday.navigation.util;

import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static org.springframework.util.StringUtils.isEmpty;

public class UrlUtil {

    private UrlUtil() {
    }

    @SneakyThrows
    public static String appendQueryParameterToUrl(String id, String url, String key, String value) {
        String questionMark = "?";
        String hashTag = "#";
        if (isEmpty(url) || hashTag.equals(id)) {
            return url;
        }
        String queryParameter = key + "=" + value;
        if (url.endsWith(questionMark)) {
            return url + queryParameter;
        }
        return url.contains(questionMark) ? url + "&" + queryParameter : url + questionMark + queryParameter;
    }

    public static Optional<String> getLastCategoryId(final String navPath) {
        String underscore = "_";
        if (StringUtils.isEmpty(navPath) || navPath.endsWith(underscore)) {
            return Optional.empty();
        } else {
            String[] ids = navPath.split(underscore);
            return Optional.ofNullable(ids[ids.length - 1]);
        }
    }
}
