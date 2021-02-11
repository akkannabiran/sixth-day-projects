package com.sixthday.navigation.integration.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import lombok.*;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class CategoryMessage extends CategoryDocument {
    private String batchId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, String> originTimestampInfo;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private EventType eventType;

    public enum EventType {
        CATEGORY_UPDATED,
        CATEGORY_REMOVED,
        UNKNOWN,
    }
}
