package com.sixthday.navigation.repository;

import com.sixthday.navigation.integration.utils.AmazonS3ClientUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class S3SiloNavTreeReaderTest {

    @InjectMocks
    S3SiloNavTreeReader loader;

    @Mock
    AmazonS3ClientUtil siloNavTreeReader;

    private String navigationTreeId = "US_desktop";

    @Test
    public void throwsNavigationTreeNotFoundException_WhenNoNavData() {
        given(siloNavTreeReader.getObject(navigationTreeId)).willReturn(null);

        assertTrue(null == loader.loadSiloNavTree(navigationTreeId));
    }

    @Test
    public void shouldLoadSiloData_whenNavTreeIdProvided() {
        given(siloNavTreeReader.getObject(navigationTreeId)).willReturn("silo data");

        final String actual = loader.loadSiloNavTree(navigationTreeId);

        assertThat(actual, is("silo data"));
    }
}
