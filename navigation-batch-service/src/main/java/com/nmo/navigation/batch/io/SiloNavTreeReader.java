package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.batch.vo.SiloNavTreeReaderResponse;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.*;

import java.util.Optional;

import static com.sixthday.sixthdayLogging.OperationType.ATG_FETCH_NAVIGATION_TREE;
import static com.sixthday.sixthdayLogging.logOperation;
import static org.springframework.web.util.HtmlUtils.htmlUnescape;

@Slf4j
public class SiloNavTreeReader {

    private RestTemplate restTemplate;

    private String endpointUrl;

    private String userAgent;

    private String countryCode;
    private String navKeyGroup;

    public SiloNavTreeReader(String endpointUrl, String countryCode, String userAgent, String navKeyGroup, RestTemplate restTemplate) {
        this.endpointUrl = endpointUrl.replace("{country_code}", countryCode).replace("{navKeyGroup}", Optional.ofNullable(navKeyGroup).orElse("A"));
        this.userAgent = userAgent;
        this.restTemplate = restTemplate;
        this.countryCode = countryCode;
        this.navKeyGroup = navKeyGroup;
    }

    ResponseEntity<String> callNavTreeAPI() {
        return restTemplate.exchange(endpointUrl, HttpMethod.GET, getHttpEntity(), String.class);
    }

    @SneakyThrows
    public SiloNavTreeReaderResponse read() {
        try {
            return logOperation(log, null, ATG_FETCH_NAVIGATION_TREE, endpointUrl, () -> {
                ResponseEntity<String> responseEntity = callNavTreeAPI();
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    return new SiloNavTreeReaderResponse(countryCode, htmlUnescape(responseEntity.getBody()), navKeyGroup);
                } else {
                    throw new NavigationBatchServiceException("Error occurred while reading data from ATG navigation service. Response body is \"" + responseEntity.getBody() + "\" and Status Code is " + responseEntity.getStatusCode());
                }
            });
        } catch (ResourceAccessException | HttpClientErrorException exception) {
            throw new NavigationBatchServiceException("Time-out exception occurred while reading data from ATG navigation service ", exception);
        }
    }

    private HttpEntity<?> getHttpEntity() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("User-Agent", userAgent);
        return new HttpEntity<>("parameters", httpHeaders);
    }
}
