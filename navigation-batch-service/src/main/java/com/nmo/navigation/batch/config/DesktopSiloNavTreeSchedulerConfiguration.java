package com.sixthday.navigation.batch.config;

import com.sixthday.navigation.batch.io.SiloNavTreeReader;
import com.sixthday.navigation.batch.processor.DesktopSiloNavTreeProcessor;
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
public class DesktopSiloNavTreeSchedulerConfiguration extends SiloNavTreeSchedulerConfiguration {

    private final DesktopSiloNavTreeProcessor desktopSiloNavTreeProcessor;

    @Autowired
    public DesktopSiloNavTreeSchedulerConfiguration(DesktopSiloNavTreeProcessor desktopSiloNavTreeProcessor) {
        this.desktopSiloNavTreeProcessor = desktopSiloNavTreeProcessor;
    }

    @Scheduled(cron = "${navigation.integration.desktop.cron}")
    public void scheduledTask() {
        executeScheduledTask();
    }

    @Override
    public void readSiloNavTree(String countryCode, String navKeyGroup) {
        SiloNavTreeReader siloNavTreeReader = new SiloNavTreeReader(
                desktopBatchConfig.getUrl(),
                countryCode,
                desktopBatchConfig.getUserAgent(),
                navKeyGroup,
                desktopRestTemplate());

        processSiloNavTree(siloNavTreeReader.read());
    }

    @Override
    public void processSiloNavTree(SiloNavTreeReaderResponse siloNavTreeReaderResponse) {
        writeSiloNavTree(desktopSiloNavTreeProcessor.process(siloNavTreeReaderResponse, objectMapper));
    }

    @Bean(name = "DesktopRestTemplate")
    protected RestTemplate desktopRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(desktopBatchConfig.getReadTimeOut());
        factory.setConnectTimeout(desktopBatchConfig.getConnectTimeOut());
        return new RestTemplate(factory);
    }
}