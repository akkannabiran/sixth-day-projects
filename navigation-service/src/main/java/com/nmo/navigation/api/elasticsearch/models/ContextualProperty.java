package com.sixthday.navigation.api.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextualProperty implements Serializable {
    private static final long serialVersionUID = 4140704566827774569L;

    private boolean redText;
    private boolean boutiqueTextAdornmentsOverride;

    private String parentId;
    private String desktopAlternateName;
    private String mobileAlternateName;
    private String driveToSubcategoryId;
    private String boutiqueTextAdornments;
    private String description;
    private String alternateImagePath;

    private List<String> childCategoryOrder;
}
