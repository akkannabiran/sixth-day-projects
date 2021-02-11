package com.sixthday.navigation;

import au.com.dius.pact.consumer.*;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.Filter;
import com.sixthday.navigation.api.elasticsearch.models.PriceRangeAtg;
import com.sixthday.navigation.integration.receiver.CategoryMessageReceiver;
import com.sixthday.navigation.integration.services.CategorySyncService;
import com.sun.jna.NativeLong;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.Message;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.sixthday.navigation.integration.messages.CategoryMessage.EventType.CATEGORY_REMOVED;
import static com.sixthday.navigation.integration.messages.CategoryMessage.EventType.CATEGORY_UPDATED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryMessageReceiverContractTest {

    @Rule
    public MessagePactProviderRule mockProvider = new MessagePactProviderRule("ATG-rabbitmq", this);
    @Mock
    private CategorySyncService categorySyncService;
    @Mock
    private Message message;
    @InjectMocks
    private CategoryMessageReceiver categoryMessageReceiver;

    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithValidMessage(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("id");
        body.stringValue("eventType", "CATEGORY_UPDATED");
        body.stringType("displayName");
        body.stringValue("messageType", "message1");
        body.stringValue("defaultPath", "idCat2_idCat1");
        body.stringValue("firstSellableProductImageUrl", "http://some-host/product-image-url.jpg");

        body.stringType("name");
        body.stringType("leftNavImageAvailableOverride");
        body.stringType("templateType", "ChanelP3");
        body.stringType("alternateSeoName");
        body.stringType("seoTitleOverride");
        body.stringType("canonicalUrl");
        body.stringType("seoContentTitle");
        body.stringType("seoContentDescription");
        body.stringType("seoTags");

        body.booleanType("boutique");
        body.booleanType("boutiqueChild");
        body.booleanType("imageAvailable");
        body.booleanType("mobileHideEntrySubcats");
        body.booleanType("leftNavImageAvailable");
        body.booleanType("htmlAvailable");
        body.booleanType("expandCategory");
        body.booleanType("dontShowChildren");
        body.booleanType("personalized");
        body.booleanType("hidden");
        body.booleanType("noResults");
        body.booleanType("displayAsGroups");
        body.booleanType("driveToGroupPDP");
        body.booleanType("excludeFromPCS");
        body.booleanType("showAllProducts");
        body.object("productRefinements")
                .booleanValue("regularPriceOnly", false)
                .booleanValue("salePriceOnly", false)
                .booleanValue("adornedPriceOnly", false)
                .booleanValue("includeAllItems", true)
                .booleanValue("adornedAndSaleOnly", false)
                .closeObject();
        body.array("preferredProductIds").closeArray();
        body.stringType("imageAvailableOverride");
        body.booleanType("hideMobileImage");
        body.array("applicableFilters")
                .object()
                .stringValue("LifeStyle", "lifestyle")
                .closeObject()
                .closeArray();
        body.array("excludedCountries")
                .stringType()
                .closeArray();
        body.object("searchCriteria")
                .object("include")
                .minArrayLike("hierarchy", 1)
                .closeObject()
                .closeArray()
                .array("promotions")
                .string("EP1234")
                .string("EP1234")
                .closeArray()
                .closeObject()
                .object("exclude")
                .minArrayLike("hierarchy", 1)
                .closeObject()
                .closeArray()
                .array("promotions")
                .string("EP321")
                .closeArray()
                .closeObject()
                .closeObject();

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with valid eventType")
                .expectsToReceive("valid category message")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithApplicableFilterHavingValues(MessagePactBuilder builder) {
        PactDslJsonBody body = mustHaveJsonBody()
                .array("applicableFilters")
                .object()
                .stringValue("defaultName", "Lifestyle")
                .stringValue("alternateName", null)
                .array("disabled").closeArray()
                .array("values")
                  .stringValue("LS1")
                  .stringValue("LS2")
                .closeArray()
                .closeObject()
                .closeArray()
                .asBody();

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with applicable filter with values")
                .expectsToReceive("category filter with values is saved")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    
    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithPromoPriceMessage(MessagePactBuilder builder) {
        PactDslJsonBody body = mustHaveJsonBody()
                .object("productRefinements")
                .object("priceRange")
                .stringValue("option", "PROMO_PRICE")
                .numberValue("min", new NativeLong(20))
                .numberValue("max", new NativeLong(50))
                .closeObject()
                .closeObject()
                .asBody();

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with promo price")
                .expectsToReceive("category promo price is saved")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithType113PercentOffMessage(MessagePactBuilder builder) {
        PactDslJsonBody body = mustHaveJsonBody()
                .object("productRefinements")
                .object("priceRange")
                .stringValue("option", "TYPE113_PERCENT_OFF")
                .numberValue("min", new NativeLong(20))
                .numberValue("max", new NativeLong(50))
                .closeObject()
                .closeObject()
                .asBody();

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with promo price")
                .expectsToReceive("category promo price is saved")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithPromoPriceOffMessage(MessagePactBuilder builder) {
        PactDslJsonBody body = mustHaveJsonBody()
                .object("productRefinements")
                .object("priceRange")
                .stringValue("option", "OFF")
                .numberValue("min", new NativeLong(0))
                .numberValue("max", new NativeLong(0))
                .closeObject()
                .closeObject()
                .asBody();

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with promo price")
                .expectsToReceive("category promo price is saved")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithThumbImageShot(MessagePactBuilder builder) {
        PactDslJsonBody body = mustHaveJsonBody()
                .stringValue("thumbImageShot", "z");

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with thumbImageShot")
                .expectsToReceive("category thumbImageShot is saved")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithCategoryRedirectUrlAndType(MessagePactBuilder builder) {
        PactDslJsonBody body = mustHaveJsonBody()
                .stringValue("redirectType", "301")
                .stringValue("redirectTo", "cat234");

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with redirectType and redirectUrl")
                .expectsToReceive("category redirectType and redirectUrl is saved")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithCategoryDeletedFlagUnderCategoryUpdatedEventType(MessagePactBuilder builder) {
        PactDslJsonBody body = mustHaveJsonBody()
                .stringType("id")
                .stringValue("eventType", "CATEGORY_UPDATED")
                .booleanValue("isDeleted", true);

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with isDeleted Flag")
                .expectsToReceive("category isDeleted Flag is saved")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @Pact(provider = "ATG-rabbitmq", consumer = "navigation-service")
    public MessagePact createPactWithCategoryDeletedFlagUnderCategoryRemovedEventType(MessagePactBuilder builder) {
        PactDslJsonBody body = mustHaveJsonBody()
                .stringType("id")
                .stringValue("eventType", "CATEGORY_REMOVED")
                .booleanValue("isDeleted", true);

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("contentType", "application/json");

        return builder.given("category message with isDeleted Flag")
                .expectsToReceive("category isDeleted Flag is saved")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @NotNull
    private PactDslJsonBody mustHaveJsonBody() {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("id")
                .stringValue("eventType", "CATEGORY_UPDATED")
                .stringValue("messageType", "message1")
                .object("searchCriteria")
                .object("include")
                .minArrayLike("hierarchy", 1)
                .closeObject()
                .closeArray()
                .array("promotions")
                .closeArray()
                .closeObject()
                .object("exclude")
                .array("promotions")
                .closeArray()
                .minArrayLike("hierarchy", 1)
                .closeObject()
                .closeArray()
                .closeObject()
                .closeObject();
        return body;
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithValidMessage")
    public void responseWhenValidEvent() {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);
        categoryMessageReceiver.onMessage(message, null);
        verify(categorySyncService).upsertOrDeleteCategory(any(CategoryDocument.class), eq(CATEGORY_UPDATED));
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithApplicableFilterHavingValues")
    public void respondWithApplicableFilterWithValues() {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> documentArgumentCaptor = ArgumentCaptor.forClass(CategoryDocument.class);

        verify(categorySyncService).upsertOrDeleteCategory(documentArgumentCaptor.capture(), eq(CATEGORY_UPDATED));
        Filter applicableFilter = documentArgumentCaptor.getValue().getApplicableFilters().get(0);
        assertThat(applicableFilter.getValues(), is(Arrays.asList("LS1", "LS2")));
    }
    
    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithPromoPriceMessage")
    public void respondWithPromoPriceMessage() {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> documentArgumentCaptor = ArgumentCaptor.forClass(CategoryDocument.class);

        verify(categorySyncService).upsertOrDeleteCategory(documentArgumentCaptor.capture(), eq(CATEGORY_UPDATED));
        PriceRangeAtg priceRange = documentArgumentCaptor.getValue().getProductRefinements().getPriceRange();
        assertThat(priceRange.getMin(), is(new BigDecimal(20)));
        assertThat(priceRange.getMax(), is(new BigDecimal(50)));
        assertThat(priceRange.getOption(), is("PROMO_PRICE"));
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithType113PercentOffMessage")
    public void respondWithType113PercentMessage() throws Exception {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> documentArgumentCaptor = ArgumentCaptor.forClass(CategoryDocument.class);

        verify(categorySyncService).upsertOrDeleteCategory(documentArgumentCaptor.capture(), eq(CATEGORY_UPDATED));
        PriceRangeAtg priceRange = documentArgumentCaptor.getValue().getProductRefinements().getPriceRange();
        assertThat(priceRange.getMin(), is(new BigDecimal(20)));
        assertThat(priceRange.getMax(), is(new BigDecimal(50)));
        assertThat(priceRange.getOption(), is("TYPE113_PERCENT_OFF"));
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithPromoPriceOffMessage")
    public void respondWithPromoPriceOffMessage() {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> documentArgumentCaptor = ArgumentCaptor.forClass(CategoryDocument.class);

        verify(categorySyncService).upsertOrDeleteCategory(documentArgumentCaptor.capture(), eq(CATEGORY_UPDATED));
        PriceRangeAtg priceRange = documentArgumentCaptor.getValue().getProductRefinements().getPriceRange();
        assertThat(priceRange.getMin(), is(new BigDecimal(0)));
        assertThat(priceRange.getMax(), is(new BigDecimal(0)));
        assertThat(priceRange.getOption(), is("OFF"));
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithThumbImageShot")
    public void respondWithThumbImageShotMessage() {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> documentArgumentCaptor = ArgumentCaptor.forClass(CategoryDocument.class);

        verify(categorySyncService).upsertOrDeleteCategory(documentArgumentCaptor.capture(), eq(CATEGORY_UPDATED));
        String thumbImageShot = documentArgumentCaptor.getValue().getThumbImageShot();
        assertThat(thumbImageShot, is("z"));
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithCategoryRedirectUrlAndType")
    public void respondWithRedirectTypeAndRedirectUrlMessage() {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> documentArgumentCaptor = ArgumentCaptor.forClass(CategoryDocument.class);

        verify(categorySyncService).upsertOrDeleteCategory(documentArgumentCaptor.capture(), eq(CATEGORY_UPDATED));
        String redirectUrl = documentArgumentCaptor.getValue().getRedirectTo();
        String redirectType = documentArgumentCaptor.getValue().getRedirectType();

        assertThat(redirectType, is("301"));
        assertThat(redirectUrl, is("cat234"));
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithCategoryDeletedFlagUnderCategoryUpdatedEventType")
    public void respondWithIsDeletedMessageOnCategoryUpdatedEventType() {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> documentArgumentCaptor = ArgumentCaptor.forClass(CategoryDocument.class);

        verify(categorySyncService).upsertOrDeleteCategory(documentArgumentCaptor.capture(), eq(CATEGORY_UPDATED));
        boolean isDeleted = documentArgumentCaptor.getValue().isDeleted();

        assertThat(isDeleted, is(true));
    }

    @Test
    @PactVerification(value = "ATG-rabbitmq", fragment = "createPactWithCategoryDeletedFlagUnderCategoryRemovedEventType")
    public void respondWithIsDeletedMessageOnCategoryRemovedEventType() {
        byte[] currentMessage = mockProvider.getMessage();
        when(message.getBody()).thenReturn(currentMessage);

        categoryMessageReceiver.onMessage(message, null);

        ArgumentCaptor<CategoryDocument> documentArgumentCaptor = ArgumentCaptor.forClass(CategoryDocument.class);

        verify(categorySyncService).upsertOrDeleteCategory(documentArgumentCaptor.capture(), eq(CATEGORY_REMOVED));
        boolean isDeleted = documentArgumentCaptor.getValue().isDeleted();

        assertThat(isDeleted, is(true));
    }
}
