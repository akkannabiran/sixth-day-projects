package com.sixthday.navigation.api.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sixthday.navigation.api.executors.AsyncServiceRequest;
import com.sixthday.navigation.config.Constants;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class CommonNavService {

    @Autowired
    private final AsyncServiceRequest asyncServiceRequest;
    private final SiloNavTreeService siloNavTreeService;
    private final BrandLinksService brandLinksService;

    @Autowired
    public CommonNavService(final AsyncServiceRequest asyncServiceRequest, final SiloNavTreeService siloNavTreeService, final BrandLinksService brandLinksService) {
        this.asyncServiceRequest = asyncServiceRequest;
        this.siloNavTreeService = siloNavTreeService;
        this.brandLinksService = brandLinksService;
    }


    @SneakyThrows
    public Map<String, Object> getNavDetails(Map<String, String> navRequestMap, final String navKeyGroup) {
        Map<String, Object> navOutputMap = new HashMap<>();
        navRequestMap.forEach((navRequest, navEndPoint) -> {
            CompletableFuture<Object> navContentFuture = asyncServiceRequest.createRequest(() ->
                    getNavResponse(navRequest, navEndPoint.split("/"), navKeyGroup));
            try {
                navOutputMap.put(navRequest, navContentFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Fail to get navigation response for given Nav request map.", e);
            }
        });
        return navOutputMap;
    }

    @SneakyThrows
    public Object getNavResponse(String navRequest, String[] navEndPoints, final String navKeyGroup) {
        Object contents = new Object();
        switch (navRequest) {
            case Constants.DESKTOP_NAV:
            case Constants.MOBILE_NAV:
                contents = siloNavTreeService.getSilos(navEndPoints[2], navEndPoints[3], navKeyGroup);
                break;
            case Constants.MOBILE_NAV_INITIAL:
                contents = siloNavTreeService.getSilos(navEndPoints[2], Constants.MOBILE, navKeyGroup);
                break;
            case Constants.BRAND_LINKS:
                contents = brandLinksService.getBrandLinks();
                break;
            default:
                log.error("Fail to return nav response as navRequest is unrecognised.");
        }
        return contents;
    }

}
