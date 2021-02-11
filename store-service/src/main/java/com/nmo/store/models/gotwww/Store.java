package com.sixthday.store.models.gotwww;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Store {
	private String distance;
	private String nbr;
	private String name;
	private String brand;
	private String longitude;
	private String latitude;
	@JacksonXmlProperty(localName = "map_url")
	private String mapUrl;

}
