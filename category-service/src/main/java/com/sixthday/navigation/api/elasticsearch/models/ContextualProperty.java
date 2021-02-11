package com.sixthday.navigation.api.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;

import static org.springframework.web.util.HtmlUtils.htmlUnescape;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class ContextualProperty implements Serializable {
    private static final long serialVersionUID = 4140704566827774569L;
    private String parentId;
    @Getter(AccessLevel.NONE)
    private String desktopAlternateName;
    private String mobileAlternateName;
    private List<String> childCategoryOrder;
    private String driveToSubcategoryId;
    private boolean redText;

    public String getDesktopAlternateName() {
        return htmlUnescape(desktopAlternateName);
    }
}
