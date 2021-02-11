package com.sixthday.store.controllers;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.store.exceptions.InvalidLatitudeLongitudeException;
import com.sixthday.store.exceptions.InvalidLocationException;
import com.sixthday.store.models.*;
import com.sixthday.store.services.StoreSearchService;
import com.sixthday.store.util.sixthdayMDCAdapter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sixthday.store.config.Constants.Actions.GET_STORE;
import static com.sixthday.store.config.Constants.Actions.STORE_SEARCH;
import static com.sixthday.store.config.Constants.Events.API_EVENT;
import static com.sixthday.store.config.Constants.Logging.*;

@RestController
public class StoreSearchController {


	private sixthdayMDCAdapter mdc;

    private StoreSearchService storeSearchService;

    private Logger logger = LoggerFactory.getLogger(StoreSearchController.class);

    @Autowired
    public StoreSearchController(final StoreSearchService storeSearchService, sixthdayMDCAdapter mdc) {
        this.storeSearchService = storeSearchService;
        this.mdc = mdc;
    }

    @LoggableEvent(eventType = API_EVENT, action = STORE_SEARCH)
    @RequestMapping(value = "/stores", produces = MediaType.APPLICATION_JSON_VALUE, params = {"brandCode", "freeFormAddress", "skuId", "quantity", "!latitude", "!longitude", "!storeId"})
    public ResponseEntity<List<sixthdayStore>> getStores(@RequestParam("brandCode") String brandCode,
                                                    @RequestParam("mileRadius") Optional<Integer> mileRadius,
                                                    @RequestParam("freeFormAddress") String freeFormAddress,
                                                    @RequestParam("skuId") String skuId,
                                                    @RequestParam("quantity") Integer quantity) {
        addInputRequestToMDC(brandCode, mileRadius, skuId);
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation(freeFormAddress);
        return getListResponseEntity(brandCode, mileRadius, storeSearchLocation, Optional.of(skuId),Optional.of(quantity));
    }

    @LoggableEvent(eventType = API_EVENT, action = STORE_SEARCH)
    @RequestMapping(value = "/skuInventoryByStore", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StoreSkuInventory> getStores(@RequestParam("storeId") String storeId,
                                                       @RequestParam("skuIds") String skuIds,
                                                       @RequestParam("caller") String caller) {
        addInputRequestToMDC(storeId, skuIds, caller);
        try {
            return new ResponseEntity<>(storeSearchService.getSKUsInventory(storeId, skuIds), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while getting skuInventoryByStore", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @LoggableEvent(eventType = API_EVENT, action = STORE_SEARCH)
    @RequestMapping(value = "/stores", produces = MediaType.APPLICATION_JSON_VALUE, params = {"brandCode", "freeFormAddress", "!skuId", "!quantity", "!latitude", "!longitude", "!storeId"})
    public ResponseEntity<StoreResponse> getStores(@RequestParam("brandCode") String brandCode,
                                                    @RequestParam("mileRadius") Integer mileRadius,
                                                    @RequestParam("freeFormAddress") String freeFormAddress) {
    	Optional<Integer> miles = Optional.of(mileRadius);
        addInputRequestToMDC(brandCode, miles, "NA");
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation(freeFormAddress);
        return getStoreNumbersResponseEntity(brandCode, miles, storeSearchLocation);
    }

    @LoggableEvent(eventType = API_EVENT, action = STORE_SEARCH)
    @RequestMapping(value = "/stores", produces = MediaType.APPLICATION_JSON_VALUE, params = {"brandCode", "latitude", "longitude", "!freeFormAddress", "!skuId", "!quantity", "!storeId"})
    public ResponseEntity<List<sixthdayStore>> getStores(@RequestParam("brandCode") String brandCode,
                                                    @RequestParam("mileRadius") Optional<Integer> mileRadius,
                                                    @RequestParam("latitude") String latitude,
                                                    @RequestParam("longitude") String longitude) {
        addInputRequestToMDC(brandCode, mileRadius, latitude, longitude);
        Coordinates coordinates;
        try {
            coordinates = new Coordinates(Float.parseFloat(latitude), Float.parseFloat(longitude));
        } catch (NumberFormatException e) {
            throw new InvalidLatitudeLongitudeException(e.getMessage());
        }
        StoreSearchLocation storeSearchLocation = new StoreSearchLocation(coordinates);
        return getListResponseEntity(brandCode, mileRadius, storeSearchLocation,Optional.empty(),Optional.empty());
    }

    @LoggableEvent(eventType = API_EVENT, action = GET_STORE)
    @RequestMapping(value = "/stores", produces = MediaType.APPLICATION_JSON_VALUE, params = {"storeId", "!latitude", "!longitude", "!freeFormAddress", "!skuId", "!quantity", "!brandCode"})
    public ResponseEntity<List<sixthdayStore>> getStores(@RequestParam("storeId") String storeId) {
        List<sixthdayStore> stores = storeSearchService.getStores(storeId);
        mdc.put(STORE_ID, storeId);
        return new ResponseEntity<>(stores, HttpStatus.OK);
    }

    @SneakyThrows
    private ResponseEntity<List<sixthdayStore>> getListResponseEntity(String brandCode, Optional<Integer> mileRadius, StoreSearchLocation storeSearchLocation, Optional<String> skuId, Optional<Integer> userReqQuantity) {
        List<sixthdayStore> sixthdayStores = null;
        if(skuId.isPresent() && userReqQuantity.isPresent()) {
          try {
            sixthdayStores= storeSearchService.getStores(brandCode, storeSearchLocation, mileRadius, skuId.get(), userReqQuantity.get());
          } catch (UndeclaredThrowableException e) {
            handleExceptionFromToggleFramework(e);
          }
        } else {
            sixthdayStores= storeSearchService.getStores(brandCode, storeSearchLocation, mileRadius);
        }
        
        if (CollectionUtils.isEmpty(sixthdayStores)) {
            logger.info("No Stores found");
        }
        
        return new ResponseEntity<>(sixthdayStores, HttpStatus.OK);
    }

    private void handleExceptionFromToggleFramework(UndeclaredThrowableException e) throws Throwable {
    //Added this block to fix issues with Toggler exception.
      Throwable exceptionFromToggler = e.getUndeclaredThrowable();
      if (exceptionFromToggler instanceof InvocationTargetException) {
        InvocationTargetException wrappedException = (InvocationTargetException) exceptionFromToggler;
        throw wrappedException.getTargetException();
      } else {
        throw exceptionFromToggler.getCause();
      }
    }
    private ResponseEntity<StoreResponse> getStoreNumbersResponseEntity(String brandCode, Optional<Integer> mileRadius,
                                                                        StoreSearchLocation storeSearchLocation) {
        final StoreResponse storeResponse = new StoreResponse();

        try {
            List<sixthdayStore> sixthdayStores = storeSearchService.getStores(brandCode, storeSearchLocation, mileRadius);
            if (CollectionUtils.isEmpty(sixthdayStores)) {
                logger.info("No Stores found");
            }
            storeResponse.setStoreNumbers(sixthdayStores.stream().map(sixthdayStore::getStoreNumber).collect(Collectors.toList()));
            return new ResponseEntity<>(storeResponse, HttpStatus.OK);
        } catch (InvalidLocationException invalidLocation) {
            logger.error("action=\"" + STORE_SEARCH + "\" Could not find any stores for given location. Error=\"{}\"", invalidLocation.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private void addInputRequestToMDC(String storeId, String skuIds, String caller) {
        mdc.put(CALLER, caller);
        mdc.put(SKU_IDS, skuIds);
        mdc.put(STORE_ID, storeId);
    }

    private void addInputRequestToMDC(String brandCode, Optional<Integer> mileRadius, String skuId) {
        mdc.put(BRAND_CODE_KEY, brandCode);
        mdc.put(MILE_RADIUS, mileRadius.isPresent()?mileRadius.get().toString():"");
        mdc.put(SKU_ID, skuId);
    }

    private void addInputRequestToMDC(String brandCode, Optional<Integer> mileRadius, String latitude, String longitude) {
        mdc.put(BRAND_CODE_KEY, brandCode);
        mdc.put(MILE_RADIUS, mileRadius.isPresent()?mileRadius.get().toString():"");
        mdc.put(LATITUDE, latitude);
        mdc.put(LONGITUDE, longitude);
    }
}

