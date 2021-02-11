package com.sixthday.store.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RedirectFilterTest {

    RedirectFilter filter;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletRequest request;
    @Mock
    private ServletResponse response;
    @Mock
    private FilterChain chain;

    @Before
    public void setup() {
        filter = new RedirectFilter();
    }

    @Test
    public void shouldForwardToNewUrlIfRequestURLStartsWithStoresAndNotCallChainDoFilter() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/stores/stores");
        filter.doFilter(request, response, chain);
        verify(request).getRequestDispatcher("/stores");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    public void shouldNotForwardToNewUrlIfRequestURLDoesNotHaveStores() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/store");
        filter.doFilter(request, response, chain);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    public void shouldNotForwardToNewUrlIfRequestURLDoesNotStartWithStores() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/newStores/stores/stores");
        filter.doFilter(request, response, chain);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    public void shouldNotForwardIfRequestURIIsNull() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn(null);
        filter.doFilter(request, response, chain);
        verify(request, never()).getRequestDispatcher(anyString());
    }
}