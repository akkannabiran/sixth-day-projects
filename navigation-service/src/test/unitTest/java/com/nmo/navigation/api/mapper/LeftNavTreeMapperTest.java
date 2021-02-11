package com.sixthday.navigation.api.mapper;

import com.sixthday.navigation.api.mappers.LeftNavNodeMapper;
import com.sixthday.navigation.api.mappers.LeftNavTreeMapper;
import com.sixthday.navigation.api.models.LeftNavCategoryBuilder;
import com.sixthday.navigation.api.models.LeftNavTree;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;

import org.junit.Test;

import static com.sixthday.navigation.api.data.LeftNavTreeTestDataFactory.getTestLeftNavDocument;
import static com.sixthday.navigation.config.Constants.US_COUNTRY_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class LeftNavTreeMapperTest {
  
  private LeftNavTreeMapper leftNavTreeMapper = new LeftNavTreeMapper(new LeftNavNodeMapper());
  
  @Test
  public void shouldReturnLeftNavTreeWhenCategoryIdPassedInParameter() {
    LeftNavDocument leftNavDocument = getTestLeftNavDocument();
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToWebLeftNav(leftNavDocument, US_COUNTRY_CODE);
    
    assertEquals(leftNavTree.getId(), leftNavDocument.getId());
    assertEquals(leftNavTree.getName(), leftNavDocument.getName());
    assertEquals(leftNavTree.getLeftNav().get(0).getId(), "cat1");
    assertEquals(leftNavTree.getLeftNav().get(0).getUrl(), "testurl");
    assertEquals(leftNavTree.getLeftNav().get(0).getCategories().size(), 2);
    assertEquals(leftNavTree.getRefreshablePath(), "refreshablePath");
  }
  
  @Test
  public void shouldReturnEmptyResponseWhenEmptyCategoriesPassedInParameter() {
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToWebLeftNav(new LeftNavDocument(null, null, null, null, null, null), US_COUNTRY_CODE);
    
    assertNull(leftNavTree);
  }
  
  
  @Test
  public void shouldReturnCompleteLeftNavTreeWhenRootCategoryIsSelected() {
    LeftNavDocument leftNavDocument = getTestLeftNavDocument();
    leftNavDocument.getLeftNav().get(0).setSelected(true);
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);
    
    assertEquals(leftNavTree.getId(), leftNavDocument.getId());
    assertEquals(leftNavTree.getName(), leftNavDocument.getName());
    assertEquals(leftNavTree.getLeftNav().get(0).getId(), "cat1");
    assertEquals(leftNavTree.getLeftNav().get(0).getUrl(), "testurl");
    assertEquals(leftNavTree.getLeftNav().get(0).getCategories().size(), 2);
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(0).getId(), equalTo("child1"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(0).isSelected());
    
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getId(), equalTo("child2"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(1).isSelected());
    
    assertNull(leftNavTree.getRefreshablePath());
  }
  
  @Test
  public void shouldReturnHybridLeftNavWithParentAndSiblingsWhenSelectedCategoryHasNoChildren() {
    LeftNavDocument leftNavDocument = getTestLeftNavDocumentWithNoChildrenForSelectedCategory();
    
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);
    
    assertThat(leftNavTree.getBoutiqueLeftNav(), empty());
    assertThat(leftNavTree.getLeftNav().size(), equalTo(1));
    assertThat(leftNavTree.getLeftNav().get(0).getId(), equalTo("cat1"));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().size(), equalTo(2));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(0).getId(), equalTo("child1"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(0).isSelected());
    
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getId(), equalTo("child2"));
    assertTrue(leftNavTree.getLeftNav().get(0).getCategories().get(1).isSelected());
  }
  
  @Test
  public void shouldReturnHybridLeftNavWithParentAndSiblingsWhenSelectedCategoryHasNullAsChildren() {
    LeftNavDocument leftNavDocument = getTestLeftNavDocumentWithNoChildrenForSelectedCategory();
    leftNavDocument.getLeftNav().get(0).setCategories(null);;
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);
    
    assertThat(leftNavTree.getBoutiqueLeftNav(), empty());
    assertThat(leftNavTree.getLeftNav().size(), equalTo(1));
    assertThat(leftNavTree.getLeftNav().get(0).getId(), equalTo("cat1"));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().size(), equalTo(0));
  }
  
  @Test
  public void shouldReturnHybridLeftNavWithAParentAndSiblingsWhenSelectedCategoryHasChildrenAtLevel3() {
    
    List<LeftNavTreeNode> l1Nodes =  new ArrayList<>();
    IntStream.range(1,5).forEach(i->{
      List<LeftNavTreeNode> l2Nodes = new ArrayList<>();
      IntStream.range(1,5).forEach(j->{
        LeftNavTreeNode l3 = new LeftNavCategoryBuilder().withId("L3-Id-"+i+"-"+j).build();
        if (i==4 && j==3) {
          l3.setSelected(true);
        }
        l2Nodes.add(l3);
      });
      l1Nodes.add(new LeftNavCategoryBuilder().withId("l2-"+i).withName("l2-name-"+i).withCategories(l2Nodes).build());
    });
    
    
    LeftNavDocument leftNavDocument =  new LeftNavDocument("cat1", "name1", "cat1_child1", l1Nodes, null, null);
    
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);
    
    assertThat(leftNavTree.getBoutiqueLeftNav(), empty());
    assertThat(leftNavTree.getLeftNav().size(), equalTo(1));
    assertThat(leftNavTree.getLeftNav().get(0).getId(), equalTo("l2-4"));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().size(), equalTo(4));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(0).getId(), equalTo("L3-Id-4-1"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(0).isSelected());
    
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getId(), equalTo("L3-Id-4-2"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(1).isSelected());
    
    
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(2).getId(), equalTo("L3-Id-4-3"));
    assertTrue(leftNavTree.getLeftNav().get(0).getCategories().get(2).isSelected());
  }
  
  
  @Test
  public void shouldReturnHybridLeftNavWithParentAndSiblingsWhenSelectedCategoryHasChildren() {
    LeftNavDocument leftNavDocument = getTestLeftNavDocumentWithHavingChildrenForSelectedCategory();
    
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);
    
    assertThat(leftNavTree.getBoutiqueLeftNav(), empty());
    assertThat(leftNavTree.getLeftNav().size(), equalTo(1));
    assertThat(leftNavTree.getLeftNav().get(0).getId(), equalTo("child2"));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().size(), equalTo(2));
    assertTrue(leftNavTree.getLeftNav().get(0).isSelected());
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(0).getId(), equalTo("grandChild1"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(0).isSelected());
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getId(), equalTo("grandChild2"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(1).isSelected());
  }
  
  @Test
  public void shouldReturnFullLeftNavWithParentAndSiblingsWhenSelectedChildCategoryIsBoutique() {
    LeftNavDocument leftNavDocument = getTestBoutiqueLeftNavDocumentForChildCategory();
    
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);
    
    assertThat(leftNavTree.getBoutiqueLeftNav(), empty());
    assertThat(leftNavTree.getLeftNav().size(), equalTo(1));
    assertThat(leftNavTree.getLeftNav().get(0).getId(), equalTo("cat1"));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().size(), equalTo(2));
    assertFalse(leftNavTree.getLeftNav().get(0).isSelected());
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(0).getId(), equalTo("child1"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(0).isSelected());
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getId(), equalTo("child2"));
    assertTrue(leftNavTree.getLeftNav().get(0).getCategories().get(1).isSelected());
  }
  
  @Test
  public void shouldReturnFullLeftNavWithParentAndSiblingsWhenSelectedGrandChildCategoryIsBoutique() {
    LeftNavDocument leftNavDocument = getTestBoutiqueLeftNavDocumentForGrandChildCategory();
    
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);
    
    assertThat(leftNavTree.getBoutiqueLeftNav(), empty());
    assertThat(leftNavTree.getLeftNav().size(), equalTo(1));
    assertThat(leftNavTree.getLeftNav().get(0).getId(), equalTo("cat1"));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().size(), equalTo(2));
    assertFalse(leftNavTree.getLeftNav().get(0).isSelected());
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(0).getId(), equalTo("child1"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(0).isSelected());
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getId(), equalTo("child2"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(1).isSelected());
    
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getCategories().size(), equalTo(2));
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getCategories().get(0).getId(), equalTo("grandChild1"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(1).getCategories().get(0).isSelected());
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getCategories().get(1).getId(), equalTo("grandChild2"));
    assertTrue(leftNavTree.getLeftNav().get(0).getCategories().get(1).getCategories().get(1).isSelected());
  }
  
  @Test
  public void shouldReturnFullLeftNavWhenNoNodeSelected() {
    LeftNavDocument leftNavDocument = getTestLeftNavDocument();
    
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);

    assertEquals(leftNavTree.getId(), leftNavDocument.getId());
    assertEquals(leftNavTree.getName(), leftNavDocument.getName());
    assertEquals(leftNavTree.getLeftNav().get(0).getId(), "cat1");
    assertEquals(leftNavTree.getLeftNav().get(0).getUrl(), "testurl");
    assertEquals(leftNavTree.getLeftNav().get(0).getCategories().size(), 2);
    assertFalse(leftNavTree.getLeftNav().get(0).isSelected());
    
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(0).getId(), equalTo("child1"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(0).isSelected());
    
    assertThat(leftNavTree.getLeftNav().get(0).getCategories().get(1).getId(), equalTo("child2"));
    assertFalse(leftNavTree.getLeftNav().get(0).getCategories().get(1).isSelected());
    
    assertNull(leftNavTree.getRefreshablePath());
  }
  
  @Test
  public void shouldReturnEmptyHybridLeftNavResponseWhenNullCategoriesPassedInParameter() {
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(new LeftNavDocument(null, null, null, null, null, null), US_COUNTRY_CODE);
    
    assertNull(leftNavTree);
  }
  
  @Test
  public void shouldReturnEmptyHybridLeftNavResponseWhenEmptyCategoriesPassedInParameter() {
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(new LeftNavDocument(null, null, null, Collections.emptyList(),
            Arrays.asList(new LeftNavCategoryBuilder().withId("boutiqueCat1").build()), null), US_COUNTRY_CODE);
    assertNull(leftNavTree);
  }
  
  @Test
  public void shouldReturnEmptyBoutiqueLinkAndNullRefreshableForHybridLeftNavTree() {
    LeftNavDocument leftNavDocument = getTestLeftNavDocument();
    leftNavDocument.getLeftNav().get(0).setSelected(true);
    LeftNavTree leftNavTree = leftNavTreeMapper.mapElasticSearchLeftNavToHybridFacet(leftNavDocument, US_COUNTRY_CODE);
    
    assertEquals(leftNavTree.getId(), leftNavDocument.getId());
    assertEquals(leftNavTree.getName(), leftNavDocument.getName());
    assertEquals(leftNavTree.getRefreshablePath(), null);
    assertThat(leftNavTree.getBoutiqueLeftNav(), empty());
    
  }
  
  public static LeftNavDocument getTestLeftNavDocumentWithNoChildrenForSelectedCategory() {
    LeftNavDocument leftNavDoc = new LeftNavDocument("cat1", "name1", "cat1_child1",
            Arrays.asList(new LeftNavCategoryBuilder().withId("cat1").withName("name1")
                    .withCategories(Arrays.asList(new LeftNavCategoryBuilder().withId("child1").build(), new LeftNavCategoryBuilder().withId("child2").withSelected(true).build())).build()),
            null, null);
    return leftNavDoc;
  }
  
  public static LeftNavDocument getTestLeftNavDocumentWithHavingChildrenForSelectedCategory() {
    List<LeftNavTreeNode> navNodeList = Arrays.asList(
            new LeftNavCategoryBuilder().withId("cat1").withName("name1")
            .withCategories(Arrays.asList(
                    new LeftNavCategoryBuilder().withId("child1").build(),
                    new LeftNavCategoryBuilder().withId("child2").withSelected(true)
                          .withCategories(Arrays.asList(new LeftNavCategoryBuilder().withId("grandChild1").build(), new LeftNavCategoryBuilder().withId("grandChild2").build())).build()))
            .build());
    LeftNavDocument leftNavDoc = new LeftNavDocument("cat1", "name1", "cat1_child1", navNodeList, null, null);
    return leftNavDoc;
  }
 
  public static LeftNavDocument getTestBoutiqueLeftNavDocumentForChildCategory() {
    List<LeftNavTreeNode> navNodeList = Arrays.asList(
            new LeftNavCategoryBuilder().withId("cat1").withName("name1")
            .withCategories(Arrays.asList(
                    new LeftNavCategoryBuilder().withId("child1").build(),
                    new LeftNavCategoryBuilder().withId("child2").withSelected(true)
                          .withCategories(Arrays.asList(new LeftNavCategoryBuilder().withId("grandChild1").build(), new LeftNavCategoryBuilder().withId("grandChild2").build())).build()))
            .build());
    LeftNavDocument leftNavDoc = new LeftNavDocument("cat1", "name1", "cat1_child1", navNodeList, Collections.singletonList(new LeftNavCategoryBuilder().withId("ShopAllDesigners").build()), null);
    return leftNavDoc;
  }
  
  public static LeftNavDocument getTestBoutiqueLeftNavDocumentForGrandChildCategory() {
    List<LeftNavTreeNode> navNodeList = Arrays.asList(
            new LeftNavCategoryBuilder().withId("cat1").withName("name1")
            .withCategories(Arrays.asList(
                    new LeftNavCategoryBuilder().withId("child1").build(),
                    new LeftNavCategoryBuilder().withId("child2")
                          .withCategories(Arrays.asList(new LeftNavCategoryBuilder().withId("grandChild1").build(), new LeftNavCategoryBuilder().withId("grandChild2").withSelected(true).build())).build()))
            .build());
    LeftNavDocument leftNavDoc = new LeftNavDocument("cat1", "name1", "cat1_child1", navNodeList, Collections.singletonList(new LeftNavCategoryBuilder().withId("ShopAllDesigners").build()), null);
    return leftNavDoc;
  }
}
