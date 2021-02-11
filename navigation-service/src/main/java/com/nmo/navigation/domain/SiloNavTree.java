package com.sixthday.navigation.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class SiloNavTree implements Serializable {

    private static final long serialVersionUID = -1453681762556940896L;

    private String id;

    private String siloData;
}
