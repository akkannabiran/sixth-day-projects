package com.sixthday.navigation.integration.services;

import com.sixthday.navigation.integration.repository.LeftNavSyncRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LeftNavSyncService {

    private LeftNavSyncRepository leftnavSyncRepository;

    @Autowired
    public LeftNavSyncService(final LeftNavSyncRepository leftnavSyncRepository) {
        this.leftnavSyncRepository = leftnavSyncRepository;
    }

    public void deleteLeftNavTreeForThatCategory(final String categoryId) {
        leftnavSyncRepository.fetchAndDeleteLeftNavDocument(categoryId);
    }
}
