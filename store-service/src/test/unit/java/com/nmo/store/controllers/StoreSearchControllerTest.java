package com.sixthday.store.controllers;

import com.sixthday.store.config.Constants;
import com.sixthday.store.data.sixthdayStoreBuilder;
import com.sixthday.store.exceptions.*;
import com.sixthday.store.models.*;
import com.sixthday.store.services.StoreSearchService;
import com.sixthday.store.util.sixthdayMDCAdapter;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(MockitoJUnitRunner.class)

public class StoreSearchControllerTest {

    private static final String TEST_STORE_ID = "100/DT";
    private static final String BRAND_CODE = "NM";
    private static final String ADDRESS = "ADDRESS";
    private static final Float LATITUDE = 1.0f;
    private static final Float LONGITUDE = 2.0f;
    private static final Integer MILE_RADIUS = 100;
    private static final String SKU_ID = "SKU_ID";
    private static final Integer REQUESTED_QUANTITY = 1;

    @Mock
    private sixthdayMDCAdapter mdc;

    @Mock
    private StoreSearchLocation mockStoreSearchLocation;

    @Mock
    private StoreSearchService storeSearchService;

    @InjectMocks
    private StoreSearchController controller;

    @Test
    public void shouldAddInputRequestParamsToMDC() throws Exception {
        whenNew(StoreSearchLocation.class).withArguments(ADDRESS).thenReturn(mockStoreSearchLocation);

        controller.getStores(BRAND_CODE, Optional.of(MILE_RADIUS), ADDRESS, SKU_ID, REQUESTED_QUANTITY);

        verify(mdc).put(Constants.Logging.BRAND_CODE_KEY, BRAND_CODE);
    }

    @Test
    public void shouldReturnStoresForAddressWhenSkuIdAndUserRequestedQuantityAreGiven() throws Exception {
        sixthdayStore sixthdayStore = new sixthdayStoreBuilder().build();

        when(storeSearchService.getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)), eq(SKU_ID), eq(REQUESTED_QUANTITY))).thenReturn(Arrays.asList(sixthdayStore));

        ResponseEntity<List<sixthdayStore>> storeResponse = controller.getStores(BRAND_CODE, Optional.of(MILE_RADIUS), ADDRESS, SKU_ID, REQUESTED_QUANTITY);

        assertThat(storeResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(storeResponse.getBody(), is(Arrays.asList(sixthdayStore)));
    }
    
    @Test
    public void shouldReturnStoreNumbersWhenFreeFormAddressAndRadiusGiven() throws Exception {
        sixthdayStore sixthdayStore = new sixthdayStoreBuilder().build();

        when(storeSearchService.getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)))).thenReturn(Arrays.asList(sixthdayStore));

        ResponseEntity<StoreResponse> storeResponse = controller.getStores(BRAND_CODE, MILE_RADIUS, ADDRESS);
        verify(storeSearchService).getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)));
        verify(mdc).put(Constants.Logging.BRAND_CODE_KEY, BRAND_CODE);
        verify(mdc).put(Constants.Logging.MILE_RADIUS, String.valueOf(MILE_RADIUS));
        verify(mdc).put(Constants.Logging.SKU_ID, "NA");
        
        assertThat(storeResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(storeResponse.getBody().getStoreNumbers(), is(Arrays.asList(sixthdayStore.getStoreNumber())));
    }
    
    @Test
    public void shouldReturnEmptyListWhenNoStoresFoundFromFreeFormAddressAndRadiusGiven() throws Exception {
        when(storeSearchService.getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)))).thenReturn(new ArrayList<>());

        ResponseEntity<StoreResponse> storeResponse = controller.getStores(BRAND_CODE, MILE_RADIUS, ADDRESS);
        verify(storeSearchService).getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)));

        assertThat(storeResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(storeResponse.getBody().getStoreNumbers(), is(Collections.emptyList()));
    }
    
    @Test
    public void shouldGiveBadRequestStatusCodeWhenFreeFormAddressIsEmpty() throws Exception {
        when(storeSearchService.getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)))).thenThrow(new InvalidLocationException("Invalid location test"));
        ResponseEntity<?> response = controller.getStores(BRAND_CODE, MILE_RADIUS, "");
        assertThat("Response status should be 400 for empty address", response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        verify(storeSearchService).getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)));
    }
    
    @Test
    public void shouldGiveBadRequestStatusCodeWhenFreeFormAddressIsNull() throws Exception {
        when(storeSearchService.getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)))).thenThrow(new InvalidLocationException("Invalid location test"));
        ResponseEntity<?> response = controller.getStores(BRAND_CODE, MILE_RADIUS, null);
        assertThat("Response status should be 400 for null address", response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        verify(storeSearchService).getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)));
    }

    @Test
    public void shouldReturnStoresForCoordinates() throws Exception {
        sixthdayStore sixthdayStore = new sixthdayStoreBuilder().build();

        when(storeSearchService.getStores(eq(BRAND_CODE), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)))).thenReturn(Arrays.asList(sixthdayStore));

        ResponseEntity<List<sixthdayStore>> storeResponse = controller.getStores(BRAND_CODE, Optional.of(MILE_RADIUS), LATITUDE.toString(), LONGITUDE.toString());

        assertThat(storeResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(storeResponse.getBody(), is(Arrays.asList(sixthdayStore)));
    }

    @Test(expected = InvalidLatitudeLongitudeException.class)
    public void shouldThrowInvalidLatitudeLongitudeExceptionForInvalidCoordinates() throws Exception {
        controller.getStores(BRAND_CODE, Optional.of(MILE_RADIUS), "10lat", "10long");
    }

    @Test
    public void shouldReturnEmptyListWhenNoStoresMatch() throws Exception {
        whenNew(StoreSearchLocation.class).withArguments(ADDRESS).thenReturn(mockStoreSearchLocation);

        when(storeSearchService.getStores(BRAND_CODE, mockStoreSearchLocation, Optional.of(MILE_RADIUS))).thenReturn(Collections.emptyList());

        ResponseEntity<List<sixthdayStore>> storeResponse = controller.getStores(BRAND_CODE, Optional.of(MILE_RADIUS), ADDRESS, SKU_ID, REQUESTED_QUANTITY);

        assertThat(storeResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(storeResponse.getBody(), is(Collections.emptyList()));
    }

    @Test
    public void shouldReturnStoreDetailsWhenStoreIdIsValid() throws ExecutionException, InterruptedException {
        sixthdayStore sixthdayStore = new sixthdayStoreBuilder().build();
        when(storeSearchService.getStores(TEST_STORE_ID)).thenReturn(Arrays.asList(sixthdayStore));

        ResponseEntity<List<sixthdayStore>> storeResponse = controller.getStores(TEST_STORE_ID);

        assertThat(storeResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(storeResponse.getBody().get(0).getStoreId(), is(sixthdayStore.getStoreId()));
    }

    @Test
    public void shouldAddStoreIdInMDC() throws ExecutionException, InterruptedException {
        sixthdayStore sixthdayStore = new sixthdayStoreBuilder().build();

        when(storeSearchService.getStores(TEST_STORE_ID)).thenReturn(Arrays.asList(sixthdayStore));

        controller.getStores(TEST_STORE_ID);

        verify(mdc).put(Constants.Logging.STORE_ID, TEST_STORE_ID);
    }
    
    @Test(expected = GotWWWCommunicationException.class)
    @SneakyThrows
    public void shouldThrowGotWWWCommunicationExceptionFromUndeclaredException() {
      UndeclaredThrowableException exceptionFromToggler = new UndeclaredThrowableException(new Throwable(new GotWWWCommunicationException(new Exception())));
      when(storeSearchService.getStores(anyString(), any(StoreSearchLocation.class), eq(Optional.of(MILE_RADIUS)), anyString(), eq(REQUESTED_QUANTITY))).thenThrow(exceptionFromToggler);

      controller.getStores(BRAND_CODE, Optional.of(MILE_RADIUS), ADDRESS, SKU_ID, REQUESTED_QUANTITY);
    }

    @Test
    public void shouldReturnStoreSkuInventory() {
        when(storeSearchService.getSKUsInventory("storeId", "sku1,sku2")).thenReturn(new StoreSkuInventory());
        ResponseEntity<StoreSkuInventory> skuInventoryResponseEntity = controller.getStores("storeId", "sku1,sku2", "UI");
        assertThat(skuInventoryResponseEntity.getStatusCode(), is(HttpStatus.OK));
        assertNotNull(skuInventoryResponseEntity.getBody());
        assertNotNull(skuInventoryResponseEntity.getBody().getSkuInventories());
        assertThat(skuInventoryResponseEntity.getBody().getSkuInventories().size(), is(0));
        assertNull(skuInventoryResponseEntity.getBody().getStore());
    }

    @Test
    public void shouldReturnResponseEntityWith500Code() {
        when(storeSearchService.getSKUsInventory("storeId", "sku1,sku2")).thenThrow(Exception.class);
        ResponseEntity<StoreSkuInventory> skuInventoryResponseEntity = controller.getStores("storeId", "sku1,sku2", "UI");
        assertThat(skuInventoryResponseEntity.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertNull(skuInventoryResponseEntity.getBody());
    }
}
