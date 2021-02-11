package com.sixthday.navigation.api.elasticsearch.repository;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({RestHighLevelClient.class, NavigationServiceConfig.class})
public class LeftNavRepositoryTest {

    @InjectMocks
    private LeftNavRepository leftNavRepository;
    @Mock
    private ESLeftNavRepository esLeftNavRepository;

    @Test
    public void testGetCategoryDocumentCallsESAndReturnValidResponse() {
        when(esLeftNavRepository.getLeftNavDocument(anyString())).thenReturn(new LeftNavDocument());
        leftNavRepository.getLeftNavDocument("someCatId");
        verify(esLeftNavRepository).getLeftNavDocument(anyString());
    }
}