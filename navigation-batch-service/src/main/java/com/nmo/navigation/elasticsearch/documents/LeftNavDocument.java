package com.sixthday.navigation.elasticsearch.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import lombok.*;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class LeftNavDocument implements Serializable {
    public static final String DOCUMENT_TYPE = "leftnav";
    private static final long serialVersionUID = 8480790827880164595L;
    private String id;
    private String categoryId;
    private String name;
    private Set<String> referenceIds = new HashSet<>();
    private String driveToPath;
    private List<LeftNavTreeNode> leftNav;
    private List<LeftNavTreeNode> boutiqueLeftNav;
    private String refreshablePath;
}