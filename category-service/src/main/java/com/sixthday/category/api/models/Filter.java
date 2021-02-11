package com.sixthday.category.api.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
  private String filterKey;
  private String displayText;
  private List<String> excludeFields;
  private List<String> values;
}
