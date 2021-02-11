package com.sixthday.category.bootstrap.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class CookieUtils {
    private CookieUtils() {
    }

    public static Map<String, String> getCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieMap = new HashMap<>();
        if (cookies == null) {
            return cookieMap;
        }
        Arrays.stream(cookies).forEach(c -> cookieMap.put(c.getName(), c.getValue()));
        return cookieMap;
    }
}
