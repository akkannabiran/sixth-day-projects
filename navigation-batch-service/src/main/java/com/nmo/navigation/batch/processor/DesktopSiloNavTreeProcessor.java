package com.sixthday.navigation.batch.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.batch.vo.*;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.content.repository.ContentServiceRepository;
import com.sixthday.navigation.domain.Silos;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.sixthday.sixthdayLogging.OperationType.SILO_SCHEDULER;
import static com.sixthday.sixthdayLogging.OperationType.TRANSFORM_DESKTOP_SILO;
import static com.sixthday.sixthdayLogging.logError;
import static com.sixthday.sixthdayLogging.logOperation;
import static com.sixthday.navigation.config.Constants.NAV_PATH_PARAMETER_NAME;
import static com.sixthday.navigation.util.UrlUtil.appendQueryParameterToUrl;

@Slf4j
@Component
public class DesktopSiloNavTreeProcessor extends AbstractSiloNavTreeProcessor {

    static final String ATG_FAILURE_EXCEPTION_MESSAGE = "Nav API response from ATG was not a success and hence the silos data from API will be ignored";

    private static final String SILO_DISPLAY_NAME = "DisplayName";
    private static final String CATEGORY_DISPLAY_NAME = "CategoryDisplayName";
    private static final String CATEGORIES = "Categories";
    private static final String COLUMNS = "columns";
    private static final String SILOS = "silos";
    private static final String ID = "id";
    private static final String LEVEL = "level";
    private static final String SILO = "silo";
    private static final String ATTRIBUTES = "attributes";
    private static final String URL = "url";

    private ContentServiceRepository contentServiceRepository;

    @Autowired
    public DesktopSiloNavTreeProcessor(NavigationBatchServiceConfig navigationBatchServiceConfig, CategoryRepository categoryRepository,
                                       ContentServiceRepository contentServiceRepository) {
        super(navigationBatchServiceConfig, categoryRepository);
        this.contentServiceRepository = contentServiceRepository;
    }

    @SneakyThrows
    @Override
    public SiloNavTreeProcessorResponse process(SiloNavTreeReaderResponse item, ObjectMapper objectMapper) {
        int level = 0;
        int columnIndex = 0;
        Silos silos = new Silos();
        String status = "status";
        String success = "success";
        return logOperation(log, null, TRANSFORM_DESKTOP_SILO, () -> {
            JsonNode node;
            try {
                node = objectMapper.readTree(item.getNavTree());
            } catch (Exception e) {
                throw new NavigationBatchServiceException(e.getMessage(), e);
            }
            if (success.equalsIgnoreCase(node.get(status).asText())) {
                silos.setSilosTree(traverseCategories(node.get(SILOS), level, columnIndex, getNavigationBatchServiceConfig().getIntegration().getCategoryType().get("live")));
            } else {
                throw new NavigationBatchServiceException(ATG_FAILURE_EXCEPTION_MESSAGE);
            }
            try {
                postProcess(silos);
                return new SiloNavTreeProcessorResponse(item.getCountryCode(), "desktop", objectMapper.writeValueAsString(silos), item.getNavKeyGroup());
            } catch (Exception e) {
                throw new NavigationBatchServiceException(e.getMessage(), e);
            }
        });
    }

    private void postProcess(Silos silos) {
        updateSiloDrawerAsset(silos);
    }

    private void updateSiloDrawerAsset(Silos silos) {
        try {
            List<String> siloIds = silos.getSilosTree().stream().map(CategoryNode::getId).collect(Collectors.toList());
            Map<String, Object> assetsFromAEM = contentServiceRepository.getSiloDrawerAsset(siloIds);
            silos.getSilosTree().forEach(silosTree -> {
                if (assetsFromAEM.get(silosTree.getId()) != null) {
                    silosTree.getAttributes().put("aem", assetsFromAEM.get(silosTree.getId()));
                }
            });
        } catch (Exception e) {
            logError(log, null, SILO_SCHEDULER,
                    "Updating silos with AEM asset failed, message " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    private List<CategoryNode> traverseCategories(JsonNode categories, int level, int columnIndex, String pathSoFar) {
        List<CategoryNode> categoryNodes = new ArrayList<>();
        int arrayCounter = 0;
        for (JsonNode category : categories) {
            Map<String, Object> attributes = new HashMap<>();
            CategoryNode categoryNode = new CategoryNode();
            categoryNode.setId(category.get(ID).asText());
            categoryNode.setCatmanId(category.get(ID).asText());
            categoryNode.setLevel(level);
            categoryNode.setName(level > 0 ? category.get(LEVEL + level + CATEGORY_DISPLAY_NAME).asText() : category.get(SILO + SILO_DISPLAY_NAME).asText());
            setAttributes(columnIndex, arrayCounter, category, attributes, categoryNode);

            String navPath = buildNavPath(pathSoFar, categoryNode.getId());
            String navPathForChildren = navPath;

            if (categoryNode.getId().equals(getNavigationBatchServiceConfig().getCategoryIdConfig().getDesignerCategoryId())) {
                navPathForChildren = pathSoFar;
            }

            navPath = updateNavPathWithDriveTo(categoryNode.getId(), pathSoFar, navPath, category.get(URL).asText());

            categoryNode.setUrl(appendQueryParameterToUrl(category.get(ID).asText(), category.get(URL).asText(), NAV_PATH_PARAMETER_NAME, navPath));
            setCategories(level, category, categoryNode, navPathForChildren);
            categoryNodes.add(categoryNode);
            arrayCounter++;
        }
        return categoryNodes;
    }

    private void setAttributes(int columnIndex, int arrayCounter, JsonNode category, Map<String, Object> attributes, CategoryNode categoryNode) {
        Iterator<Entry<String, JsonNode>> attributeValues = category.get(ATTRIBUTES).fields();
        while (attributeValues.hasNext()) {
            Entry<String, JsonNode> attribute = attributeValues.next();
            attributes.put(attribute.getKey(), attribute.getValue());
        }
        if (columnIndex > 0 && arrayCounter == 0) {
            attributes.put("flgColumnBreak", true);
        }
        categoryNode.setAttributes(attributes);
    }

    private void setCategories(int level, JsonNode category, CategoryNode categoryNode, String pathSoFar) {
        JsonNode columns = category.get(COLUMNS);
        boolean columnsNodeIsNull = columns == null || columns.isNull();
        if (!columnsNodeIsNull) {
            List<CategoryNode> navCategories = new ArrayList<>();
            int columnCounter = 0;
            for (JsonNode column : columns) {
                JsonNode categoryList = column.get(LEVEL + (level + 1) + CATEGORIES);
                navCategories.addAll(traverseCategories(categoryList, level + 1, columnCounter, pathSoFar));
                columnCounter++;
            }
            categoryNode.setCategories(navCategories);
        } else {
            categoryNode.setCategories(new ArrayList<>());
        }
    }
}
