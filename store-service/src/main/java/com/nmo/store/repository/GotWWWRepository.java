package com.sixthday.store.repository;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.store.config.GotWWWConfig;
import com.sixthday.store.exceptions.GotWWWCommunicationException;
import com.sixthday.store.exceptions.GotWWWResponseParseException;
import com.sixthday.store.exceptions.InvalidLocationException;
import com.sixthday.store.models.Coordinates;
import com.sixthday.store.models.StoreSearchLocation;
import com.sixthday.store.models.gotwww.GeoResponse;
import com.sixthday.store.models.gotwww.Store;
import com.sixthday.store.models.gotwww.Stores;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.sixthday.store.config.Constants.Actions.STORE_SEARCH_GOT_WWW;
import static com.sixthday.store.config.Constants.Events.REPOSITORY_EVENT;

@Repository
public class GotWWWRepository {
    private static final String GOT_WWW_HYSTRIX_KEY = "got-www-api";
    private static final String ACTION = "action=\"";
    private XmlMapper mapper;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Qualifier("GOT_WWW_CONFIG")
    private GotWWWConfig config;

    private Logger logger = LoggerFactory.getLogger(GotWWWRepository.class);

    public GotWWWRepository() {
        this.mapper = new XmlMapper();
    }

    @LoggableEvent(eventType = REPOSITORY_EVENT, action = STORE_SEARCH_GOT_WWW, hystrixCommandKey = GOT_WWW_HYSTRIX_KEY)
    @HystrixCommand(ignoreExceptions = {InvalidLocationException.class}, groupKey = GOT_WWW_HYSTRIX_KEY,
            commandKey = GOT_WWW_HYSTRIX_KEY, threadPoolKey = GOT_WWW_HYSTRIX_KEY)
    @Cacheable(value = "got_www", key = "{#brandCode, #storeSearchLocation?.toString(), #radius}")
    public List<String> getStores(final String brandCode, final StoreSearchLocation storeSearchLocation, final Optional<Integer> radius) {
        try {
            GeoResponse geoResponse = getStoresFromGotWWW(buildUri(brandCode, radius, storeSearchLocation));
            if (isInvalidGeoLocation(geoResponse)) {
                throw new InvalidLocationException(storeSearchLocation.toString());
            }
            if (geoResponse.getStores() == null) {
                logger.info("No stores found in GotWWW for {}", storeSearchLocation.toString());
                return Collections.emptyList();
            }
            return mapToStoreNumbers(geoResponse.getStores());
        } catch (RestClientException e) {
            logger.error(ACTION + STORE_SEARCH_GOT_WWW + "\" Error retrieving store geolocation from got-www for {}", storeSearchLocation.toString(), e);
            throw new GotWWWCommunicationException(e);
        } catch (IOException e) {
            logger.error(ACTION + STORE_SEARCH_GOT_WWW + "\" Error parsing store location response from got-www for {}", storeSearchLocation.toString(), e);
            throw new GotWWWResponseParseException(e);
        }
    }

    private GeoResponse getStoresFromGotWWW(URI uri) throws IOException {
    	ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        return mapper.readValue(response.getBody(), GeoResponse.class);
    }

    private boolean isInvalidGeoLocation(GeoResponse geoResponse) {
        return geoResponse.getLookup().getStartingLatitude() == null && geoResponse.getLookup().getStartingLongitude() == null;
    }

    private List<String> mapToStoreNumbers(Stores stores) {
        return Arrays.stream(stores.getStores())
                .map(Store::getNbr)
                .collect(Collectors.toList());
    }

    private URI buildUri(String brandCode, Optional<Integer> radius, StoreSearchLocation storeSearchLocation) {
        List<NameValuePair> queryParams = new ArrayList<>();

        queryParams.add(new BasicNameValuePair("mile_radius", radius.orElse(config.getDefaultMileRadius()).toString()));
        queryParams.add(new BasicNameValuePair("brand1", brandCode));
        queryParams.add(new BasicNameValuePair("min_results", config.getResultLimit().toString()));

        if (storeSearchLocation.getFreeFormAddress() != null) {
            queryParams.add(new BasicNameValuePair("freeform_address", storeSearchLocation.getFreeFormAddress()));
        } else {
            Coordinates coordinates = storeSearchLocation.getCoordinates();
            queryParams.add(new BasicNameValuePair("latitude", coordinates.getLatitude().toString()));
            queryParams.add(new BasicNameValuePair("longitude", coordinates.getLongitude().toString()));
        }

        return config.getUrl(queryParams);
    }
}
