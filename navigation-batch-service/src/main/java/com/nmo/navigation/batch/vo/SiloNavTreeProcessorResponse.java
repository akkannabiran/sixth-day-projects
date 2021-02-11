package com.sixthday.navigation.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
@AllArgsConstructor
public class SiloNavTreeProcessorResponse {
    private String countryCode;
    private String deviceType;
    private String silo;
    private String navKeyGroup;

    public String getNavTreeId() {
        String navTreeId = this.countryCode.toUpperCase() + "_" + this.deviceType;
        if (!StringUtils.isEmpty(this.navKeyGroup) && !"A".equals(this.navKeyGroup)) {
            navTreeId += "_" + this.navKeyGroup;
        }
        return navTreeId;
    }
}
