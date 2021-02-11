package com.sixthday.navigation.batch.config;

import com.sixthday.navigation.batch.io.SiloNavTreeReader;
import com.sixthday.navigation.batch.processor.MobileSiloNavTreeProcessor;
import com.sixthday.navigation.batch.vo.SiloNavTreeReaderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling
public class MobileSiloNavTreeSchedulerConfiguration extends SiloNavTreeSchedulerConfiguration {

    private final MobileSiloNavTreeProcessor mobileSiloNavTreeProcessor;

    @Autowired
    public MobileSiloNavTreeSchedulerConfiguration(MobileSiloNavTreeProcessor mobileSiloNavTreeProcessor) {
        this.mobileSiloNavTreeProcessor = mobileSiloNavTreeProcessor;
    }

    @Scheduled(cron = "${navigation.integration.mobile.cron}")
    public void scheduledTask() {
        executeScheduledTask();
    }

    @Override
    public void readSiloNavTree(String countryCode, String navKeyGroup) {
        SiloNavTreeReader siloNavTreeReader = new SiloNavTreeReader(
                mobileBatchConfig.getUrl(),
                countryCode,
                mobileBatchConfig.getUserAgent(),
                navKeyGroup,
                mobileRestTemplate());

        processSiloNavTree(siloNavTreeReader.read());

    }

    @Override
    public void processSiloNavTree(SiloNavTreeReaderResponse siloNavTreeReaderResponse) {
        writeSiloNavTree(mobileSiloNavTreeProcessor.process(siloNavTreeReaderResponse, objectMapper));
    }

    @Bean(name = "MobileRestTemplate")
    protected RestTemplate mobileRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(mobileBatchConfig.getReadTimeOut());
        factory.setConnectTimeout(mobileBatchConfig.getConnectTimeOut());
        return new RestTemplate(factory);
    }
