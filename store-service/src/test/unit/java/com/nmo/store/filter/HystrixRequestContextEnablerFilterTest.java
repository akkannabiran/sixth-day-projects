package com.sixthday.store.filter;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;
import com.sixthday.store.hooks.RequestContextHystrixRequestVariable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificatiosixthdaydeFactory.times;
import static org.powermock.api.mockito.PowerMockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({HystrixRequestContext.class, HystrixRequestVariableDefault.class, RequestContextHystrixRequestVariable.class})
public class HystrixRequestContextEnablerFilterTest {

    private HystrixRequestContextEnablerFilter filter;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletRequest request;

    @Mock
    private ServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private RequestAttributes requestAttributes;
    @Mock
    private HystrixRequestVariableDefault hystrixRequestVariableDefault;
    @Mock
    private HystrixRequestContext hystrixRequestContext;

    @Before
    public void setUp() {
        filter = new HystrixRequestContextEnablerFilter();
        mockStatic(HystrixRequestContext.class);
        mockStatic(HystrixRequestVariableDefault.class);
        mockStatic(RequestContextHystrixRequestVariable.class);
    }

    @Test
    public void shouldInitializeHystrixRequestContext() throws Exception {
        when(RequestContextHystrixRequestVariable.getInstance()).thenReturn(hystrixRequestVariableDefault);
        when(HystrixRequestContext.initializeContext()).thenReturn(hystrixRequestContext);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyStatic(times(1));
        HystrixRequestContext.initializeContext();
        verifyNoMoreInteractions(HystrixRequestContext.class);
    }

    @Test
    public void shouldSetRequestContextHystrixRequestVariable() throws Exception {
        when(RequestContextHystrixRequestVariable.getInstance()).thenReturn(hystrixRequestVariableDefault);
        when(HystrixRequestContext.initializeContext()).thenReturn(hystrixRequestContext);

        RequestContextHolder.setRequestAttributes(requestAttributes);

        filter.doFilter(request, response, chain);

        verify(hystrixRequestVariableDefault).set(requestAttributes);
    }
}