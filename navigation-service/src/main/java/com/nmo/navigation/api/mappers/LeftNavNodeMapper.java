package com.sixthday.navigation.api.mappers;

import com.sixthday.navigation.api.models.response.LeftNavNode;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Component
public class LeftNavNodeMapper {
    public List<LeftNavNode> map(List<LeftNavTreeNode> leftNavCategories) {
        if (isEmpty(leftNavCategories)) {
            return emptyList();
        }
        return leftNavCategories.stream().map(this::map)
                .collect(Collectors.toList());
    }

    public LeftNavNode map(LeftNavTreeNode leftNavTreeNode) {
        return new LeftNavNode(leftNavTreeNode.getId(), leftNavTreeNode.getName(), leftNavTreeNode.getUrl(), leftNavTreeNode.getLevel(),
                map(leftNavTreeNode.getCategories()), leftNavTreeNode.getPath(),
                leftNavTreeNode.isRedText(), leftNavTreeNode.isSelected());
    }
}
