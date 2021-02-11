package com.sixthday.store.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown=true)
public class StoreResponseForAPIValidation {
	private String storeNumber;
	private String storeId;
	private MockSkuAvailability skuAvailability;
	
	@Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class MockSkuAvailability {
    	private String status;
    	private boolean inventoryAvailable;
    	private String addToCartMessage;
    }
}