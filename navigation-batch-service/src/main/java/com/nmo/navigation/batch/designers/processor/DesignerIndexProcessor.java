package com.sixthday.navigation.batch.designers.processor;

import com.sixthday.model.serializable.designerindex.*;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.repository.dynamodb.DesignerIndexRepository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.sixthday.sixthdayLogging.EventType.ON_EVENT;
import static com.sixthday.sixthdayLogging.OperationType.BUILD_DI;
import static com.sixthday.sixthdayLogging.logOperation;
import static org.springframework.web.util.HtmlUtils.htmlUnescape;

@Component
@Slf4j
public class DesignerIndexProcessor {

    public static final String BOUTIQUE_TEXT_ADORNMENT_OVERRIDE = "/category/templates/boutiqueOverride.png";
    private static final String VIEW_ALL_A_Z = "VIEW ALL A-Z";
    private static final String UNDER_SCORE = "_";

    private LeftNavTreeProcessor leftNavTreeProcessor;
    private DesignerIndexRepository designerIndexRepository;
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Autowired
    public DesignerIndexProcessor(LeftNavTreeProcessor leftNavTreeProcessor, DesignerIndexRepository designerIndexRepository,
                                  NavigationBatchServiceConfig navigationBatchServiceConfig) {
        this.leftNavTreeProcessor = leftNavTreeProcessor;
        this.designerIndexRepository = designerIndexRepository;
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
    }

    public DesignerIndex getDesignerIndex(String categoryId) {
        return designerIndexRepository.get(categoryId);
    }

    public boolean isRebuildDesignerIndex(CategoryDocument categoryDocument) {
        leftNavTreeProcessor.getCategoryDocumentMap().put(categoryDocument.getId(), categoryDocument);
        return categoryDocument != null && (
                isCategoryIdBecomeOneOfTheParent(categoryDocument.getParents()) ||
                        categoryDocument.isBoutique() ||
                        categoryDocument.isBoutiqueChild());
    }

    private boolean isCategoryIdBecomeOneOfTheParent(Map<String, Integer> parents) {
        if (!CollectionUtils.isEmpty(parents)) {
            String designerCategoryId = navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId();
            String designerByCategoryId = navigationBatchServiceConfig.getCategoryIdConfig().getDesignerByCategory();
            return parents.keySet().parallelStream().anyMatch(parentCategoryId -> parentCategoryId.equals(designerByCategoryId) || parentCategoryId.equals(designerCategoryId));
        }
        return false;
    }

    public void buildDesignerIndex() {
        logOperation(log, ON_EVENT, BUILD_DI, () -> {
            List<DesignerByCategory> designersByCategory = buildDesignerByCategoryObject();
            buildDesignerByCategoryIndexObject(designersByCategory);
            buildDesignerIndexObject(designersByCategory);
            return null;
        });
    }

    private List<DesignerByCategory> buildDesignerByCategoryObject() {

        final List<DesignerByCategory> designersByCategoryList = new ArrayList<>();

        Optional<CategoryDocument> optionalDesignerCategoryDocument = leftNavTreeProcessor.getCategoryDocument(navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId());
        optionalDesignerCategoryDocument.ifPresent(designerCategoryDocument -> designersByCategoryList.add(new DesignerByCategory(designerCategoryDocument.getId(), VIEW_ALL_A_Z, designerCategoryDocument.getCanonicalUrl())));

        Optional<CategoryDocument> designerByCategory = leftNavTreeProcessor.getCategoryDocument(navigationBatchServiceConfig.getCategoryIdConfig().getDesignerByCategory());
        if (designerByCategory.isPresent() && !CollectionUtils.isEmpty(designerByCategory.get().getChildren())) {
            for (String childCategoryId : designerByCategory.get().getChildren()) {
                Optional<CategoryDocument> childCategoryDocument = leftNavTreeProcessor.getCategoryDocument(childCategoryId);
                if (childCategoryDocument.isPresent()) {
                    ContextualProperty childCategoryDocumentContextualProperty = childCategoryDocument.get().getApplicablePropertiesForCategory(designerByCategory.get().getId());
                    designersByCategoryList.add(new DesignerByCategory(childCategoryId, childCategoryDocument.get().getCategoryName(childCategoryDocumentContextualProperty),
                            childCategoryDocument.get().getCanonicalUrl()));
                }
            }
        }

        return designersByCategoryList;
    }

    @SneakyThrows
    private void buildDesignerByCategoryIndexObject(List<DesignerByCategory> designersByCategory) {
        Optional<CategoryDocument> designerByCategory = leftNavTreeProcessor.getCategoryDocument(navigationBatchServiceConfig.getCategoryIdConfig().getDesignerByCategory());
        String designerCategoryId = navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId();
        String liveTreeCategoryId = navigationBatchServiceConfig.getIntegration().getCategoryType().get("live");
        if (designerByCategory.isPresent() && !CollectionUtils.isEmpty(designerByCategory.get().getChildren())) {
            for (String childCategoryId : designerByCategory.get().getChildren()) {

                DesignerIndex designerIndex = new DesignerIndex();
                designerIndex.setDesignersByCategory(new ArrayList<>());
                designerIndex.getDesignersByCategory().addAll(designersByCategory);
                designerIndex.setId(childCategoryId);

                Optional<CategoryDocument> childCategoryDocument = leftNavTreeProcessor.getCategoryDocument(childCategoryId);
                if (childCategoryDocument.isPresent() && !CollectionUtils.isEmpty(childCategoryDocument.get().getChildren())) {
                    ContextualProperty childCategoryDocumentContextualProperty = childCategoryDocument.get().getApplicablePropertiesForCategory(designerByCategory.get().getId());

                    designerIndex.setName(childCategoryDocument.get().getCategoryName(childCategoryDocumentContextualProperty));

                    Optional<TreeMap<String, DesignerCategory>> boutiqueCategories = getBoutiqueCategories(designerCategoryId, liveTreeCategoryId, childCategoryDocument.get());

                    sortByAlphaNumeric(designerIndex, boutiqueCategories.orElse(new TreeMap<>()));

                    designerIndexRepository.save(designerIndex);
                }
            }
        }
    }

    private Optional<TreeMap<String, DesignerCategory>> getBoutiqueCategories(String designerCategoryId, String liveTreeCategoryId, CategoryDocument childCategoryDocument) {
        return Optional.of(getAllBoutiqueCategories(childCategoryDocument.getChildren().get(0), liveTreeCategoryId + "_" + designerCategoryId));
    }

    @SneakyThrows
    private void buildDesignerIndexObject(List<DesignerByCategory> designersByCategory) {
        String designerCategoryId = navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId();
        String liveTreeCategoryId = navigationBatchServiceConfig.getIntegration().getCategoryType().get("live");

        DesignerIndex designerIndex = new DesignerIndex();
        designerIndex.setDesignersByCategory(new ArrayList<>());
        designerIndex.getDesignersByCategory().addAll(designersByCategory);
        designerIndex.setId(designerCategoryId);

        Optional<TreeMap<String, DesignerCategory>> boutiqueCategories = Optional.of(getAllBoutiqueCategories(designerCategoryId, liveTreeCategoryId + "_" + designerCategoryId));

        sortByAlphaNumeric(designerIndex, boutiqueCategories.orElse(new TreeMap<>()));

        designerIndexRepository.save(designerIndex);
    }

    private void sortByAlphaNumeric(DesignerIndex designerIndex, TreeMap<String, DesignerCategory> boutiqueCategories) {
        designerIndex.setDesignersByIndex(new ArrayList<>());
        String[] alphaNumericIndex = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
        for (String index : alphaNumericIndex) {
            DesignersByIndex designersByIndex = new DesignersByIndex();
            designersByIndex.setName(index);
            designersByIndex.setCategories(new ArrayList<>());
            designerIndex.getDesignersByIndex().add(designersByIndex);

            for (Map.Entry<String, DesignerCategory> entry : boutiqueCategories.entrySet()) {
                if (entry.getKey().toUpperCase().startsWith(index)) {
                    designersByIndex.getCategories().add(entry.getValue());
                } else if ("#".equals(index) && entry.getKey().matches("[_0-9](.*)")) {
                    designersByIndex.getCategories().add(entry.getValue());
                }
            }
        }
    }

    private TreeMap<String, DesignerCategory> getAllBoutiqueCategories(String grandChildCategoryId, String path) {
        TreeMap<String, DesignerCategory> boutiqueCategories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        Optional<CategoryDocument> grandChildCategoryDocument = leftNavTreeProcessor.getCategoryDocument(grandChildCategoryId);
        if (grandChildCategoryDocument.isPresent() && !CollectionUtils.isEmpty(grandChildCategoryDocument.get().getChildren())) {
            for (String designerCategoryId : grandChildCategoryDocument.get().getChildren()) {
                Optional<CategoryDocument> designerCategoryDocument = leftNavTreeProcessor.getCategoryDocument(designerCategoryId);
                if (designerCategoryDocument.isPresent() && designerCategoryDocument.get().isBoutique() && !designerCategoryDocument.get().hasNoResultsOrIsHiddenOrIsDeleted()) {
                    ContextualProperty designerCategoryDocumentContextualProperty = designerCategoryDocument.get().getApplicablePropertiesForCategory(grandChildCategoryDocument.get().getId());

                    String driveToPath = designerCategoryDocument.get().getDriveToSubCategoryId(designerCategoryDocumentContextualProperty, leftNavTreeProcessor);
                    String canonicalUrl = addQueryParameterToUrl(path, designerCategoryId, getCanonicalUrl(leftNavTreeProcessor, driveToPath, designerCategoryDocument.get()), driveToPath);

                    DesignerCategory boutiqueCategory = new DesignerCategory(designerCategoryId,
                            htmlUnescape(designerCategoryDocument.get().getCategoryName(designerCategoryDocumentContextualProperty)),
                            canonicalUrl,
                            getBoutiqueTextAdornments(designerCategoryDocument.get(), designerCategoryDocumentContextualProperty),
                            designerCategoryDocument.get().getExcludedCountries());
                    boutiqueCategories.put(designerCategoryDocument.get().getCategoryName(designerCategoryDocumentContextualProperty), boutiqueCategory);
                }
            }
        }
        return boutiqueCategories;
    }

    private String getBoutiqueTextAdornments(CategoryDocument categoryDocument, ContextualProperty contextualProperty) {
        if (categoryDocument.isBoutiqueTextAdornmentsOverride(contextualProperty)) {
            return BOUTIQUE_TEXT_ADORNMENT_OVERRIDE;
        } else {
            return categoryDocument.getBoutiqueTextAdornments(contextualProperty);
        }
    }

    private String addQueryParameterToUrl(String path, String designerCategoryId, String canonicalUrl, String driveToPath) {
        final String queryParam = "?navpath=";

        return "".concat(StringUtils.isEmpty(canonicalUrl) ? "" : canonicalUrl)
                .concat(queryParam)
                .concat(path)
                .concat(UNDER_SCORE)
                .concat(designerCategoryId)
                .concat(StringUtils.isEmpty(driveToPath) ? "" : UNDER_SCORE + driveToPath);
    }

    private String getCanonicalUrl(LeftNavTreeProcessor leftNavTreeProcessor, String driveToPath, CategoryDocument designerCategoryDocument) {
        if (!StringUtils.isEmpty(driveToPath)) {
            String[] ids = driveToPath.split(UNDER_SCORE);
            Optional<CategoryDocument> categoryDocument = leftNavTreeProcessor.getCategoryDocument(ids[ids.length - 1]);
            if (categoryDocument.isPresent()) {
                return categoryDocument.get().getCanonicalUrl();
            }
        }
        return designerCategoryDocument.getCanonicalUrl();
    }
}