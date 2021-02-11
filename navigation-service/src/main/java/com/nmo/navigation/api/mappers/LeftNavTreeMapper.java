package com.sixthday.navigation.api.mappers;

import com.sixthday.navigation.api.models.LeftNavTree;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Component
public class LeftNavTreeMapper {
  
  private LeftNavNodeMapper leftNavNodeMapper;
  
  @Autowired
  public LeftNavTreeMapper(LeftNavNodeMapper leftNavNodeMapper) {
    this.leftNavNodeMapper = leftNavNodeMapper;
  }
  
  public LeftNavTree mapElasticSearchLeftNavToWebLeftNav(final LeftNavDocument leftNavDocument, String countryCode) {
    List<LeftNavTreeNode> filteredLeftNavCategories = filterByCountryCode(leftNavDocument.getLeftNav(), countryCode);
    List<LeftNavTreeNode> filteredBoutiqueLeftNavCategories = filterByCountryCode(leftNavDocument.getBoutiqueLeftNav(), countryCode);
    if (isEmpty(filteredBoutiqueLeftNavCategories) && isEmpty(filteredLeftNavCategories)) {
      return null;
    }
    return new LeftNavTree(leftNavDocument.getId(), leftNavDocument.getName(), leftNavNodeMapper.map(filteredLeftNavCategories), leftNavNodeMapper.map(filteredBoutiqueLeftNavCategories),
            leftNavDocument.getRefreshablePath());
  }
  
  public LeftNavTree mapElasticSearchLeftNavToHybridFacet(final LeftNavDocument leftNavDocument, String countryCode) {
    List<LeftNavTreeNode> leftNavNodes = leftNavDocument.getLeftNav();
    List<LeftNavTreeNode> boutiqueNodes = leftNavDocument.getBoutiqueLeftNav();
    
    String categoryId = leftNavDocument.getId();
    String categoryName = leftNavDocument.getName();
    
    if (!isEmpty(boutiqueNodes)) {
      boutiqueNodes = null;
    } else if (!isEmpty(leftNavNodes)) {
      boutiqueNodes = null;
      Optional<LeftNavTreeNode> selectedNodeOptional = Optional.ofNullable(getSelectedNode(leftNavNodes));
      
      if (selectedNodeOptional.isPresent()) {
        LeftNavTreeNode selectedNode = selectedNodeOptional.get();
        LeftNavTreeNode finalNode = selectedNode;
        List<LeftNavTreeNode> childNodes = selectedNode.getCategories();
        if (CollectionUtils.isEmpty(childNodes)) {
          Optional<LeftNavTreeNode> parentNodeOptional = Optional.ofNullable(getParentNode(leftNavNodes, selectedNode));
          finalNode = parentNodeOptional.orElse(selectedNode);
        }
        leftNavNodes = Arrays.asList(finalNode);
      }
    }
    
    List<LeftNavTreeNode> filteredLeftNavCategories = filterByCountryCode(leftNavNodes, countryCode);
    
    if (isEmpty(filteredLeftNavCategories)) {
      return null;
    }
    
    return new LeftNavTree(categoryId, categoryName, leftNavNodeMapper.map(filteredLeftNavCategories), Collections.emptyList(), null);
  }
  
  private List<LeftNavTreeNode> filterByCountryCode(List<LeftNavTreeNode> leftNavCategories, String countryCode) {
    if (isEmpty(leftNavCategories)) {
      return leftNavCategories;
    }
    
    List<LeftNavTreeNode> filteredLeftNavCategories = leftNavCategories.stream().filter(leftNavCategory -> {
      List<String> excludedCountries = leftNavCategory.getExcludedCountries();
      return isEmpty(excludedCountries) || !excludedCountries.contains(countryCode);
    }).collect(Collectors.toList());
    for (LeftNavTreeNode leftNavTreeNode : filteredLeftNavCategories) {
      leftNavTreeNode.setCategories(filterByCountryCode(leftNavTreeNode.getCategories(), countryCode));
    }
    return filteredLeftNavCategories;
  }
  
  private LeftNavTreeNode getSelectedNode(List<LeftNavTreeNode> nodes) {
    if (nodes != null) {
      return nodes.stream().filter(Objects::nonNull).map(node -> node.isSelected() ? node : getSelectedNode(node.getCategories())).filter(Objects::nonNull).findFirst().orElse(null);
    } else {
      return null;
    }
  }
  
  private LeftNavTreeNode getParentNode(final List<LeftNavTreeNode> nodes, final LeftNavTreeNode childNode) {
    if (nodes != null) {
      return nodes.stream().filter(Objects::nonNull)
              .map(node -> CollectionUtils.containsInstance(node.getCategories(), childNode) ? node : getParentNode(node.getCategories(), childNode)).filter(Objects::nonNull).findFirst().orElse(null);
    } else {
      return null;
    }
  }
}
