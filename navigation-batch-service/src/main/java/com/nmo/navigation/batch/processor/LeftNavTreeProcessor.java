package com.sixthday.navigation.batch.processor;

import com.google.common.collect.Lists;
import com.sixthday.navigation.batch.io.CategoryDocumentReader;
import com.sixthday.navigation.batch.io.LeftNavBatchWriter;
import com.sixthday.navigation.batch.utils.LeftNavTreeProcessorUtil;
import com.sixthday.navigation.batch.vo.LeftNavTreeNode;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.documents.LeftNavDocument;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.elasticsearch.repository.LeftNavRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.sixthday.sixthdayLogging.EventType.BUILD_LEFTNAV_ON_STARTUP;
import static com.sixthday.sixthdayLogging.OperationType.*;
import static com.sixthday.sixthdayLogging.logDebugOperation;
import static com.sixthday.sixthdayLogging.logError;
import static com.sixthday.navigation.batch.vo.LeftNavTreeNode.SEPARATOR;
import static com.sixthday.navigation.config.Constants.*;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class LeftNavTreeProcessor {

    private LeftNavBatchWriter leftNavBatchWriter;
    private NavigationBatchServiceConfig navigationBatchServiceConfig;
    private LeftNavRepository leftNavRepository;
    private CategoryDocumentReader categoryDocumentReader;
    private CategoryRepository categoryRepository;
    private LeftNavTreeProcessorUtil leftNavTreeProcessorUtil;

    @Setter
    @Getter
    private Map<String, CategoryDocument> categoryDocumentMap;
    private Map<String, Set<String>> retryNodes;
    @Getter
    private String designersCategoryId;

    @Autowired
    public LeftNavTreeProcessor(LeftNavBatchWriter leftNavBatchWriter, NavigationBatchServiceConfig navigationBatchServiceConfig, LeftNavRepository leftNavRepository, CategoryDocumentReader categoryDocumentReader, CategoryRepository categoryRepository) {
        this.leftNavBatchWriter = leftNavBatchWriter;
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
        this.leftNavRepository = leftNavRepository;
        this.categoryDocumentReader = categoryDocumentReader;
        this.categoryRepository = categoryRepository;
        this.leftNavTreeProcessorUtil = new LeftNavTreeProcessorUtil();
        retryNodes = new ConcurrentHashMap<>();
        categoryDocumentMap = new ConcurrentHashMap<>();
        designersCategoryId = navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId();
    }

    @EventListener(value = ApplicationReadyEvent.class)
    protected void preLoadTheCategoriesAndBuildTheLeftNavDocuments() {
        if (navigationBatchServiceConfig.getLeftNavBatchConfig().isBuildOnStartup()) {
            buildTheLeftNavDocuments();
        }
    }

    private void buildTheLeftNavDocuments() {
        categoryDocumentMap.putAll(categoryDocumentReader.getAllCategories());
        try {
            logDebugOperation(log, BUILD_LEFTNAV_ON_STARTUP, BUILD_LEFTNAV, () -> {
                categoryDocumentMap.values().forEach(categoryDocument -> startByEvent(categoryDocument, false));
                return null;
            });
        } catch (Exception e) {
            logError(log, BUILD_LEFTNAV_ON_STARTUP, BUILD_LEFTNAV, "Unable to continue building leftnav on the start-up", e);
        }
    }

    public void startByEvent(String categoryId, boolean includeReferenceIdsPathToRebuild) {
        getCategoryDocument(categoryId).ifPresent(categoryDocument -> startByEvent(categoryDocument, includeReferenceIdsPathToRebuild));
    }

    public void startByEvent(CategoryDocument categoryDocument, boolean includeReferenceIdsPathToRebuild) {
        designersCategoryId = navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId();
        Set<String> pathsToRebuild = new HashSet<>();
        if (categoryDocument != null && !categoryDocument.isDeleted()) {
            logDebugOperation(log, null, FIND_PATHS, categoryDocument.getId(), () -> {
                categoryDocumentMap.put(categoryDocument.getId(), categoryDocument);
                getModifiedNodePaths(categoryDocument.getId()).ifPresent(pathsToRebuild::addAll);
                getPathFromSiblingCategories(categoryDocument).ifPresent(pathsToRebuild::addAll);
                getRetryNodes(categoryDocument.getId()).ifPresent(paths -> paths.forEach(path ->
                        getModifiedNodePaths(path).ifPresent(pathsToRebuild::addAll)
                ));
                removeRetryNodes(categoryDocument.getId());
                if (includeReferenceIdsPathToRebuild) {
                    pathsToRebuild.addAll(leftNavRepository.getPathsByReferenceId(categoryDocument.getId()));
                }
                return null;
            });
            buildLeftNavDocuments(pathsToRebuild);
        }
    }

    @SneakyThrows
    private void buildLeftNavDocuments(Set<String> paths) {
        List<List<String>> batches = Lists.partition(new ArrayList<>(paths), navigationBatchServiceConfig.getLeftNavBatchConfig().getWriteBatchSize());

        for (List<String> batch : batches) {
            List<LeftNavDocument> leftNavDocuments = new ArrayList<>();

            logDebugOperation(log, null, BUILD_LEFTNAV, "Building LeftNavDocument for " + paths.size() + " paths", () -> {
                batch.stream().map(this::buildLeftNavDocumentByPath).forEach(leftNavDocument -> leftNavDocument.ifPresent(leftNavDocuments::add));
                return null;
            });

            if (!CollectionUtils.isEmpty(leftNavDocuments)) {
                logDebugOperation(log, null, SAVE_LEFTNAV, "Saving " + leftNavDocuments.size() + " LeftNavDocuments", () -> {
                    leftNavBatchWriter.saveLeftNavDocuments(leftNavDocuments);
                    return null;
                });
            }
        }
    }

    private Optional<LeftNavDocument> buildLeftNavDocumentByPath(String actualPath) {
        List<String> categoryIds = Arrays.asList(actualPath.split(SEPARATOR));

        if (!actualPath.startsWith(navigationBatchServiceConfig.getIntegration().getCategoryType().get(LIVE)) &&
                !actualPath.startsWith(navigationBatchServiceConfig.getIntegration().getCategoryType().get(STAGE)) &&
                !actualPath.startsWith(navigationBatchServiceConfig.getIntegration().getCategoryType().get(MARKETING))) {
            return Optional.empty();
        }

        LeftNavDocument leftNavDocument = new LeftNavDocument();

        int categoryIndex = updateLeftNavDocument(leftNavDocument, actualPath);

        return buildLeftNavDocumentByType(actualPath, categoryIds, leftNavDocument, categoryIndex);
    }

    private Optional<LeftNavDocument> buildLeftNavDocumentByType(String actualPath, List<String> categoryIds, LeftNavDocument leftNavDocument, int categoryIndex) {
        if (categoryIndex >= 0) {
            if (categoryIndex == 0) {
                if (actualPath.startsWith(navigationBatchServiceConfig.getIntegration().getCategoryType().get(LIVE)) && categoryIds.size() > 1) {
                    String currentPath = String.join("_", categoryIds.subList(0, 1));
                    leftNavDocument.setLeftNav(buildLeftNavForACategory(categoryIds.get(0), categoryIds.get(1), new LeftNavTreeNode(), currentPath, actualPath, leftNavDocument).getCategories());
                } else if (actualPath.startsWith(navigationBatchServiceConfig.getIntegration().getCategoryType().get(STAGE)) && categoryIds.size() > 1) {
                    leftNavDocument.setLeftNav(Collections.singletonList(buildLeftNavForACategory(null, categoryIds.get(0), new LeftNavTreeNode(), categoryIds.get(0), actualPath, leftNavDocument)));
                } else if (actualPath.startsWith(navigationBatchServiceConfig.getIntegration().getCategoryType().get(MARKETING)) && categoryIds.size() >= 2) {
                    String currentPath = String.join("_", categoryIds.subList(0, categoryIds.size() - 1));
                    leftNavDocument.setLeftNav(Collections.singletonList(buildLeftNavForACategory(categoryIds.get(categoryIds.size() - 2), categoryIds.get(categoryIds.size() - 1), new LeftNavTreeNode(), currentPath, actualPath, leftNavDocument)));
                }
            } else {
                String currentPath = String.join("_", categoryIds.subList(0, categoryIndex));
                leftNavDocument.setLeftNav(Collections.singletonList(buildLeftNavForACategory(categoryIds.get(categoryIndex - 1), categoryIds.get(categoryIndex), new LeftNavTreeNode(), currentPath, actualPath, leftNavDocument)));
            }
            return Optional.of(leftNavDocument);
        } else {
            return Optional.empty();
        }
    }

    private int updateLeftNavDocument(LeftNavDocument leftNavDocument, String actualPath) {
        List<String> categoryIds = Arrays.asList(actualPath.split(SEPARATOR));
        Optional<CategoryDocument> categoryDocument = getCategoryDocument(categoryIds.get(categoryIds.size() - 1));
        int designerIndex = -1;

        if (categoryDocument.isPresent()) {
            leftNavDocument.setId(actualPath);
            leftNavDocument.setCategoryId(categoryDocument.get().getId());
            leftNavDocument.setName(categoryDocument.get().getName());
            leftNavDocument.setRefreshablePath(getRefreshablePath(categoryDocument.get()));
            leftNavDocument.setDriveToPath(getDriveToNavPath(actualPath, categoryIds, categoryDocument.get()));

            designerIndex = getDesignerObjectAndIndex(leftNavDocument, categoryIds, categoryDocument);
            if (leftNavDocument.getBoutiqueLeftNav() == null) {
                leftNavDocument.setBoutiqueLeftNav(Collections.emptyList());
            }
        } else {
            log.warn("Category [SELECTED CATEGORY] {} is not found while building the LeftNav document, updating the retry queue", categoryIds.get(categoryIds.size() - 1));
        }
        return designerIndex;
    }

    private int getDesignerObjectAndIndex(LeftNavDocument leftNavDocument, List<String> categoryIds, Optional<CategoryDocument> categoryDocument) {
        int designerIndex;
        int index = 0;
        designerIndex = 0;

        for (String categoryId : categoryIds) {
            Optional<CategoryDocument> categoryDocumentFromPath = getCategoryDocument(categoryId);
            if (categoryDocumentFromPath.isPresent() && categoryDocumentFromPath.get().isBoutique()) {
                if (!"ChanelP3".equals(categoryDocument.get().getTemplateType())) {
                    LeftNavTreeNode boutiqueLeftNavTreeNode = new LeftNavTreeNode();
                    boutiqueLeftNavTreeNode.setId(designersCategoryId);
                    boutiqueLeftNavTreeNode.setName("Shop All Designers");
                    boutiqueLeftNavTreeNode.setUrl("/Designers/" +designersCategoryId+ "/c.cat");
                    leftNavDocument.setBoutiqueLeftNav(Collections.singletonList(boutiqueLeftNavTreeNode));
                }
                designerIndex = index;
                break;
            }
            index++;
        }
        return designerIndex;
    }

    private LeftNavTreeNode buildLeftNavForACategory(String parentId, String categoryId, LeftNavTreeNode leftNavTreeNode, String currentPath, String actualPath, LeftNavDocument leftNavDocument) {
        List<String> categoryIds = Arrays.asList(actualPath.split(SEPARATOR));
        Optional<CategoryDocument> categoryDocument = getCategoryDocument(categoryId);
        if (categoryDocument.isPresent()) {
            if (!categoryDocument.get().hasNoResultsOrIsHiddenOrIsDeleted()) {
                leftNavTreeNode.setId(categoryDocument.get().getId());
                ContextualProperty contextualProperty = categoryDocument.get().getApplicablePropertiesForCategory(parentId);
                leftNavTreeNode.setName(categoryDocument.get().getCategoryName(contextualProperty));
                String path = currentPath;
                if (!currentPath.equals(categoryDocument.get().getId())) {
                    path = currentPath.concat("_").concat(categoryDocument.get().getId());
                }
                leftNavTreeNode.setPath(path);
                setUrl(categoryId, leftNavTreeNode, path, categoryIds, categoryDocument.get());
                leftNavTreeNode.setExcludedCountries(categoryDocument.get().getExcludedCountries());
                leftNavTreeNode.setRedText(categoryDocument.get().isRedTextAvailable(contextualProperty));
                if (actualPath.endsWith(categoryId)) {
                    leftNavTreeNode.setSelected(true);
                }
                if (!categoryDocument.get().isDontShowChildren() && categoryDocument.get().getChildren() != null) {
                    buildLeftNavUsingChildCategories(leftNavTreeNode, path, actualPath, leftNavDocument, categoryDocument.get(), contextualProperty);
                }
            }
        } else {
            leftNavDocument.getReferenceIds().add(categoryId);
            log.warn("Category [PARENT] {} is not found while building the LeftNav document, updating the retry queue", categoryId);
            String lastCategoryId = categoryIds.get(categoryIds.size() - 1);
            updateRetryNode(categoryId, lastCategoryId);
        }
        return leftNavTreeNode;
    }

    private void setUrl(String categoryId, LeftNavTreeNode leftNavTreeNode, String currentPath, List<String> categoryIds, CategoryDocument categoryDocument) {
        if (categoryDocument.isBoutique()) {
            Optional<CategoryDocument> designersCategoryDocument = getCategoryDocument(designersCategoryId);
            if (designersCategoryDocument.isPresent()) {
                String designersCategoryCurrentPath = categoryIds.get(0).concat("_").concat(designersCategoryId).concat("_").concat(categoryId);
                leftNavTreeNode.setUrl(leftNavTreeProcessorUtil.getUrl(designersCategoryCurrentPath, categoryDocument, categoryDocumentMap));
            } else {
                log.warn("Category [DESIGNERS] {} is not found while building the LeftNav document, updating the retry queue", designersCategoryId);
                updateRetryNode(designersCategoryId, categoryId);
            }
        } else {
            leftNavTreeNode.setUrl(leftNavTreeProcessorUtil.getUrl(currentPath, categoryDocument, categoryDocumentMap));
        }
    }

    private void buildLeftNavUsingChildCategories(LeftNavTreeNode leftNavTreeNode, String currentPath, String actualPath, LeftNavDocument leftNavDocument, CategoryDocument categoryDocument, ContextualProperty contextualProperty) {
        List<String> categoryIds = Arrays.asList(actualPath.split(SEPARATOR));
        List<LeftNavTreeNode> categories = new ArrayList<>();
        leftNavTreeNode.setCategories(categories);
        for (String childCategoryId : categoryDocument.getChildCategoryOrder(contextualProperty)) {
            Optional<CategoryDocument> childCategoryDocument = getCategoryDocument(childCategoryId);
            leftNavDocument.getReferenceIds().add(childCategoryId);
            if (childCategoryDocument.isPresent()) {
                if (!childCategoryDocument.get().hasNoResultsOrIsHiddenOrIsDeleted()) {
                    buildLeftNavUsingValidChildCategories(currentPath, actualPath, leftNavDocument, categoryDocument, categoryIds, categories, childCategoryDocument);
                }
            } else {
                log.warn("Category [CHILD] {} is not found while building the LeftNav document, updating the retry queue", childCategoryId);
                List<String> categoryIdss = Arrays.asList(actualPath.split("_"));
                String lastCategoryId = categoryIdss.get(categoryIdss.size() - 1);
                updateRetryNode(childCategoryId, lastCategoryId);
            }
        }
    }

    private void buildLeftNavUsingValidChildCategories(String currentPath, String actualPath, LeftNavDocument leftNavDocument, CategoryDocument categoryDocument, List<String> categoryIds, List<LeftNavTreeNode> categories, Optional<CategoryDocument> childCategoryDocument) {
        if (categoryIds.indexOf(childCategoryDocument.get().getId()) > -1 && !childCategoryDocument.get().isDontShowChildren()) {
            categories.add(buildLeftNavForACategory(categoryDocument.getId(), childCategoryDocument.get().getId(), new LeftNavTreeNode(), currentPath, actualPath, leftNavDocument));
        } else {
            buildLeftNavTreeNode(currentPath, actualPath, leftNavDocument, categoryDocument, categories, childCategoryDocument.get().getId(), childCategoryDocument.get());
        }
    }

    private void buildLeftNavTreeNode(String currentPath, String actualPath, LeftNavDocument leftNavDocument, CategoryDocument categoryDocument, List<LeftNavTreeNode> categories, String childCategoryId, CategoryDocument childCategoryDocument) {
        List<String> categoryIds = Arrays.asList(actualPath.split(SEPARATOR));
        LeftNavTreeNode childLeftNavTreeNode = new LeftNavTreeNode();
        childLeftNavTreeNode.setId(childCategoryDocument.getId());
        ContextualProperty childContextualProperty = childCategoryDocument.getApplicablePropertiesForCategory(categoryDocument.getId());
        childLeftNavTreeNode.setName(childCategoryDocument.getCategoryName(childContextualProperty));
        childLeftNavTreeNode.setPath(currentPath + "_" + childCategoryId);
        childLeftNavTreeNode.setUrl(leftNavTreeProcessorUtil.getUrl(childLeftNavTreeNode.getPath(), categoryDocument, categoryDocumentMap));
        childLeftNavTreeNode.setExcludedCountries(childCategoryDocument.getExcludedCountries());
        childLeftNavTreeNode.setRedText(childCategoryDocument.isRedTextAvailable(childContextualProperty));
        List<String> selectedNodeChildren = getCategoryDocument(categoryIds.get(categoryIds.size() - 1)).orElse(new CategoryDocument()).getChildren();
        if (selectedNodeChildren != null && selectedNodeChildren.indexOf(childCategoryId) > -1 && childCategoryDocument.isExpandCategory()) {
            childLeftNavTreeNode.setCategories(getCategories(childCategoryDocument, childLeftNavTreeNode.getPath(), childCategoryId, leftNavDocument));
        }
        if (actualPath.endsWith(childCategoryId)) {
            childLeftNavTreeNode.setSelected(true);
        }
        categories.add(childLeftNavTreeNode);
    }

    @NotNull
    private List<LeftNavTreeNode> getCategories(CategoryDocument categoryDocument, String currentPath, String parentId, LeftNavDocument referenceNodes) {
        List<LeftNavTreeNode> categories = new ArrayList<>();
        ContextualProperty contextualProperty = categoryDocument.getApplicablePropertiesForCategory(parentId);
        if (categoryDocument.getChildren() != null) {
            for (String childCategoryId : categoryDocument.getChildCategoryOrder(contextualProperty)) {
                Optional<CategoryDocument> childCategoryDocument = getCategoryDocument(childCategoryId);
                if (childCategoryDocument.isPresent()) {
                    getCategoriesForValidCategory(currentPath, parentId, referenceNodes, categories, childCategoryId, childCategoryDocument);
                } else {
                    log.warn("Category [EXPAND_CHILD] {} is not found while building the LeftNav document, updating the retry queue", childCategoryId);
                }
            }
        }
        return categories;
    }

    private void getCategoriesForValidCategory(String currentPath, String parentId, LeftNavDocument referenceNodes, List<LeftNavTreeNode> categories, String childCategoryId, Optional<CategoryDocument> childCategoryDocument) {
        if (!childCategoryDocument.get().hasNoResultsOrIsHiddenOrIsDeleted()) {
            referenceNodes.getReferenceIds().add(childCategoryId);
            ContextualProperty childContextualProperty = childCategoryDocument.get().getApplicablePropertiesForCategory(parentId);
            LeftNavTreeNode childLeftNavTreeNode = new LeftNavTreeNode();
            childLeftNavTreeNode.setId(childCategoryDocument.get().getId());
            childLeftNavTreeNode.setName(childCategoryDocument.get().getCategoryName(childContextualProperty));
            childLeftNavTreeNode.setPath(currentPath + "_" + childCategoryId);
            childLeftNavTreeNode.setUrl(leftNavTreeProcessorUtil.getUrl(currentPath + "_" + childCategoryId, childCategoryDocument.get(), categoryDocumentMap));
            childLeftNavTreeNode.setRedText(childCategoryDocument.get().isRedTextAvailable(childContextualProperty));
            childLeftNavTreeNode.setExcludedCountries(childCategoryDocument.get().getExcludedCountries());
            categories.add(childLeftNavTreeNode);
        }
    }

    private Optional<Set<String>> getModifiedNodePaths(String categoryId) {
        return traverseNode(categoryId, categoryId, categoryId, new HashSet<>());
    }

    private Optional<Set<String>> getPathFromSiblingCategories(CategoryDocument categoryDocument) {
        Set<String> paths = new HashSet<String>();
        final Map<String, Integer> parents = categoryDocument.getParents();
        Optional<CategoryDocument> parentCategoryDocument = null;
        for (Map.Entry<String, Integer> entry : parents.entrySet()) {
            parentCategoryDocument = getCategoryDocument(entry.getKey());
            if (parentCategoryDocument.isPresent()) {
                paths.addAll(leftNavRepository.getCategorySiblingsPath(parentCategoryDocument.get().getDefaultPath()));
            }
        }
        return Optional.of(paths);
    }

    private Optional<Set<String>> traverseNode(String categoryId, String modifiedCategoryId, String path, Set<String> paths) {
        Optional<CategoryDocument> categoryDocument = getCategoryDocument(categoryId);

        if (!categoryDocument.isPresent()) {
            updateRetryNode(categoryId, modifiedCategoryId);
            return Optional.empty();
        }

        if (categoryDocument.get().hasNoResultsOrIsHiddenOrIsDeleted()) {
            return Optional.empty();
        }

        Map<String, Integer> parents = categoryDocument.get().getParents();
        if (isEmpty(parents)) {
            paths.add(path);
        }

        if (!isEmpty(parents)) {
            for (Map.Entry<String, Integer> entry : parents.entrySet()) {
                traverseNode(entry.getKey(), modifiedCategoryId, entry.getKey() + "_" + path, paths);
            }
        }
        return Optional.of(paths);
    }

    private String getRefreshablePath(CategoryDocument categoryDocument) {
        if (categoryDocument.isLeftNavImageAvailable()) {
            if (!isBlank(categoryDocument.getLeftNavImageAvailableOverride())) {
                return navigationBatchServiceConfig.getLeftNavConfig().getLeftNavRefreshablePath().replace("{refreshableCatId}", categoryDocument.getLeftNavImageAvailableOverride());
            }
            return navigationBatchServiceConfig.getLeftNavConfig().getLeftNavRefreshablePath().replace("{refreshableCatId}", categoryDocument.getId());
        }
        return null;
    }

    private String getDriveToNavPath(String path, List<String> listOfCategoryIds, CategoryDocument selectedCategory) {
        String driveToPath = path;
        if (listOfCategoryIds.size() >= 2) {
            String parentOfSelectedCategory = listOfCategoryIds.get(listOfCategoryIds.size() - 2);
            ContextualProperty childContextualProperty = selectedCategory.getApplicablePropertiesForCategory(parentOfSelectedCategory);
            driveToPath = selectedCategory.getDriveToSubCategoryId(childContextualProperty, this);
            if (!StringUtils.isEmpty(driveToPath)) {
                driveToPath = isValidateDriveToPath(Arrays.asList(driveToPath.split("_")), listOfCategoryIds, selectedCategory) ? driveToPath : "";
            }
        }
        return driveToPath.isEmpty() ? "" : path + SEPARATOR + driveToPath;
    }

    private boolean isValidateDriveToPath(List<String> driveToIds, List<String> listOfCategoryIds, CategoryDocument selectedCategory) {
        if (driveToIds.size() == 1 && selectedCategory.getChildren().indexOf(driveToIds.get(0)) == -1) {
            return false;
        } else if (driveToIds.size() == 2) {
            Optional<CategoryDocument> categoryDocument = getCategoryDocument(driveToIds.get(0));
            if (categoryDocument.isPresent()) {
                return categoryDocument.get().getChildren() != null && categoryDocument.get().getChildren().indexOf(driveToIds.get(1)) != -1;
            } else {
                log.warn("Category [DRIVE_TO] {} is not found while building the LeftNav document, updating the retry queue", driveToIds.get(1));
                updateRetryNode(driveToIds.get(1), listOfCategoryIds.get(listOfCategoryIds.size() - 1));
            }
        }
        return true;
    }

    private void updateRetryNode(String categoryId, String modifiedCategoryId) {
        Set<String> impactedNodes = CollectionUtils.isEmpty(retryNodes.get(categoryId)) ? new CopyOnWriteArraySet<>() : retryNodes.get(categoryId);
        if (!impactedNodes.contains(modifiedCategoryId)) {
            impactedNodes.add(modifiedCategoryId);
        }
        retryNodes.put(categoryId, impactedNodes);
    }

    public Map<String, Set<String>> getRetryNodes() {
        return retryNodes;
    }

    public Optional<CategoryDocument> getCategoryDocument(String categoryId) {
        CategoryDocument categoryDocument = categoryDocumentMap.get(categoryId);
        if (categoryDocument == null) {
            categoryDocument = categoryRepository.getCategoryDocument(categoryId);
            if (categoryDocument == null) {
                return Optional.empty();
            }
            categoryDocumentMap.put(categoryId, categoryDocument);
        }
        return Optional.of(categoryDocument);
    }

    public void reprocessPendingNodes() {
        retryNodes.forEach((key, value) -> value.forEach(categoryId -> {
            CategoryDocument categoryDocument = categoryDocumentMap.get(categoryId);
            if (categoryDocument != null) {
                startByEvent(categoryDocument, false);
                value.remove(categoryId);
            }
        }));
    }

    public void buildLeftNav() {
        buildTheLeftNavDocuments();
    }

    private Optional<Set<String>> getRetryNodes(String modifiedCategoryId) {
        Set<String> nodes = retryNodes.get(modifiedCategoryId);
        return CollectionUtils.isEmpty(nodes) ? Optional.empty() : Optional.of(nodes);
    }

    private void removeRetryNodes(String modifiedCategoryId) {
        retryNodes.remove(modifiedCategoryId);
    }
}