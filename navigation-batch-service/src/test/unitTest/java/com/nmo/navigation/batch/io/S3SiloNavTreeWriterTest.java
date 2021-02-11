package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.batch.vo.SiloNavTreeProcessorResponse;
import com.sixthday.navigation.domain.SiloNavTree;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import com.sixthday.navigation.util.AmazonS3ClientUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class S3SiloNavTreeWriterTest {
    @InjectMocks
    private S3SiloNavTreeWriter s3SiloNavTreeWriter;

    @Mock
    private AmazonS3ClientUtil amazonS3ClientUtil;

    @Before
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMockCreation() {
        assertNotNull(s3SiloNavTreeWriter);
        assertNotNull(amazonS3ClientUtil);
    }

    @Test
    public void shouldSaveSimpleNavTree() {
        SiloNavTreeProcessorResponse navigationTreeData = new SiloNavTreeProcessorResponse("US", "mobile_simple", "{ test_json: true }", null);

        s3SiloNavTreeWriter.write(navigationTreeData);

        ArgumentCaptor<SiloNavTree> argCaptor = ArgumentCaptor.forClass(SiloNavTree.class);
        verify(amazonS3ClientUtil).uploadObject(argCaptor.capture());
        SiloNavTree actual = argCaptor.getValue();
        assertThat(actual.getId(), equalTo("US_mobile_simple"));
        assertThat(actual.getSiloData(), equalTo("{ test_json: true }"));
    }

    @Test(expected = NavigationBatchServiceException.class)
    public void shouldThrow_whenNavigationTreeDataIsNull() {
        s3SiloNavTreeWriter.write(null);
    }

    @Test
    public void shouldSaveSimpleNavTreeWithoutGroupKeyWhenNavKeyGroupIsEmpty() {
        SiloNavTreeProcessorResponse navigationTreeData = new SiloNavTreeProcessorResponse("US", "DEVICE", "{ test_json: true }", "");
        s3SiloNavTreeWriter.write(navigationTreeData);
        ArgumentCaptor<SiloNavTree> argCaptor = ArgumentCaptor.forClass(SiloNavTree.class);
        verify(amazonS3ClientUtil).uploadObject(argCaptor.capture());
        SiloNavTree actual = argCaptor.getValue();
        assertThat(actual.getId(), equalTo("US_DEVICE"));
        assertThat(actual.getSiloData(), equalTo("{ test_json: true }"));
    }

    @Test
    public void shouldSaveSimpleNavTreeWithoutGroupKeyWhenNavKeyGroupIsA() {
        SiloNavTreeProcessorResponse navigationTreeData = new SiloNavTreeProcessorResponse("US", "DEVICE", "{ test_json: true }", "A");
        s3SiloNavTreeWriter.write(navigationTreeData);
        ArgumentCaptor<SiloNavTree> argCaptor = ArgumentCaptor.forClass(SiloNavTree.class);
        verify(amazonS3ClientUtil).uploadObject(argCaptor.capture());
        SiloNavTree actual = argCaptor.getValue();
        assertThat(actual.getId(), equalTo("US_DEVICE"));
        assertThat(actual.getSiloData(), equalTo("{ test_json: true }"));
    }

    @Test
    public void shouldSaveSimpleNavTreeWithGroupKeyWhenNavKeyGroupIsB() {
        SiloNavTreeProcessorResponse navigationTreeData = new SiloNavTreeProcessorResponse("US", "DEVICE", "{ test_json: true }", "B");
        s3SiloNavTreeWriter.write(navigationTreeData);
        ArgumentCaptor<SiloNavTree> argCaptor = ArgumentCaptor.forClass(SiloNavTree.class);
        verify(amazonS3ClientUtil).uploadObject(argCaptor.capture());
        SiloNavTree actual = argCaptor.getValue();
        assertThat(actual.getId(), equalTo("US_DEVICE_B"));
        assertThat(actual.getSiloData(), equalTo("{ test_json: true }"));
    }
}
