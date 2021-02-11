package com.sixthday.navigation.api;

import com.sixthday.navigation.Application;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.integration.config.SubscriberConfiguration;
import lombok.SneakyThrows;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class CategorySyncIntegrationTest {

    private static final int PROPAGATION_WAIT_TIME = 5000;
    private String CATEGORY_ID = "cat1234";
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SubscriberConfiguration subscriberConfiguration;
    @Autowired
    private RestHighLevelClient client;

    @Before
    public void setup() {
        CachingConnectionFactory factory = new CachingConnectionFactory(subscriberConfiguration.getRabbitmqConfig().getHost());
        factory.setPort(subscriberConfiguration.getRabbitmqConfig().getPort());
        factory.setUsername(subscriberConfiguration.getVaultConfig().getRabbitmqUsername());
        factory.setPassword(subscriberConfiguration.getVaultConfig().getRabbitmqPassword());
        factory.setConnectionTimeout(subscriberConfiguration.getRabbitmqConfig().getConnectionTimeout());
        rabbitTemplate = new RabbitTemplate(factory);
        rabbitTemplate.setRoutingKey(subscriberConfiguration.getRabbitmqConfig().getQueueName());
    }

    @After
    @SneakyThrows
    public void tearDown() {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new DeleteRequest(
                subscriberConfiguration.getElasticSearchConfig().getIndexName(),
                CategoryDocument.DOCUMENT_TYPE,
                CATEGORY_ID));
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @SneakyThrows
    private void sendMessage(String message) {
        final Message msg = MessageBuilder
                .withBody(message.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        rabbitTemplate.convertAndSend(subscriberConfiguration.categoryQueue().getName(), msg);
        Thread.sleep(PROPAGATION_WAIT_TIME);
    }

    @Test
    @SneakyThrows
    public void shouldNotRemoveCategoryDocumentWhenWeGetCategoryRemovedMessageAndDeletedFlagIsFalse() {
        String message = "{\n" +
                "  \"id\": \"" + CATEGORY_ID + "\",\n" +
                "  \"eventType\": \"CATEGORY_REMOVED\",\n" +
                "  \"isDeleted\": \"false\",\n" +
                "  \"searchCriteria\": {\n" +
                "    \"include\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    },\n" +
                "    \"exclude\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    }\n" +
                "  }\n" +
                "}";

        sendMessage(message);

        CategoryDocument categoryDocument = categoryRepository.getCategoryDocument(CATEGORY_ID);
        assertThat(categoryDocument.isDeleted(), is(false));
    }

    @Test
    @SneakyThrows
    public void shouldSaveCategoryWithIsDeletedFalseIfDeletedFlagIsNotInTheMessage() {
        String message = "{\n" +
                "  \"id\": \"" + CATEGORY_ID + "\",\n" +
                "  \"eventType\": \"CATEGORY_UPDATED\",\n" +
                "  \"searchCriteria\": {\n" +
                "    \"include\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    },\n" +
                "    \"exclude\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    }\n" +
                "  }\n" +
                "}";

        sendMessage(message);

        CategoryDocument categoryDocument = categoryRepository.getCategoryDocument(CATEGORY_ID);
        assertThat(categoryDocument.isDeleted(), is(false));
    }

    @Test
    public void shouldRemoveCategoryDocumentWhenWeGetCategoryRemovedMessageAndDeletedFlagIsTrue() {
        try {
            String message = "{\n" +
                    "  \"id\": \"" + CATEGORY_ID + "\",\n" +
                    "  \"eventType\": \"CATEGORY_REMOVED\",\n" +
                    "  \"isDeleted\": \"true\",\n" +
                    "  \"searchCriteria\": {\n" +
                    "    \"include\": {\n" +
                    "      \"hierarchy\": [],\n" +
                    "      \"attributes\": []\n" +
                    "    },\n" +
                    "    \"exclude\": {\n" +
                    "      \"hierarchy\": [],\n" +
                    "      \"attributes\": []\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            sendMessage(message);

            categoryRepository.getCategoryDocument(CATEGORY_ID);
            fail();
        } catch (CategoryNotFoundException e) {
            assertThat(e.getMessage(), is("Category information is not available for the requested category cat1234"));
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @SneakyThrows
    public void shouldSaveCategoryWithApplicableFilterWithValuesInTheMessage() {
        CATEGORY_ID = "cat1200";
        String message = "{\n" +
                "  \"id\": \"" + CATEGORY_ID + "\",\n" +
                "  \"eventType\": \"CATEGORY_UPDATED\",\n" +
                "  \"searchCriteria\": {\n" +
                "    \"include\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    },\n" +
                "    \"exclude\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    }\n" +
                "  }\n,"
                + "\"applicableFilters\":[{\"defaultName\":\"Length\",\"alternateName\":null,\"disabled\":[],\"values\":[\"Midi\",\"S\",\"Long\",\"Mid/Cropped\",\"MS\",\"Cropped Pant\",\"Midi/Cropped\",\"Knee\"]}]" +
                "}";

        sendMessage(message);

        CategoryDocument categoryDocument = categoryRepository.getCategoryDocument(CATEGORY_ID);
        assertThat(categoryDocument.getApplicableFilters().size(), is(1));
        assertThat(categoryDocument.getApplicableFilters().get(0).getDefaultName(), is("Length"));
        assertThat(categoryDocument.getApplicableFilters().get(0).getAlternateName(), Matchers.nullValue());
        assertThat(categoryDocument.getApplicableFilters().get(0).getValues(), is(Arrays.asList("Midi", "S", "Long", "Mid/Cropped", "MS", "Cropped Pant", "Midi/Cropped", "Knee")));
    }
    
    @Test
    @SneakyThrows
    public void shouldSaveCategoryWithApplicableFilterWithEmptyValuesInTheMessage() {
        CATEGORY_ID = "cat12345";
        String message = "{\n" +
                "  \"id\": \"" + CATEGORY_ID + "\",\n" +
                "  \"eventType\": \"CATEGORY_UPDATED\",\n" +
                "  \"searchCriteria\": {\n" +
                "    \"include\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    },\n" +
                "    \"exclude\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    }\n" +
                "  }\n,"
                + "\"applicableFilters\":[{\"defaultName\":\"Length\",\"alternateName\":null,\"disabled\":[],\"values\":[]}]" +
                "}";

        sendMessage(message);

        CategoryDocument categoryDocument = categoryRepository.getCategoryDocument(CATEGORY_ID);
        assertThat(categoryDocument.getApplicableFilters().size(), is(1));
        assertThat(categoryDocument.getApplicableFilters().get(0).getDefaultName(), is("Length"));
        assertThat(categoryDocument.getApplicableFilters().get(0).getAlternateName(), Matchers.nullValue());
        assertThat(categoryDocument.getApplicableFilters().get(0).getValues(), is(Collections.emptyList()));
    }
    
    @Test
    @SneakyThrows
    public void shouldSaveCategoryWithApplicableFilterWithNulllValuesAndAltNameInTheMessage() {
        CATEGORY_ID = "cat123456";
        String message = "{\n" +
                "  \"id\": \"" + CATEGORY_ID + "\",\n" +
                "  \"eventType\": \"CATEGORY_UPDATED\",\n" +
                "  \"searchCriteria\": {\n" +
                "    \"include\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    },\n" +
                "    \"exclude\": {\n" +
                "      \"hierarchy\": [],\n" +
                "      \"attributes\": []\n" +
                "    }\n" +
                "  }\n,"
                + "\"applicableFilters\":[{\"defaultName\":\"Length\",\"alternateName\":\"NullLength\",\"disabled\":[],\"values\":null}]" +
                "}";

        sendMessage(message);

        CategoryDocument categoryDocument = categoryRepository.getCategoryDocument(CATEGORY_ID);
        assertThat(categoryDocument.getApplicableFilters().size(), is(1));
        assertThat(categoryDocument.getApplicableFilters().get(0).getDefaultName(), is("Length"));
        assertThat(categoryDocument.getApplicableFilters().get(0).getAlternateName(), is("NullLength"));
        assertThat(categoryDocument.getApplicableFilters().get(0).getValues(), Matchers.nullValue());
    }
}
