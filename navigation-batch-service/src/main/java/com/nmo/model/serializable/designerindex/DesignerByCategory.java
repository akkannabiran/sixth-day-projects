package com.sixthday.model.serializable.designerindex;

import java.io.Serializable;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;

@Getter
@Setter
@DynamoDBDocument
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DesignerByCategory implements Serializable {
  private static final long serialVersionUID = -8436537791188209549L;
  @DynamoDBAttribute
  private String id;
  @DynamoDBAttribute
  private String name;
  @DynamoDBAttribute
  private String url;
}
