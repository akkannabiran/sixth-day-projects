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
public class DesignersByIndex implements Serializable {
  private static final long serialVersionUID = -5328863664401989003L;
  @DynamoDBAttribute
  private String name;
  @DynamoDBAttribute
  private List<DesignerCategory> categories;
}
