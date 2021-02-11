package com.sixthday.navigation.batch.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeftNavTreeNode implements Serializable {
    public static final String SEPARATOR = "_";
    private static final long serialVersionUID = -7361651542147060078L;
    private String id;
    private String name;
    private String url;
    private int level;
    private List<LeftNavTreeNode> categories;
    private String path;
    private boolean redText;
    private boolean selected;
    private List<String> excludedCountries;
}
