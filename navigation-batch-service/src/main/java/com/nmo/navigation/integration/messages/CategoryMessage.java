package com.sixthday.navigation.integration.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import lombok.*;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryMessage extends CategoryDocument {

    private EventType eventType;
    private String messageType;
    private String batchId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, String> originTimestampInfo;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private transient Map<String, Object> dataPoints;

    public enum EventType {
        CATEGORY_UPDATED,
        CATEGORY_REMOVED
    }
}
