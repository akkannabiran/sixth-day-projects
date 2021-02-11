package com.sixthday.navigation.batch.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import com.sixthday.navigation.batch.vo.*;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.Silos;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.IntStream;

import static com.sixthday.sixthdayLogging.OperationType.TRANSFORM_MOBILE_SILO;
import static com.sixthday.sixthdayLogging.logOperation;

@Slf4j
@Component
public class MobileSiloNavTreeProcessor extends AbstractSiloNavTreeProcessor {
    private static final String NUMBER_KEY = "#";
    private static Long counterId;

    @Autowired
    public MobileSiloNavTreeProcessor(NavigationBatchServiceConfig navigationBatchServiceConfig, CategoryRepository categoryRepository) {
        super(navigationBatchServiceConfig, categoryRepository);
    }

    @SneakyThrows
    @Override
    public SiloNavTreeProcessorResponse process(SiloNavTreeReaderResponse siloNavTreeReaderResponse, ObjectMapper objectMapper) {
        return logOperation(log, null, TRANSFORM_MOBILE_SILO, () -> {
            counterId = 0L;
            MobileNavTreeNode navTreeNode;
            try {
                navTreeNode = objectMapper.readValue(siloNavTreeReaderResponse.getNavTree(), MobileNavTreeNode.class);
            } catch (Exception e) {
                throw new NavigationBatchServiceException(e.getMessage(), e);
            }

            int level = -1;
            CategoryNode categoryNode = traverseRoot(navTreeNode, level, "");
            Silos silos = new Silos();
            silos.setSilosTree(categoryNode.getCategories());
            try {
                return new SiloNavTreeProcessorResponse(siloNavTreeReaderResponse.getCountryCode(), "mobile", objectMapper.writeValueAsString(silos), siloNavTreeReaderResponse.getNavKeyGroup());
            } catch (Exception e) {
                throw new NavigationBatchServiceException(e.getMessage(), e);
            }
        });
    }

    private CategoryNode traverseRoot(MobileNavTreeNode node, int rootLevel, String pathSoFar) {
        CategoryNode categoryNode = new CategoryNode();
        categoryNode.setName(node.getDisplayName().trim());
        categoryNode.setId(getNextNodeId());
        categoryNode.setCatmanId(node.getId());
        String navPath = buildNavPath(pathSoFar, categoryNode.getCatmanId());

        navPath = updateNavPathWithDriveTo(categoryNode.getCatmanId(), pathSoFar, navPath, node.getUrl());

        categoryNode.setUrl(buildUrl(node.getUrl(), navPath));
        categoryNode.setLevel(rootLevel);

        Map<String, Object> attributes = new HashMap<>();
        evaluateObjectAndSetAttributesValue(attributes, "textAdornment", node.getTextAdornment());
        evaluateObjectAndSetAttributesValue(attributes, "redTextFlag", node.getRedTextFlag());
        evaluateObjectAndSetAttributesValue(attributes, "flgBoutiqueTextAdornmentsOverride", node.getFlgBoutiqueTextAdornmentsOverride());
        evaluateCollectionAndSetAttributesValue(attributes, "tags", node.getTags());

        categoryNode.setAttributes(attributes);

        if (categoryNode.getCatmanId().equals(getNavigationBatchServiceConfig().getCategoryIdConfig().getDesignerCategoryId())) {
            categoryNode.setCategories(traverseDesigners(node.getChildren(), categoryNode.getLevel() + 1, navPath));
        } else {
            categoryNode.setCategories(traverseChild(node.getChildren(), categoryNode.getLevel() + 1, navPath));
        }
        return categoryNode;
    }

    private String buildUrl(String url, String navPath) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }
        return url.contains("?") ? url + "&navpath=" + navPath : url + "?navpath=" + navPath;
    }

    private String getNextNodeId() {
        return Long.toString(counterId++);
    }

    private void evaluateObjectAndSetAttributesValue(Map<String, Object> attributes, String key, Object value) {
        if (!StringUtils.isEmpty(value)) {
            attributes.put(key, value);
        }
    }

    private void evaluateCollectionAndSetAttributesValue(Map<String, Object> attributes, String key, Collection<?> value) {
        if (!CollectionUtils.isEmpty(value)) {
            attributes.put(key, value);
        }
    }

    private List<CategoryNode> traverseChild(List<MobileNavTreeNode> nodes, int level, String pathSoFar) {
        List<CategoryNode> categoryNodes = new ArrayList<>();
        if (nodes != null) {
            for (MobileNavTreeNode node : nodes) {
                categoryNodes.add(traverseRoot(node, level, pathSoFar));
            }
        }
        return categoryNodes;
    }

    private List<CategoryNode> traverseDesigners(List<MobileNavTreeNode> designers, int level, String pathSoFar) {
        Map<String, List<MobileNavTreeNode>> designerMap = new HashMap<>();
        List<CategoryNode> trees = new ArrayList<>();

        if (!CollectionUtils.isEmpty(designers)) {
            designers.forEach(node -> addToDesignerMap(designerMap, node.getDisplayName(), node));

            IntStream.rangeClosed('A', 'Z')
                    .mapToObj(c -> Character.toString((char) c))
                    .forEach(key -> trees.add(createAlphaCategory(key, level, designerMap.get(key), pathSoFar)));

            trees.add(createAlphaCategory(NUMBER_KEY, level, designerMap.get(NUMBER_KEY), pathSoFar));
        }
        return trees;
    }

    private void addToDesignerMap(Map<String, List<MobileNavTreeNode>> designerMap, String name, MobileNavTreeNode children) {
        String firstLetterAsKey = name.trim().substring(0, 1).toUpperCase();

        if (Ints.tryParse(firstLetterAsKey) == null) {
            if (!designerMap.containsKey(firstLetterAsKey)) {
                designerMap.put(firstLetterAsKey, new ArrayList<>());
            }
            designerMap.get(firstLetterAsKey).add(children);
        } else {
            if (!designerMap.containsKey(NUMBER_KEY)) {
                designerMap.put(NUMBER_KEY, new ArrayList<>());
            }
            designerMap.get(NUMBER_KEY).add(children);
        }
    }

    private CategoryNode createAlphaCategory(String key, int level, List<MobileNavTreeNode> nodes, String pathSoFar) {
        CategoryNode alphaNode = new CategoryNode();
        alphaNode.setCatmanId(key);
        alphaNode.setId(getNextNodeId());
        alphaNode.setName(key);
        alphaNode.setLevel(level);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("designerAlphaCategory", true);
        alphaNode.setAttributes(attributes);

        if (nodes == null) {
            alphaNode.setCategories(new ArrayList<>());
        } else {
            List<CategoryNode> children = new ArrayList<>();
            nodes.forEach(node -> children.add(traverseRoot(node, level + 1, pathSoFar)));
            alphaNode.setCategories(children);
        }
        String index = NUMBER_KEY.equals(key) ? "NUMBER" : key;
        alphaNode.setUrl(buildUrl("/Designers/" + getNavigationBatchServiceConfig().getCategoryIdConfig().getDesignerCategoryId() + "/c.cat?dIndex=" + index + "&drawer=Designer", buildNavPath(pathSoFar, alphaNode.getCatmanId())));
        return alphaNode;
    }
}
