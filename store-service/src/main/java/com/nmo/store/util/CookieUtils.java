package com.sixthday.store.util;

import org.apache.commons.lang.ArrayUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CookieUtils {
    public static Map<String, String> getCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieMap = new HashMap<>();
        if(ArrayUtils.isEmpty(cookies)) {
            return cookieMap;
        }
        Arrays.stream(cookies).forEach(c -> cookieMap.put(c.getName(), c.getValue()));
        return cookieMap;
    }
}
