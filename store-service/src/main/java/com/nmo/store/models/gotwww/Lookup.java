package com.sixthday.store.models.gotwww;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Lookup {
	private String limit;
	private Brands brands;
	@JacksonXmlProperty(localName = "freeform_address")
	private String freeFormAddress;
	@JacksonXmlProperty(localName = "mile_radius")
	private String mileRadius;
	@JacksonXmlProperty(localName = "starting_longitude")
	private String startingLongitude;
	private String longitude;
	@JacksonXmlProperty(localName = "starting_latitude")
	private String startingLatitude;
	private String latitude;
	private String ip;
}
