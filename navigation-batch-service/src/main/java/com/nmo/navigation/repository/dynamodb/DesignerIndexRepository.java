package com.sixthday.navigation.repository.dynamodb;

import static com.sixthday.sixthdayLogging.logOperation;
import static com.sixthday.sixthdayLogging.EventType.ON_EVENT;
import static com.sixthday.sixthdayLogging.OperationType.GET_DI_DYNAMO;
import static com.sixthday.sixthdayLogging.OperationType.GET_DI_DYNAMO_CACHE;
import static com.sixthday.sixthdayLogging.OperationType.SAVE_DI_DYNAMO;
import static com.sixthday.sixthdayLogging.OperationType.SKIP_SAVE_DI_DYNAMO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.navigation.repository.IDesignerIndexRepository;
import com.sixthday.navigation.toggles.FeatureToggles;
import com.sixthday.navigation.toggles.Features;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class DesignerIndexRepository implements IDesignerIndexRepository {
  
  private DynamoDBMapper dynamoDBMapper;
  private Map<String, String> designerHashCodes = new HashMap<>();
  private Map<String, DesignerIndex> designerCache = new HashMap<>();
  private FeatureToggles featureToggles;
  
  @Autowired
  public DesignerIndexRepository(DynamoDBMapper dynamoDBMapper, FeatureToggles featureToggles) {
    this.dynamoDBMapper = dynamoDBMapper;
    this.featureToggles = featureToggles;
    if (this.featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)) {
      Optional.ofNullable(dynamoDBMapper.scan(DesignerIndex.class, new DynamoDBScanExpression())).ifPresent(designerIndexes ->  
        designerIndexes.stream().forEach(di-> designerCache.put(di.getId(), di))
      );
    }
  }
  
  @Override
  public DesignerIndex get(String key) {
    if (featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE) && !CollectionUtils.isEmpty(designerCache) && designerCache.containsKey(key)) {
      return logOperation(log, ON_EVENT, GET_DI_DYNAMO_CACHE, () -> designerCache.get(key));
    }
    return logOperation(log, ON_EVENT, GET_DI_DYNAMO, () -> dynamoDBMapper.load(DesignerIndex.class, key));
  }
  
  @Override
  public void save(DesignerIndex designerIndex) {
    String indexId = designerIndex.getId();
    String hashCode = String.valueOf(designerIndex.hashCode());
    if (!hashCode.equals(designerHashCodes.get(indexId))) {
      logOperation(log, ON_EVENT, SAVE_DI_DYNAMO, () -> {
        designerHashCodes.put(indexId, hashCode);
        designerCache.put(indexId, designerIndex);
        dynamoDBMapper.save(designerIndex);
        return null;
      });
    } else {
      logOperation(log, ON_EVENT, SKIP_SAVE_DI_DYNAMO, () -> "DesignerIndex has no changes");
    }
  }
}
