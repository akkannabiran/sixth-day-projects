package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import com.sixthday.navigation.elasticsearch.repository.LeftNavRepository;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.sixthday.sixthdayLogging.OperationType.ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT;
import static com.sixthday.sixthdayLogging.logError;

@Component
@Slf4j
public class LeftNavBatchWriter {

    private final LeftNavRepository leftNavRepository;

    @Autowired
    public LeftNavBatchWriter(LeftNavRepository leftNavRepository) {
        this.leftNavRepository = leftNavRepository;
    }

    public void saveLeftNavDocuments(List<LeftNavDocument> leftNavDocument) {
        try {
            leftNavRepository.save(leftNavDocument);
        } catch (NavigationBatchServiceException e) {
            logError(log, null, ELASTICSEARCH_UPSERT_LEFTNAV_DOCUMENT, "Caught error while Insert/Update LeftNavDocuments into ES", e);
        }
    }
}
