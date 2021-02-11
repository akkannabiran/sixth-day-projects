package com.sixthday.store.models.storeindex;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreDocument {
	public static final String DOCUMENT_TYPE = "_doc";

	private String id;
	private String storeNumber;
	private String storeName;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private String zipCode;
	private String phoneNumber;
	private String storeHours;
	private String storeDescription;
	private boolean displayable;
	private boolean eligibleForBOPS;

	private List<StoreEventDocument> events = new ArrayList<>();

	@JsonIgnore
    public String asJsonString() throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(this);
	}
}
