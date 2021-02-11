package com.sixthday.navigation.api.elasticsearch.models;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Filter implements Serializable {
  private static final long serialVersionUID = 1128320011032213390L;
  private String defaultName;
  private String alternateName;
  private List<String> disabled;
  private List<String> values;
}
