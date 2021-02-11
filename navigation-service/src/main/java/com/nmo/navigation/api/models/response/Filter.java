package com.sixthday.navigation.api.models.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Filter {
  private String filterKey;
  private String displayText;
  private List<String> excludeFields;
  private List<String> values;
}
