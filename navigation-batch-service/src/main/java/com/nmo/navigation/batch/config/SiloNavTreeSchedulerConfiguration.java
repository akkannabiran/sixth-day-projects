package com.sixthday.navigation.batch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.batch.io.SiloNavTreeWriter;
import com.sixthday.navigation.batch.vo.SiloNavTreeProcessorResponse;
import com.sixthday.navigation.batch.vo.SiloNavTreeReaderResponse;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.sixthday.sixthdayLogging.EventType.ATG_API;
import static com.sixthday.sixthdayLogging.OperationType.SILO_SCHEDULER;
import static com.sixthday.sixthdayLogging.logError;
import static com.sixthday.sixthdayLogging.logOperation;

@Configuration
@EnableScheduling
@Slf4j
public abstract class SiloNavTreeSchedulerConfiguration {

    @Autowired
    @Qualifier(value = "mobileNavBatchConfig")
    NavigationBatchServiceConfig.BatchConfig mobileBatchConfig;

    @Autowired
    @Qualifier("desktopNavBatchConfig")
    NavigationBatchServiceConfig.BatchConfig desktopBatchConfig;
    @Autowired
    List<String> countryCodes;

    @Autowired(required = false)
    List<String> navKeyGroups;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired(required = false)
    @Qualifier("S3SiloNavTreeWriter")
    SiloNavTreeWriter s3SiloNavTreeWriter;

    abstract void readSiloNavTree(String countryCode, String navKeyGroup);

    abstract void processSiloNavTree(SiloNavTreeReaderResponse siloNavTreeReaderResponse);

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2);
        return threadPoolTaskScheduler;
    }

    void executeScheduledTask() {
        logOperation(log, ATG_API, SILO_SCHEDULER, () -> {
            Set<String> groups = new TreeSet<>(CollectionUtils.isEmpty(navKeyGroups) ? Collections.singleton("") : navKeyGroups);
            for (String countryCode : countryCodes) {
                for (String navKeyGroup : groups) {
                    try {
                        log.debug("Fetching SiloNavTree for country=\"{}\", group=\"{}\"", countryCode, navKeyGroup);
                        readSiloNavTree(countryCode, navKeyGroup);
                    } catch (Exception e) {
                        logError(log, null, SILO_SCHEDULER,
                                "Retrieving silo failed for country " + countryCode + ", message " + e.getMessage(), e);
                    }
                }
            }
            return null;
        });
    }

    void writeSiloNavTree(SiloNavTreeProcessorResponse siloNavTreeProcessorResponse) {
        s3SiloNavTreeWriter.write(siloNavTreeProcessorResponse);
    }
}