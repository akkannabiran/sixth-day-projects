package com.sixthday.navigation.batch.designers.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.sixthday.model.serializable.designerindex.DesignerByCategory;
import com.sixthday.model.serializable.designerindex.DesignerIndex;
import com.sixthday.model.serializable.designerindex.DesignersByIndex;
import com.sixthday.navigation.repository.dynamodb.DesignerIndexRepository;
import com.sixthday.navigation.toggles.FeatureToggles;
import com.sixthday.navigation.toggles.Features;

import lombok.SneakyThrows;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;

import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBDesignerIndexRepositoryTest {
  @Mock
  private DynamoDBMapper dynamoDBMapper;
  @Mock
  private FeatureToggles featureToggles;
  
  @Mock
  private PaginatedScanList<DesignerIndex> dynamoDBScanResults;
  
  @InjectMocks
  private DesignerIndexRepository dynamoDBDesignerIndexRepository;
  
  @Before
  @SneakyThrows
  public void setup() {
    when(featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)).thenReturn(Boolean.TRUE);
    DesignerIndex di = new DesignerIndex("id1", "name1", null, null);
    dynamoDBScanResults.add(di);
    when(dynamoDBMapper.scan(eq(DesignerIndex.class), any(DynamoDBScanExpression.class))).thenReturn(dynamoDBScanResults);
    Map<String, DesignerIndex> designerCache = new HashMap<>();
    designerCache.put("id1", di);
    Map<String, String> desingerHashCache = new HashMap<>();
    desingerHashCache.put("id1", String.valueOf(di.hashCode()));
    FieldUtils.writeField(dynamoDBDesignerIndexRepository, "designerCache", designerCache, true);
    FieldUtils.writeField(dynamoDBDesignerIndexRepository, "designerHashCodes", desingerHashCache, true);
  }
  
  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public void testCacheConstruction() {
    DesignerIndex di2 = new DesignerIndex("id2", "name2", null, null);
    when(dynamoDBScanResults.stream()).thenReturn(Stream.of(di2));
    
    DesignerIndexRepository repo = new DesignerIndexRepository(dynamoDBMapper, featureToggles);
    DesignerIndex actualDI = repo.get("id2");
    assertThat(actualDI, equalTo(new DesignerIndex("id2", "name2", null, null)));
    
    Map<String, DesignerIndex> designerCache = (Map<String, DesignerIndex>)FieldUtils.readField(repo, "designerCache", true);
    assertThat(designerCache.get("id2"), equalTo(di2));
  }
  
  @Test
  public void shouldGetDesignerObjectFromCacheWhenReadFromCacheToggleIsON() {
    when(featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)).thenReturn(Boolean.TRUE);
    assertThat(dynamoDBDesignerIndexRepository.get("id1"), equalTo(new DesignerIndex("id1", "name1", null, null)));
  }

  @Test
  public void shouldGetDesignerObjectFromDynamoDBWhenReadFromCacheToggleIsOnAndObjectNotAvailableInCache() {
    when(featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)).thenReturn(Boolean.TRUE);
    when(dynamoDBMapper.load(eq(DesignerIndex.class), anyString())).thenReturn(new DesignerIndex("id2", "name2", null, null));

    DesignerIndex actualDI = dynamoDBDesignerIndexRepository.get("id2");
    assertThat(actualDI, equalTo(new DesignerIndex("id2", "name2", null, null)));
    verify(dynamoDBMapper).load(eq(DesignerIndex.class), eq("id2"));
  }
  
  @Test
  @SneakyThrows
  public void shouldGetDesignerObjectFromDynamoDBWhenReadFromCacheToggleIsOnAndCacheIsEmpty() {
    FieldUtils.writeField(dynamoDBDesignerIndexRepository, "designerCache", Collections.emptyMap(), true);
    when(featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)).thenReturn(Boolean.TRUE);
    when(dynamoDBMapper.load(eq(DesignerIndex.class), anyString())).thenReturn(new DesignerIndex("id2", "name2", null, null));

    DesignerIndex actualDI = dynamoDBDesignerIndexRepository.get("id2");
    assertThat(actualDI, equalTo(new DesignerIndex("id2", "name2", null, null)));
    verify(dynamoDBMapper).load(eq(DesignerIndex.class), eq("id2"));
  }
  
  @Test
  @SneakyThrows
  public void shouldGetDesignerObjectFromDynamoDBWhenReadFromCacheToggleIsOnAndCacheIsNull() {
    FieldUtils.writeField(dynamoDBDesignerIndexRepository, "designerCache", null, true);
    when(featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)).thenReturn(Boolean.TRUE);
    when(dynamoDBMapper.load(eq(DesignerIndex.class), anyString())).thenReturn(new DesignerIndex("id2", "name2", null, null));

    DesignerIndex actualDI = dynamoDBDesignerIndexRepository.get("id2");
    assertThat(actualDI, equalTo(new DesignerIndex("id2", "name2", null, null)));
    verify(dynamoDBMapper).load(eq(DesignerIndex.class), eq("id2"));
  }

  @Test
  public void shouldGetDesignerObjectFromDynamoDBWhenReadFromCacheToggleIsOFF() {
    when(featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)).thenReturn(Boolean.FALSE);
    when(dynamoDBMapper.load(eq(DesignerIndex.class), anyString())).thenReturn(new DesignerIndex("id1", "name1",
            Collections.singletonList(new DesignerByCategory()), Collections.singletonList(new DesignersByIndex())));

    DesignerIndex actualDI = dynamoDBDesignerIndexRepository.get("id1");
    assertThat(actualDI, equalTo(new DesignerIndex("id1", "name1", Collections.singletonList(new DesignerByCategory()), Collections.singletonList(new DesignersByIndex()))));
    verify(dynamoDBMapper).load(eq(DesignerIndex.class), eq("id1"));
  }
 
  @Test
  @SneakyThrows
  public void shouldSaveDesignerIndexToDynamoDBWhenHashCodeMapIsEmpty() {
    FieldUtils.writeField(dynamoDBDesignerIndexRepository, "designerHashCodes", new HashMap<>(), true);
    when(featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)).thenReturn(Boolean.FALSE);
    doNothing().when(dynamoDBMapper).save(any(DesignerIndex.class));

    DesignerIndex designerIndex = new DesignerIndex("id1", "name1", 
            Collections.singletonList(DesignerByCategory.builder().build()), Collections.singletonList(DesignersByIndex.builder().build()));
    dynamoDBDesignerIndexRepository.save(designerIndex);
    
    verify(dynamoDBMapper).save(designerIndex);
  }
  
  @Test
  @SneakyThrows
  public void shouldSkipSavingDesignerIndexToDynamoDBWhenHashCodeMapHasSameObject() {
    DesignerByCategory desingersByCat = new DesignerByCategory();
    desingersByCat.setId("siloId");
    desingersByCat.setName("siloName");
    desingersByCat.setUrl("boutiqueUrl");
    DesignerIndex designerIndex = new DesignerIndex("id1", "name1",
            Collections.singletonList(desingersByCat), 
            Collections.singletonList(new DesignersByIndex("AlphabetList", null)));
    Map<String, String> hashCodesMap = new HashMap<>();
    hashCodesMap.put(designerIndex.getId(), String.valueOf(designerIndex.hashCode()));
    FieldUtils.writeField(dynamoDBDesignerIndexRepository, "designerHashCodes", hashCodesMap, true);
    when(featureToggles.isEnabled(Features.ENABLE_DYNAMODB_READ_CACHE)).thenReturn(Boolean.FALSE);
    doNothing().when(dynamoDBMapper).save(any(DesignerIndex.class));
    
    dynamoDBDesignerIndexRepository.save(designerIndex);
    
    verify(dynamoDBMapper, never()).save(designerIndex);
  }
  
  @Test
  public void verifyEqualsDesignerIndex() {
   EqualsVerifier.forClass(DesignerIndex.class).suppress(Warning.STRICT_INHERITANCE,Warning.NONFINAL_FIELDS).verify();
  }

  @Test
  public void verifyEqualsDesignerByCategory() {
   EqualsVerifier.forClass(DesignerByCategory.class).suppress(Warning.STRICT_INHERITANCE,Warning.NONFINAL_FIELDS).verify();
  }
  
  @Test
  public void verifyEqualsDesignerByIndex() {
   EqualsVerifier.forClass(DesignersByIndex.class).suppress(Warning.STRICT_INHERITANCE,Warning.NONFINAL_FIELDS).verify();
  }
  
}
