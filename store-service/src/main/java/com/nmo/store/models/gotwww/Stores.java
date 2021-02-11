package com.sixthday.store.models.gotwww;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Stores {
	@JacksonXmlProperty(localName = "store")
	@JacksonXmlElementWrapper(useWrapping = false)
	private Store[] stores;
}
