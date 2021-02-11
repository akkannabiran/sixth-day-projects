package com.sixthday.navigation.batch.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MobileNavTreeNode {
    private String id;
    private String displayName;
    private String url;
    private String textAdornment;
    private List<MobileNavTag> tags;
    private List<MobileNavTreeNode> children;
    private Boolean redTextFlag;
    private Boolean flgBoutiqueTextAdornmentsOverride;
}
