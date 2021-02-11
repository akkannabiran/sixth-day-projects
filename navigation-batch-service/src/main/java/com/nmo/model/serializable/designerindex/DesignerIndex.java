package com.sixthday.model.serializable.designerindex;

import java.io.Serializable;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "sixthday_designer_index")
@EqualsAndHashCode
public class DesignerIndex implements Serializable {
  private static final long serialVersionUID = -427973010457452357L;
  @DynamoDBHashKey(attributeName = "designer_id")
  private String id;
  @DynamoDBAttribute
  private String name;
  @DynamoDBAttribute(attributeName = "designers_by_category")
  private List<DesignerByCategory> designersByCategory;
  @DynamoDBAttribute(attributeName = "designers_by_index")
  private List<DesignersByIndex> designersByIndex;

}
