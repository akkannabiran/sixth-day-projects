package com.sixthday.navigation.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.models.Silos;
import com.sixthday.navigation.domain.CategoryNode;
import com.sixthday.navigation.repository.S3SiloNavTreeReader;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SiloNavTreeService {

    private static final String MOBILE_DEVICE_TYPE = "mobile";
    private static final int INITIAL_MOBILE_SILOS_MAX_LEVEL = 1;
    private static final String NAV_ID_FORMAT = "%s_%s";
    private static final String NAV_KEY_GROUP_CONTROL = "A";

    private S3SiloNavTreeReader s3SiloNavTreeReader;
    private NavigationServiceConfig navigationServiceConfig;
    private ObjectMapper objectMapper;

    @Autowired
    public SiloNavTreeService(final S3SiloNavTreeReader siloNavTreeLoader, NavigationServiceConfig navigationServiceConfig) {
        this.s3SiloNavTreeReader = siloNavTreeLoader;
        this.navigationServiceConfig = navigationServiceConfig;
        this.objectMapper = new ObjectMapper();
    }

    @SneakyThrows
    public Silos getSilos(final String countryCode, final String deviceType, final String navKeyGroup) {
        String siloData = s3SiloNavTreeReader.loadSiloNavTree(getNavKey(countryCode, deviceType, navKeyGroup));

        return objectMapper.readValue(siloData, Silos.class);
    }

    protected String getNavKey(final String countryCode, final String deviceType, final String navKeyGroup) {
      String navKey = String.format(NAV_ID_FORMAT, getResolvedCountryCode(countryCode).toUpperCase(), deviceType.toLowerCase());
      if (!StringUtils.isEmpty(navKeyGroup) && !NAV_KEY_GROUP_CONTROL.equals(navKeyGroup)) {
        navKey += "_"+navKeyGroup;
      }
      return navKey;
    }
    
    protected String getResolvedCountryCode(String countryCode) {
        return navigationServiceConfig.getIntegration().getCountryCodes().contains(countryCode) ? countryCode : "US";
    }

    @SneakyThrows
    public Silos getInitialMobileSilos(String countryCode, final String navKeyGroup) {
        final String siloData = s3SiloNavTreeReader.loadSiloNavTree(getNavKey(countryCode, MOBILE_DEVICE_TYPE, navKeyGroup));
        Silos silos = objectMapper.readValue(siloData, Silos.class);
        silos.getSilosTree().parallelStream().forEach(this::trimTree);
        return silos;
    }

    private void trimTree(CategoryNode node) {
        if (node.getLevel() >= INITIAL_MOBILE_SILOS_MAX_LEVEL) {
            node.setCategories(null);
        } else if (node.getCategories() != null) {
            node.getCategories().parallelStream().forEach(this::trimTree);
        }
    }
}
