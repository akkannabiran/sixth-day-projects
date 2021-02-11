package com.sixthday.navigation.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class SiloNavTree implements Serializable {

    private static final long serialVersionUID = 1779470329469203192L;

    private String id;

    private String siloData;

    public SiloNavTree(final String id, final String siloData) {
        this.id = id;
        this.siloData = siloData;
    }
}
