package com.sixthday.navigation.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SiloNavTreeReaderResponse {
    private String countryCode;
    private String navTree;
    private String navKeyGroup;
}
