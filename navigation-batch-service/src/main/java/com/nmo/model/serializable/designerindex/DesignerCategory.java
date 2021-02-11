package com.sixthday.model.serializable.designerindex;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@DynamoDBDocument
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DesignerCategory implements Serializable {
  private static final long serialVersionUID = -471704678975743725L;
  @DynamoDBAttribute
  private String id;
  @DynamoDBAttribute
  private String name;
  @DynamoDBAttribute
  private String url;
  @DynamoDBAttribute(attributeName = "adornment_tag")
  private String adornmentTag;
  @DynamoDBAttribute(attributeName = "excluded_countries")
  private List<String> excludedCountries;
}
