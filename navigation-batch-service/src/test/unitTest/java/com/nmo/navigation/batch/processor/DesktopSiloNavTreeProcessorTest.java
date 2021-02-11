package com.sixthday.navigation.batch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.batch.vo.*;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.content.repository.ContentServiceRepository;
import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.domain.Silos;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.exceptions.NavigationBatchServiceException;
import lombok.SneakyThrows;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static com.sixthday.testing.ExceptionThrower.assertThrown;
import static com.sixthday.testing.LogCapture.captureLogOutput;
import static com.sixthday.testing.LogCapture.stopLogCapture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DesktopSiloNavTreeProcessorTest {
    private final String ROOT_CAT_ID = "cat000000";
    private final String DESIGNER_CAT_ID = "cat00073";

    private final String CATEGORY_WITH_DRIVETO = "cat25560734";
    private final String PARENT_OF_CATEGORY_WITH_DRIVETO = "cat44670744";
    private final String DRIVETO_0 = "cat30409209";
    private final String DRIVETO_1 = "cat39550741";
    private ByteArrayOutputStream loggingOutput;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationBatchServiceConfig navigationBatchServiceConfig;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ContentServiceRepository contentServiceRepository;

    @InjectMocks
    private DesktopSiloNavTreeProcessor desktopSiloNavTreeProcessor;

    @Before
    public void before() {
        when(navigationBatchServiceConfig.getIntegration().getCategoryType().get("live")).thenReturn(ROOT_CAT_ID);
        when(navigationBatchServiceConfig.getCategoryIdConfig().getDesignerCategoryId()).thenReturn(DESIGNER_CAT_ID);
        when(contentServiceRepository.getSiloDrawerAsset(anyList())).thenReturn(getMockContentResponse());
        loggingOutput = captureLogOutput(DesktopSiloNavTreeProcessor.class);
    }

    @After
    public void after() {
        stopLogCapture(DesktopSiloNavTreeProcessor.class, loggingOutput);
    }

    @Test
    @SneakyThrows
    public void shouldConvertTheChildrenNodes() {
        String navTreeNodeData = createActualTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedNavigationTreeData();
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldExcludeDesignerSiloIdFromNavPathForDesignerSiloItems() {
        String navTreeNodeData = createActualDesignerTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedDesignerNavigationTreeData();
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldIncludeAttributesInTheResponseForEachNode() {
        String navTreeNodeData = createActualTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedNavigationTreeData();
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldIncludeColumnBreakAttributeInTheResponseForFirstElementInSecondColumn() {
        String navTreeNodeData = createActualTreeNodeDataWithMultipleColumns();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedTreeNodeDataWithColumnBreakFlag();
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldInitializeEmptyListWhenThereAreNoChildCategories() {
        String navTreeNodeData = createActualTreeNodeDataOnlyWithSilos();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedTreeNodeWithEmptyList();
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    public void shouldFailWhenResponseIsNotSuccessful() {
        String navTreeNodeData = createActualTreeNodeDataWithNoSuccessMessage();

        Throwable ex = assertThrown(() -> desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper));

        assertThat("Failure exception class", ex, instanceOf(NavigationBatchServiceException.class));
        assertThat("Failure exception message", ex.getMessage(), is(DesktopSiloNavTreeProcessor.ATG_FAILURE_EXCEPTION_MESSAGE));
    }

    @Test
    @SneakyThrows
    public void shouldIncludeChildDriveToCategoryIdsInNavPath() {
        List<ContextualProperty> contextualPropertyList = new ArrayList<>();
        contextualPropertyList.add(ContextualProperty.builder().parentId(PARENT_OF_CATEGORY_WITH_DRIVETO).driveToSubcategoryId(DRIVETO_1).build());
        CategoryDocument categoryDocument = CategoryDocument.builder().id(CATEGORY_WITH_DRIVETO).contextualProperties(contextualPropertyList).build();
        when(categoryRepository.getCategoryDocument(CATEGORY_WITH_DRIVETO)).thenReturn(categoryDocument);

        String navTreeNodeData = createActualCategoryWithDriveToTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE.CHILD);
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldIncludeGrandchildDriveToCategoryIdsInNavPath() {
        List<ContextualProperty> contextualPropertyList = new ArrayList<>();
        contextualPropertyList.add(ContextualProperty.builder().parentId(PARENT_OF_CATEGORY_WITH_DRIVETO).driveToSubcategoryId(DRIVETO_0 + ":" + DRIVETO_1).build());
        CategoryDocument categoryDocument = CategoryDocument.builder().id(CATEGORY_WITH_DRIVETO).contextualProperties(contextualPropertyList).build();
        when(categoryRepository.getCategoryDocument(CATEGORY_WITH_DRIVETO)).thenReturn(categoryDocument);

        String navTreeNodeData = createActualCategoryWithDriveToTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE.GRANDCHILD);
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldShouldNotIncludeMissingDriveToCategoryIdsInNavpath() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id(CATEGORY_WITH_DRIVETO).build();
        when(categoryRepository.getCategoryDocument(CATEGORY_WITH_DRIVETO)).thenReturn(categoryDocument);

        String navTreeNodeData = createActualCategoryWithDriveToTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE.MISSING);
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldNotIncludeAEMContentInResponseIfAEMCallThrowsException() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id(CATEGORY_WITH_DRIVETO).build();
        when(categoryRepository.getCategoryDocument(CATEGORY_WITH_DRIVETO)).thenReturn(categoryDocument);
        when(contentServiceRepository.getSiloDrawerAsset(anyList())).thenThrow(new RuntimeException());

        String navTreeNodeData = createActualCategoryWithDriveToTreeNodeData();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE.MISSING);
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    @Test
    @SneakyThrows
    public void shouldIncludeAEMContentInResponseIfCategoryIdMatchesWithAEMResponseCategoryId() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id(CATEGORY_WITH_DRIVETO).build();
        when(categoryRepository.getCategoryDocument(CATEGORY_WITH_DRIVETO)).thenReturn(categoryDocument);
        when(contentServiceRepository.getSiloDrawerAsset(anyList())).thenReturn(getMockContentResponse());

        String navTreeNodeData = createActualCategoryWithSiloDrawerAsserWithAEMData();
        SiloNavTreeProcessorResponse navigationTreeData = desktopSiloNavTreeProcessor.process(new SiloNavTreeReaderResponse("US", navTreeNodeData, null), objectMapper);

        String expectedNavigationTreeData = createExpectedCategoryWitSiloDrawerAssetWithAEMData();
        assertThat(navigationTreeData.getSilo(), is(expectedNavigationTreeData));
    }

    private String createActualCategoryWithSiloDrawerAsserWithAEMData() {
        return "{\"status\": \"SUCCESS\"," +
                "\"silos\":[" +
                "{\"id\":\"cat00000\"," +
                "\"siloDisplayName\": \"Women's Clothing\"," +
                "\"url\":\"/c/womens-clothing-cat000001\"," +
                "\"attributes\":{\"aem\":\"some-aem-content\"}," +
                "\"columns\":[" +
                "{\"level1Categories\":[" +
                "{\"id\": \"" + PARENT_OF_CATEGORY_WITH_DRIVETO + "\"," +
                "\"level1CategoryDisplayName\":\"Featured Designers\"," +
                "\"url\": \"/c/womens-clothing-featured-designers-" + PARENT_OF_CATEGORY_WITH_DRIVETO + "\"," +
                "\"attributes\":{}," +
                "\"columns\":[" +
                "{\"level2Categories\":[" +
                "{\"id\":\"" + CATEGORY_WITH_DRIVETO + "\"," +
                "\"level2CategoryDisplayName\":\"BC By Brunello\"," +
                "\"url\": \"/c/designers-brunello-cucinelli-womens-apparel-" + DRIVETO_1 + "\"," +
                "\"attributes\":{}}]}]}]}]}]}";
    }

    private String createExpectedCategoryWitSiloDrawerAssetWithAEMData() throws JsonProcessingException {
        Silos silos = new Silos();

        CategoryNode level0Node = CategoryNode.builder().catmanId("cat00000").id("cat00000").level(0).name("Women's Clothing").
                url("/c/womens-clothing-cat000001?navpath=cat000000").
                attributes(new HashMap<>()).build();

        CategoryNode level1Node = CategoryNode.builder().catmanId(PARENT_OF_CATEGORY_WITH_DRIVETO).id(PARENT_OF_CATEGORY_WITH_DRIVETO).level(1).name("Featured Designers").
                url("/c/womens-clothing-featured-designers-" + PARENT_OF_CATEGORY_WITH_DRIVETO + "?navpath=cat000000_"
                        + PARENT_OF_CATEGORY_WITH_DRIVETO).
                attributes(new HashMap<>()).build();

        CategoryNode level2Node = CategoryNode.builder().catmanId(CATEGORY_WITH_DRIVETO).id(CATEGORY_WITH_DRIVETO).level(2).name("BC By Brunello").
                url("/c/designers-brunello-cucinelli-womens-apparel-" + DRIVETO_1 + "?navpath=cat000000_"
                        + PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO).
                attributes(new HashMap<>()).categories(new ArrayList<>()).build();

        level1Node.setCategories(Arrays.asList(level2Node));
        level0Node.setCategories(Arrays.asList(level1Node));
        Map<String, Object> attributesMap = new HashMap();
        attributesMap.put("aem", "some-aem-content");
        level0Node.setAttributes(attributesMap);
        silos.setSilosTree(Arrays.asList(level0Node));

        return objectMapper.writeValueAsString(silos);
    }

    private String createActualTreeNodeDataOnlyWithSilos() {
        return "{\"status\":\"SUCCESS\","
                + "\"silos\":[{"
                + "\"catmanId\":\"cat000001\","
                + "\"id\":\"cat000001\","
                + "\"siloDisplayName\":\"Women's Apparel\","
                + "\"url\":\"/Womens-Clothing/cat000001_cat000000/c.cat\","
                + "\"attributes\":{}}]}";
    }

    private String createActualDesignerTreeNodeData() {
        return "{\"status\":\"SUCCESS\","
                + "\"silos\":[{"
                + "\"catmanId\":\"" + DESIGNER_CAT_ID + "\","
                + "\"id\":\"" + DESIGNER_CAT_ID + "\","
                + "\"siloDisplayName\":\"Designers\","
                + "\"url\":\"/Designers/" + DESIGNER_CAT_ID + "_cat000000/c.cat\","
                + "\"attributes\":{\"flgBoutiqueTextAdornmentsOverride\":true},"
                + "\"columns\":[{\"level1Categories\":[{\"id\":\"cat000009\","
                + "\"level1CategoryDisplayName\":\"All Designers\",\"url\":\"/Designers/All-Designers/cat000009_" + DESIGNER_CAT_ID + "_cat000000/c.cat\","
                + "\"attributes\":{},\"columns\":[{\"level2Categories\":[]}]}]}]}]}";
    }

    private String createActualCategoryWithDriveToTreeNodeData() {
        return "{\"status\": \"SUCCESS\"," +
                "\"silos\":[" +
                "{\"id\":\"cat000001\"," +
                "\"siloDisplayName\": \"Women's Clothing\"," +
                "\"url\":\"/c/womens-clothing-cat000001\"," +
                "\"attributes\":{}," +
                "\"columns\":[" +
                "{\"level1Categories\":[" +
                "{\"id\": \"" + PARENT_OF_CATEGORY_WITH_DRIVETO + "\"," +
                "\"level1CategoryDisplayName\":\"Featured Designers\"," +
                "\"url\": \"/c/womens-clothing-featured-designers-" + PARENT_OF_CATEGORY_WITH_DRIVETO + "\"," +
                "\"attributes\":{}," +
                "\"columns\":[" +
                "{\"level2Categories\":[" +
                "{\"id\":\"" + CATEGORY_WITH_DRIVETO + "\"," +
                "\"level2CategoryDisplayName\":\"BC By Brunello\"," +
                "\"url\": \"/c/designers-brunello-cucinelli-womens-apparel-" + DRIVETO_1 + "\"," +
                "\"attributes\":{}}]}]}]}]}]}";
    }

    private String createActualTreeNodeData() {
        return "{\"status\":\"SUCCESS\","
                + "\"silos\":[{"
                + "\"catmanId\":\"cat000001\","
                + "\"id\":\"cat000001\","
                + "\"siloDisplayName\":\"Women's Apparel\","
                + "\"url\":\"/Womens-Clothing/cat000001_cat000000/c.cat\","
                + "\"attributes\":{\"flgBoutiqueTextAdornmentsOverride\":true},"
                + "\"columns\":[{\"level1Categories\":[{\"id\":\"cat000009\","
                + "\"level1CategoryDisplayName\":\"All Designers\",\"url\":\"/Womens-Clothing/All-Designers/cat000009_cat000001_cat000000/c.cat\","
                + "\"attributes\":{},\"columns\":[{\"level2Categories\":[]}]}]}]}]}";
    }

    private String createActualTreeNodeDataWithMultipleColumns() {
        return "{\"status\":\"SUCCESS\","
                + "\"silos\":[{"
                + "\"catmanId\":\"cat000001\","
                + "\"id\":\"cat000001\","
                + "\"siloDisplayName\":\"Women's Apparel\","
                + "\"url\":\"/Womens-Clothing/cat000001_cat000000/c.cat\","
                + "\"attributes\":{\"flgBoutiqueTextAdornmentsOverride\":true},"
                + "\"columns\":[{\"level1Categories\":[{\"id\":\"cat000009\","
                + "\"level1CategoryDisplayName\":\"All Designers\",\"url\":\"/Womens-Clothing/All-Designers/cat000009_cat000001_cat000000/c.cat?abc=xyz\","
                + "\"attributes\":{},\"columns\":[{\"level2Categories\":[]}"
                + "]}]},"
                + "{\"level1Categories\":"
                + "[{"
                + "\"catmanId\":\"cat000009\","
                + "\"id\":\"cat000009\","
                + "\"level1CategoryDisplayName\":\"All Designers\","
                + "\"url\":\"/Womens-Clothing/All-Designers/cat000009_cat000001_cat000000/c.cat\","
                + "\"attributes\":{},\"columns\":[{\"level2Categories\":[]}]}]}"
                + "]}]}";
    }

    private String createActualTreeNodeDataWithNoSuccessMessage() {
        return "{\"status\":\"Incorrect Status\","
                + "\"silos\":[{"
                + "\"id\":\"cat000001\","
                + "\"siloDisplayName\":\"Women's Apparel\","
                + "\"url\":\"/Womens-Clothing/cat000001_cat000000/c.cat\","
                + "\"attributes\":{\"flgBoutiqueTextAdornmentsOverride\":true},"
                + "\"columns\":[{\"level1Categories\":[{\"id\":\"cat000009\","
                + "\"level1CategoryDisplayName\":\"All Designers\",\"url\":\"/Womens-Clothing/All-Designers/cat000009_cat000001_cat000000/c.cat\","
                + "\"attributes\":{},\"columns\":[{\"level2Categories\":[]}]}]}]}]}";
    }

    private String createExpectedCategoryWithDriveToNavigationTreeData(DRIVETO_TYPE type) throws JsonProcessingException {
        Silos silos = new Silos();

        CategoryNode level0Node = CategoryNode.builder().catmanId("cat000001").id("cat000001").level(0).name("Women's Clothing").
                url("/c/womens-clothing-cat000001?navpath=cat000000_cat000001").
                attributes(new HashMap<>()).build();

        CategoryNode level1Node = CategoryNode.builder().catmanId(PARENT_OF_CATEGORY_WITH_DRIVETO).id(PARENT_OF_CATEGORY_WITH_DRIVETO).level(1).name("Featured Designers").
                url("/c/womens-clothing-featured-designers-" + PARENT_OF_CATEGORY_WITH_DRIVETO + "?navpath=cat000000_cat000001_"
                        + PARENT_OF_CATEGORY_WITH_DRIVETO).
                attributes(new HashMap<>()).build();

        CategoryNode level2Node = null;

        switch (type) {
            case MISSING:
                level2Node = CategoryNode.builder().catmanId(CATEGORY_WITH_DRIVETO).id(CATEGORY_WITH_DRIVETO).level(2).name("BC By Brunello").
                        url("/c/designers-brunello-cucinelli-womens-apparel-" + DRIVETO_1 + "?navpath=cat000000_cat000001_"
                                + PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO).
                        attributes(new HashMap<>()).categories(new ArrayList<>()).build();
                break;
            case CHILD:
                level2Node = CategoryNode.builder().catmanId(CATEGORY_WITH_DRIVETO).id(CATEGORY_WITH_DRIVETO).level(2).name("BC By Brunello").
                        url("/c/designers-brunello-cucinelli-womens-apparel-" + DRIVETO_1 + "?navpath=cat000000_cat000001_"
                                + PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO + "_" + DRIVETO_1).
                        attributes(new HashMap<>()).categories(new ArrayList<>()).build();
                break;
            case GRANDCHILD:
                level2Node = CategoryNode.builder().catmanId(CATEGORY_WITH_DRIVETO).id(CATEGORY_WITH_DRIVETO).level(2).name("BC By Brunello").
                        url("/c/designers-brunello-cucinelli-womens-apparel-" + DRIVETO_1 + "?navpath=cat000000_cat000001_"
                                + PARENT_OF_CATEGORY_WITH_DRIVETO + "_" + CATEGORY_WITH_DRIVETO + "_" + DRIVETO_0 + "_" + DRIVETO_1).
                        attributes(new HashMap<>()).categories(new ArrayList<>()).build();
                break;
        }

        level1Node.setCategories(Arrays.asList(level2Node));
        level0Node.setCategories(Arrays.asList(level1Node));
        silos.setSilosTree(Arrays.asList(level0Node));

        return objectMapper.writeValueAsString(silos);
    }

    private String createExpectedNavigationTreeData() throws JsonProcessingException {
        Silos silos = new Silos();

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("flgBoutiqueTextAdornmentsOverride", true);

        CategoryNode level0Node = CategoryNode.builder().catmanId("cat000001").id("cat000001").level(0).name("Women's Apparel").
                url("/Womens-Clothing/cat000001_cat000000/c.cat?navpath=cat000000_cat000001").
                attributes(attributes).build();

        CategoryNode level1Node = CategoryNode.builder().catmanId("cat000009").id("cat000009").level(1).name("All Designers").
                url("/Womens-Clothing/All-Designers/cat000009_cat000001_cat000000/c.cat?navpath=cat000000_cat000001_cat000009").
                attributes(new HashMap<>()).categories(new ArrayList<>()).build();

        level0Node.setCategories(Arrays.asList(level1Node));
        silos.setSilosTree(Arrays.asList(level0Node));

        return objectMapper.writeValueAsString(silos);
    }

    private String createExpectedDesignerNavigationTreeData() throws JsonProcessingException {
        Silos silos = new Silos();

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("flgBoutiqueTextAdornmentsOverride", true);

        CategoryNode level0Node = CategoryNode.builder().catmanId(DESIGNER_CAT_ID).id(DESIGNER_CAT_ID).level(0).name("Designers").
                url("/Designers/" + DESIGNER_CAT_ID + "_cat000000/c.cat?navpath=cat000000_" + DESIGNER_CAT_ID).
                attributes(attributes).build();

        CategoryNode level1Node = CategoryNode.builder().catmanId("cat000009").id("cat000009").level(1).name("All Designers").
                url("/Designers/All-Designers/cat000009_" + DESIGNER_CAT_ID + "_cat000000/c.cat?navpath=cat000000_cat000009").
                attributes(new HashMap<>()).categories(new ArrayList<>()).build();

        level0Node.setCategories(Arrays.asList(level1Node));
        silos.setSilosTree(Arrays.asList(level0Node));

        return objectMapper.writeValueAsString(silos);
    }

    private String createExpectedTreeNodeDataWithColumnBreakFlag() throws JsonProcessingException {
        Silos silos = new Silos();

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("flgBoutiqueTextAdornmentsOverride", true);

        CategoryNode level0Node = CategoryNode.builder().catmanId("cat000001").id("cat000001").level(0).name("Women's Apparel").
                url("/Womens-Clothing/cat000001_cat000000/c.cat?navpath=cat000000_cat000001").
                attributes(attributes).build();

        List<CategoryNode> level1Nodes = new ArrayList<>();

        CategoryNode level1Node = CategoryNode.builder().catmanId("cat000009").id("cat000009").level(1).name("All Designers").
                url("/Womens-Clothing/All-Designers/cat000009_cat000001_cat000000/c.cat?abc=xyz&navpath=cat000000_cat000001_cat000009").
                attributes(new HashMap<>()).categories(new ArrayList<>()).build();

        level1Nodes.add(level1Node);

        attributes = new HashMap<>();
        attributes.put("flgColumnBreak", true);

        level1Node = CategoryNode.builder().catmanId("cat000009").id("cat000009").level(1).name("All Designers").
                url("/Womens-Clothing/All-Designers/cat000009_cat000001_cat000000/c.cat?navpath=cat000000_cat000001_cat000009").
                attributes(attributes).categories(new ArrayList<>()).build();

        level1Nodes.add(level1Node);

        level0Node.setCategories(level1Nodes);
        silos.setSilosTree(Arrays.asList(level0Node));
        return objectMapper.writeValueAsString(silos);
    }

    private String createExpectedTreeNodeWithEmptyList() throws JsonProcessingException {
        Silos silos = new Silos();

        CategoryNode level0Node = CategoryNode.builder().catmanId("cat000001").id("cat000001").level(0).name("Women's Apparel").
                url("/Womens-Clothing/cat000001_cat000000/c.cat?navpath=cat000000_cat000001").
                attributes(new HashMap<>()).categories(new ArrayList<>()).build();

        silos.setSilosTree(Arrays.asList(level0Node));
        return objectMapper.writeValueAsString(silos);
    }

    private Map<String, Object> getMockContentResponse() {
        return Collections.singletonMap("cat00000", new String("some-aem-content"));
    }

    private enum DRIVETO_TYPE {
        MISSING, CHILD, GRANDCHILD
    }
}
