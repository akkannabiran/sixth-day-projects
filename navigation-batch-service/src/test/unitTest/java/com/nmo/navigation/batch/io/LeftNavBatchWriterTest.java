package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import com.sixthday.navigation.elasticsearch.repository.LeftNavRepository;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LeftNavBatchWriterTest {

    @InjectMocks
    LeftNavBatchWriter leftNavBatchWriter;

    @Mock
    LeftNavRepository leftNavRepository;

    @Test
    public void testCallsRepositorySaveMethodWhenEverythingLooksGood() {
        leftNavBatchWriter.saveLeftNavDocuments(new ArrayList<>());
        verify(leftNavRepository).save(anyListOf(LeftNavDocument.class));
    }

    @Test
    public void testExceptionCaughtWhenSomethingGoesWrong() {
        doThrow(NavigationBatchServiceException.class).when(leftNavRepository).save(anyListOf(LeftNavDocument.class));
        leftNavBatchWriter.saveLeftNavDocuments(new ArrayList<>());
        verify(leftNavRepository).save(anyListOf(LeftNavDocument.class));
    }
}