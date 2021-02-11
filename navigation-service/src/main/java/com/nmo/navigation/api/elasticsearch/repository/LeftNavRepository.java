package com.sixthday.navigation.api.elasticsearch.repository;

import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class LeftNavRepository {

    private ESLeftNavRepository esLeftNavRepository;

    @Autowired
    public LeftNavRepository(ESLeftNavRepository esLeftNavRepository) {
        this.esLeftNavRepository = esLeftNavRepository;
    }

    public LeftNavDocument getLeftNavDocument(final String navPath) {
        return esLeftNavRepository.getLeftNavDocument(navPath);
    }
}
