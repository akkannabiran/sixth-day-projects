package com.sixthday.store.models.gotwww;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoResponse {
	@JacksonXmlProperty(localName = "status_code")
	private String statusCode;
	@JacksonXmlProperty(localName = "status_desc")
	private String statusDescription;
	private Stores stores;
	private Lookup lookup;
}
