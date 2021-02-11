package com.sixthday.store.models.storeindex;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class StoreMessage extends StoreDocument {
	private EventType eventType;
	
	public enum EventType {
		STORE_UPSERT
	}

	@JsonProperty(access=Access.WRITE_ONLY)
  private String batchId;
	@JsonProperty(access=Access.WRITE_ONLY)
  private Map<String, String> originTimestampInfo;
	@JsonProperty(access=Access.WRITE_ONLY)
  private Map<String, Object> dataPoints;
	
	public StoreMessage(String storeId) {
		super();
		this.setId(storeId);
	}

	@JsonIgnore
  public boolean isValid() {
		return eventType != null;
	}

	@JsonIgnore
	public boolean isInvalid() {
		return !isValid();
	}
}
