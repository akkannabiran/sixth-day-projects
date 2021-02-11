package com.sixthday.navigation.batch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.batch.vo.*;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.domain.Silos;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MobileSiloNavTreeProcessorTest {

    private final String DESIGNER_CAT_ID = "cat00073";

    private final String CATEGORY_WITH_DRIVETO = "cat25560734";
    private final String PARENT_OF_CATEGORY_WITH_DRIVETO = "cat000019";
    private final String DRIVETO_0 = "cat30409209";
    private final String DRIVETO_1 = "cat39550741";
    ObjectMapper objectMapper = new ObjectMapper();
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private MobileSiloNavTreeProcessor mobileNavProcessor;

    @Before
    public void before() {
        when(navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId()).thenReturn(DESIGNER_CAT_ID);
    }

    @Test
    public void shouldConvertARootNodeWithNoChildrenToAnEmptyArray() throws Exception {
        String navTreeNodeData = "{" +
                "\"id\": \"cat000000\"," +
                "\"displayName\": \"\"," +
                "\"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\"," +
                "\"tags\": [\"HC\"]," +
                "\"children\": []" +
                "}";

        SiloNavTreeProcessorResponse navigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = "{\"silos\":[]}";
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    public void shouldConvertARootNodeWithNullChildrenToAnEmptyArray() throws Exception {
        String navTreeNodeData = "{" +
                "\"id\": \"cat000000\"," +
                "\"displayName\": \"\"," +
                "\"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\"," +
                "\"tags\": [\"HC\"]," +
                "\"children\": null" +
                "}";

        SiloNavTreeProcessorResponse navigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = "{\"silos\":[]}";
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    public void shouldConvertTheChildrenNodes() throws Exception {
        String navTreeNodeData = createTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        expectNavigationTreeData(navigationTreeData.getSilo());
        assertThat(navigationTreeData.getSilo(), containsString("\"attributes\":{\"redTextFlag\":true,\"flgBoutiqueTextAdornmentsOverride\":true,\"textAdornment\":\"adornment-text\",\"tags\":[\"ADCAT\",\"HC\"]},"));
        assertEmptyLetterPresent(navigationTreeData.getSilo(), "A");
        assertEmptyLetterPresent(navigationTreeData.getSilo(), "G");
        assertEmptyLetterPresent(navigationTreeData.getSilo(), "Z");
    }

    @Test
    public void shouldAttributeElementIsEmptyInTheResponseForEachNode_whenAttributeValuesAreEmptyOrNull() throws Exception {
        String inputNavTreeData = "{" +
                "\"id\": \"cat000000\"," +
                "\"displayName\": \"\"," +
                "\"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\"," +
                "\"tags\": [" +
                "]," +
                "\"children\": [" +
                "{" +
                "\"id\": \"" + DESIGNER_CAT_ID + "\"," +
                "\"displayName\": \"Designers\"," +
                "\"url\": \"/Designers/" + DESIGNER_CAT_ID + "_cat000000/c.cat\"," +
                "\"tags\": [" +
                "]," +
                "\"children\": [" +
                "{" +
                "\"id\": \"cat71090787\"," +
                "\"displayName\": \"111 Skin\"," +
                "\"url\": \"/c/designers-111-skin-cat71090787\"," +
                "\"tags\": [ ]," +
                "\"children\": [ ]" +
                "}]," +
                "\"flgBoutiqueTextAdornmentsOverride\": \"\"," +
                "\"redTextFlag\": \"\"," +
                "\"textAdornment\": \"\"" +
                "}" +
                "]" +
                "}";
        SiloNavTreeProcessorResponse actualNavigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", inputNavTreeData, null), objectMapper);

        expectNavigationTreeData(actualNavigationTreeData.getSilo());
        assertEmptyLetterPresent(actualNavigationTreeData.getSilo(), "A");
        assertEmptyLetterPresent(actualNavigationTreeData.getSilo(), "G");
        assertEmptyLetterPresent(actualNavigationTreeData.getSilo(), "Z");
    }

    @Test
    public void shouldIncludeEmptyCategoriesNodeForDesignerSiloWhenChildrenNodeForDesignerSiloIsEmpty() throws Exception {
        String inputNavTreeData = "{" +
                "\"id\": \"cat000000\"," +
                "\"displayName\": \"\"," +
                "\"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\"," +
                "\"tags\": [" +
                "]," +
                "\"children\": [" +
                "{" +
                "\"id\": \"" + DESIGNER_CAT_ID + "\"," +
                "\"displayName\": \"Designers\"," +
                "\"url\": \"/Designers/" + DESIGNER_CAT_ID + "_cat000000/c.cat\"," +
                "\"tags\": [" +
                "]," +
                "\"children\": []," +
                "\"flgBoutiqueTextAdornmentsOverride\": \"\"," +
                "\"redTextFlag\": \"\"," +
                "\"textAdornment\": \"\"" +
                "}" +
                "]" +
                "}";
        SiloNavTreeProcessorResponse actualNavigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", inputNavTreeData, null), objectMapper);

        expectNavigationTreeData(actualNavigationTreeData.getSilo());
        assertThat(actualNavigationTreeData.getSilo(), containsString("\"categories\":[]"));

    }


    @Test
    public void shouldNotIncludeTheDesignerAToZCategoryInTheNavPathForACategoryUnderDesignersAToZ() throws IOException {
        String treeNodeDataWithDesigners = "{\n" +
                "  \"id\": \"cat000000\",\n" +
                "  \"displayName\": \"\",\n" +
                "  \"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\",\n" +
                "  \"tags\": [\n" +
                "    \"HC\"\n" +
                "  ],\n" +
                "  \"children\": [\n" +
                "    {\n" +
                "      \"id\": \"" + DESIGNER_CAT_ID + "\",\n" +
                "      \"displayName\": \"Designers\",\n" +
                "      \"url\": \"/Designers/" + DESIGNER_CAT_ID + "_cat000000/c.cat\",\n" +
                "      \"tags\": [\n" +
                "        \"ADCAT\",\n" +
                "        \"HC\"\n" +
                "      ],\n" +
                "      \"children\": [\n" +
                "        {\n" +
                "          \"id\": \"cat57510746\",\n" +
                "          \"displayName\": \"Camilla and Marc\",\n" +
                "          \"url\": \"/Camilla-and-Marc/cat57510746_" + DESIGNER_CAT_ID + "_cat000000/c.cat\",\n" +
                "          \"tags\": [],\n" +
                "          \"children\": []\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        SiloNavTreeProcessorResponse actualNavigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", treeNodeDataWithDesigners, null), objectMapper);

        Silos silos = objectMapper.readValue(actualNavigationTreeData.getSilo(), Silos.class);
        List<CategoryNode> aToZDesignersCategories = silos.getSilosTree().get(0).getCategories();
        List<CategoryNode> cSectionDesignerCategories = aToZDesignersCategories.get(2).getCategories();
        String camillaAndMarcCategoryUrl = cSectionDesignerCategories.get(0).getUrl();
        String navPathQuery = camillaAndMarcCategoryUrl.substring(camillaAndMarcCategoryUrl.indexOf("navpath="), camillaAndMarcCategoryUrl.length());

        assertThat("navpath=cat000000_" + DESIGNER_CAT_ID + "_cat57510746", is(navPathQuery));
    }

    @Test
    public void shouldNotAppendARepeatedCategoryIdToNavPath() throws IOException {
        String treeNodeData = "{\n" +
                "  \"id\": \"cat000000\",\n" +
                "  \"displayName\": \"\",\n" +
                "  \"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\",\n" +
                "  \"children\": [\n" +
                "    {\n" +
                "      \"id\": \"cat000000\",\n" +
                "      \"displayName\": \"\",\n" +
                "  \"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\",\n" +
                "      \"children\": [\n" +
                "{\n" +
                "    \"id\": \"cat000000\",\n" +
                "      \"displayName\": \"\",\n" +
                "  \"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\",\n" +
                "      \"children\": [\n" +
                " ]" +
                "}" +
                "]" +
                "    }\n" +
                "  ]\n" +
                "}";

        SiloNavTreeProcessorResponse actualNavigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", treeNodeData, null), objectMapper);

        Silos silos = objectMapper.readValue(actualNavigationTreeData.getSilo(), Silos.class);
        String parentUrl = silos.getSilosTree().get(0).getUrl();
        String navPathParentQuery = parentUrl.substring(parentUrl.indexOf("navpath="), parentUrl.length());
        String childUrl = silos.getSilosTree().get(0).getCategories().get(0).getUrl();
        String navPathChildQuery = childUrl.substring(childUrl.indexOf("navpath="), childUrl.length());

        assertThat(navPathParentQuery, is("navpath=cat000000"));
        assertThat(navPathChildQuery, is("navpath=cat000000"));
    }

    @Test
    @SneakyThrows
    public void shouldIncludeChildDriveToCategoryIdsInNavpath() {
        List<ContextualProperty> contextualPropertyList = new ArrayList<>();
        contextualPropertyList.add(ContextualProperty.builder().parentId(PARENT_OF_CATEGORY_WITH_DRIVETO).driveToSubcategoryId(DRIVETO_1).build());
        CategoryDocument categoryDocument = CategoryDocument.builder().id(CATEGORY_WITH_DRIVETO).contextualProperties(contextualPropertyList).build();
        when(categoryRepository.getCategoryDocument(CATEGORY_WITH_DRIVETO)).thenReturn(categoryDocument);

        String navTreeNodeData = createActualCategoryWithDriveToTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE.CHILD);
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldIncludeGrandchildDriveToCategoryIdsInNavpath() {
        List<ContextualProperty> contextualPropertyList = new ArrayList<>();
        contextualPropertyList.add(ContextualProperty.builder().parentId(PARENT_OF_CATEGORY_WITH_DRIVETO).driveToSubcategoryId(DRIVETO_0 + ":" + DRIVETO_1).build());
        CategoryDocument categoryDocument = CategoryDocument.builder().id(CATEGORY_WITH_DRIVETO).contextualProperties(contextualPropertyList).build();
        when(categoryRepository.getCategoryDocument(CATEGORY_WITH_DRIVETO)).thenReturn(categoryDocument);

        String navTreeNodeData = createActualCategoryWithDriveToTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE.GRANDCHILD);
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldShouldNotIncludeMissingDriveToCategoryIdsInNavpath() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id(CATEGORY_WITH_DRIVETO).build();
        when(categoryRepository.getCategoryDocument(CATEGORY_WITH_DRIVETO)).thenReturn(categoryDocument);

        String navTreeNodeData = createActualCategoryWithDriveToTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = mobileNavProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE.MISSING);
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    private String createActualCategoryWithDriveToTreeNodeData() {
        return "{" +
                "\"id\":\"cat000000\"," +
                "\"displayName\": \"\"," +
                "\"url\":\"/c/neimans-store-catalog-cat000000\"," +
                "\"tags\": [\"HC\"]," +
                "\"children\": [" +
                "{\"id\": \"cat000001\"," +
                "\"displayName\": \"Women's Clothing\"," +
                "\"url\": \"/c/womens-clothing-cat000001\"," +
                "\"tags\": [\"HC\"]," +
                "\"children\": [" +
                "{\"id\": \"cat000009\"," +
                "\"displayName\": \"All Designers\"," +
                "\"url\": \"/c/womens-clothing-all-designers-cat000009\"," +
                "\"tags\": [\"HC\"]," +
                "\"children\": [{" +
                "\"id\": \"" + PARENT_OF_CATEGORY_WITH_DRIVETO + "\"," +
                "\"displayName\": \"Women's Apparel\"," +
                "\"url\": \"/c/womens-clothing-all-designers-womens-apparel-" + PARENT_OF_CATEGORY_WITH_DRIVETO + "\"," +
                "\"tags\": [\"HC\"]," +
                "\"children\": [" +
                "{\"id\": \"" + CATEGORY_WITH_DRIVETO + "\"," +
                "\"displayName\": \"Brunello Cucinelli\"," +
                "\"url\": \"/c/designers-brunello-cucinelli-womens-apparel-" + DRIVETO_1 + "\"," +
                "\"tags\": [\"HC\"]," +
                "\"children\": [{" +
                "\"id\": \"" + CATEGORY_WITH_DRIVETO + "\"," +
                "\"displayName\": \"All Brunello Cucinelli\"," +
                "\"url\": \"/c/designers-brunello-cucinelli-womens-apparel-" + DRIVETO_1 + "\"," +
                "\"children\": []}]}]}]}]}]}";
    }

    private String createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE type) throws JsonProcessingException {
        Silos silos = new Silos();

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("tags", new String[]{"HC"});

        CategoryNode level0Node = CategoryNode.builder().catmanId("cat000001").id("1").level(0).name("Women's Clothing").
                url("/c/womens-clothing-cat000001?navpath=cat000000_cat000001").
                attributes(attributes).build();

        CategoryNode level1Node = CategoryNode.builder().catmanId("cat000009").id("2").level(1).name("All Designers").
                url("/c/womens-clothing-all-designers-cat000009?navpath=cat000000_cat000001_cat000009").
                attributes(attributes).build();

        CategoryNode level2Node = CategoryNode.builder().catmanId(PARENT_OF_CATEGORY_WITH_DRIVETO).id("3").level(2).name("Women's Apparel").
                url("/c/womens-clothing-all-designers-womens-apparel-" + PARENT_OF_CATEGORY_WITH_DRIVETO + "?navpath=cat000000_cat000001_cat000009_" +
                        PARENT_OF_CATEGORY_WITH_DRIVETO).
                attributes(attributes).build();

        CategoryNode level3Node = null, level4Node = null;

        switch (type) {
            case MISSING:
                level3Node = CategoryNode.builder().catmanId(CATEGORY_WITH_DRIVETO).id("4").level(3).name("Brunello Cucinelli").
                        url("/c/designers-brunello-cucinelli-womens-apparel-cat39550741?navpath=cat000000_cat000001_cat000009_" +
                                PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO).
                        attributes(attributes).build();

                level4Node = CategoryNode.builder().catmanId("cat25560734").id("5").level(4).name("All Brunello Cucinelli").
                        url("/c/designers-brunello-cucinelli-womens-apparel-cat39550741?navpath=cat000000_cat000001_cat000009_" +
                                PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO).
                        attributes(new HashMap<>()).categories(new ArrayList<>()).build();
                break;
            case CHILD:
                level3Node = CategoryNode.builder().catmanId(CATEGORY_WITH_DRIVETO).id("4").level(3).name("Brunello Cucinelli").
                        url("/c/designers-brunello-cucinelli-womens-apparel-cat39550741?navpath=cat000000_cat000001_cat000009_" +
                                PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO + "_" + DRIVETO_1).
                        attributes(attributes).build();

                level4Node = CategoryNode.builder().catmanId("cat25560734").id("5").level(4).name("All Brunello Cucinelli").
                        url("/c/designers-brunello-cucinelli-womens-apparel-cat39550741?navpath=cat000000_cat000001_cat000009_" +
                                PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO + "_" + DRIVETO_1).
                        attributes(new HashMap<>()).categories(new ArrayList<>()).build();
                break;
            case GRANDCHILD:
                level3Node = CategoryNode.builder().catmanId(CATEGORY_WITH_DRIVETO).id("4").level(3).name("Brunello Cucinelli").
                        url("/c/designers-brunello-cucinelli-womens-apparel-cat39550741?navpath=cat000000_cat000001_cat000009_" +
                                PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO + "_" + DRIVETO_0 + "_" + DRIVETO_1).
                        attributes(attributes).build();

                level4Node = CategoryNode.builder().catmanId("cat25560734").id("5").level(4).name("All Brunello Cucinelli").
                        url("/c/designers-brunello-cucinelli-womens-apparel-cat39550741?navpath=cat000000_cat000001_cat000009_" +
                                PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO + "_" + DRIVETO_0 + "_" + DRIVETO_1).
                        attributes(new HashMap<>()).categories(new ArrayList<>()).build();
                break;
        }

        level3Node.setCategories(Arrays.asList(level4Node));
        level2Node.setCategories(Arrays.asList(level3Node));
        level1Node.setCategories(Arrays.asList(level2Node));
        level0Node.setCategories(Arrays.asList(level1Node));
        silos.setSilosTree(Arrays.asList(level0Node));

        return objectMapper.writeValueAsString(silos);
    }

    private String createTreeNodeData() {
        return "{" +
                "\"id\": \"cat000000\"," +
                "\"displayName\": \"\"," +
                "\"url\": \"/Neimans-Store-Catalog/cat000000/c.cat\"," +
                "\"tags\": [" +
                "\"HC\"" +
                "]," +
                "\"children\": [" +
                "{" +
                "\"id\": \"" + DESIGNER_CAT_ID + "\"," +
                "\"displayName\": \"Designers\"," +
                "\"url\": \"/Designers/" + DESIGNER_CAT_ID + "_cat000000/c.cat\"," +
                "\"tags\": [" +
                "\"ADCAT\"," +
                "\"HC\"" +
                "]," +
                "\"children\": [" +
                "{" +
                "\"id\": \"cat71090787\"," +
                "\"displayName\": \"111 Skin\"," +
                "\"url\": \"/c/designers-111-skin-cat71090787\"," +
                "\"tags\": [ ]," +
                "\"children\": [ ]" +
                "}]," +
                "\"flgBoutiqueTextAdornmentsOverride\": \"true\"," +
                "\"redTextFlag\": \"true\"," +
                "\"textAdornment\": \"adornment-text\"" +
                "}," +
                "{" +
                "\"id\": \"cat000731\"," +
                "\"displayName\": \"Designers\"," +
                "\"tags\": [" +
                "\"ADCAT\"," +
                "\"HC\"" +
                "]," +
                "\"children\": []," +
                "\"flgBoutiqueTextAdornmentsOverride\": \"true\"," +
                "\"redTextFlag\": \"true\"," +
                "\"textAdornment\": \"adornment-text\"" +
                "}" +
                "]" +
                "}";
    }

    private void expectNavigationTreeData(String actual) {
        assertThat(actual, containsString("\"catmanId\":\"" + DESIGNER_CAT_ID + "\""));
        assertThat(actual, containsString("\"level\":0,"));
        assertThat(actual, containsString("\"name\":\"Designers\","));
        assertThat(actual, containsString("/Designers/" + DESIGNER_CAT_ID + "_cat000000/c.cat?navpath=cat000000_" + DESIGNER_CAT_ID));

    }

    private void assertEmptyLetterPresent(String actual, String letter) {
        assertThat(actual, containsString("\"catmanId\":\"" + letter + "\""));
        assertThat(actual, containsString("\"level\":1"));
        assertThat(actual, containsString("\"name\":\"" + letter + "\""));
        assertThat(actual, containsString("/Designers/" + DESIGNER_CAT_ID + "/c.cat?dIndex=" + letter + "&drawer=Designer&navpath=cat000000_" + DESIGNER_CAT_ID));
        assertThat(actual, containsString("\"attributes\":{\"designerAlphaCategory\":true}"));
    }

    private enum DRIVETO_TYPE {
        MISSING, CHILD, GRANDCHILD
    }
}

