package com.sixthday.store.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class CookieUtilsTest {
  @Mock
  HttpServletRequest request;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldReturnEmptyMapIfRequestHasNoCookies() {
    when(request.getCookies()).thenReturn(null);
    assertThat(CookieUtils.getCookies(request), is(Collections.emptyMap()));
    when(request.getCookies()).thenReturn(new Cookie[]{});
    assertThat(CookieUtils.getCookies(request), is(Collections.emptyMap()));
  }

  @Test
  public void shouldReturnMapofCookiesIfRequestHasCookies() {
    Cookie bops = new Cookie("BOPS", "true");
    Cookie bops2 = new Cookie("BOPS2", "true");
    Cookie[] cookies = {bops, bops2};
    Map<String, String> cookieMap = new HashMap<>();
    cookieMap.put("BOPS", "true");
    cookieMap.put("BOPS2", "true");
    when(request.getCookies()).thenReturn(cookies);
    assertThat(CookieUtils.getCookies(request), is(cookieMap));
  }

  @Test
  public void shouldReturnAMapOfCookiesWhenThereDuplicateCookies() {
    HashMap<String, String> expectedMap = new HashMap<String, String>() {
      private static final long serialVersionUID = 21475536733436906L;

      {
        put("foo", "bar");
        put("AGA", "GroupId:TestId2");
      }
    };
    when(request.getCookies()).thenReturn(new Cookie[] {
            new Cookie("foo", "bar"),
            new Cookie("AGA", null),
            new Cookie("AGA", "GroupId:TestId"),
            new Cookie("AGA", "GroupId:TestId2")});

    assertThat(CookieUtils.getCookies(request), is(expectedMap));
  }
}
