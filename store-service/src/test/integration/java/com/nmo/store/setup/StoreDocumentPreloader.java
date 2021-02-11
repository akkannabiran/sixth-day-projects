package com.sixthday.store.setup;

import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.mockito.Mockito.mock;

@Deprecated
public class StoreDocumentPreloader {
    private final String TEST_STORE_ID_1 = "100/DT";
    private final String TEST_STORE_ID_2 = "101/DT";
    private final String TEST_STORE_ID_3 = "102/DT";
    private final String TEST_STORE_ID_4 = "103/DT";
    private final String TEST_STORE_ID_5 = "104/DT";
    private final String TEST_STORE_ID_6 = "105/DT";

    private final String TEST_STORE_NUMBER_1 = "99997";
    private final String TEST_STORE_NUMBER_2 = "99998";
    private final String TEST_STORE_NUMBER_3 = "99999";

    private final int DISABLE_BULK_ACTIONS = -1;

    private RestHighLevelClient client;

    public StoreDocumentPreloader(RestHighLevelClient client) {
        this.client = client;
    }

    public void removeTestStoresFromElasticSearch() {
                BulkRequest bulkRequest = new BulkRequest();
                bulkRequest.add(new DeleteRequest("store_index", StoreDocument.DOCUMENT_TYPE, TEST_STORE_ID_1));
                bulkRequest.add(new DeleteRequest("store_index", StoreDocument.DOCUMENT_TYPE, TEST_STORE_ID_2));
                bulkRequest.add(new DeleteRequest("store_index", StoreDocument.DOCUMENT_TYPE, TEST_STORE_ID_3));
        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTestStoresToElasticSearch() throws Exception {

        BulkProcessor.Listener mock = mock(BulkProcessor.Listener.class);
        BulkProcessor bulkProcessor = BulkProcessor
                .builder((request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), mock)
                .setBulkActions(DISABLE_BULK_ACTIONS)
                .setConcurrentRequests(0)
                .build();

        bulkProcessor
                .add(new IndexRequest("store_index", StoreDocument.DOCUMENT_TYPE, TEST_STORE_ID_1).source(jsonBuilder()
                        .startObject()
                        .field("id", TEST_STORE_ID_1)
                        .field("storeNumber", TEST_STORE_NUMBER_1)
                        .field("storeName", "someStoreName")
                        .field("storeDescription", "someStoreDescription")
                        .field("addressLine1", "someAddress1")
                        .field("addressLine2", "someAddress2")
                        .field("city", "someCity")
                        .field("state", "someState")
                        .field("phoneNumber", "number")
                        .field("storeHours", "storeHours")
                        .field("zipCode", "zipCode")
                        .field("displayable", true)
                        .field("eligibleForBOPS", false)
                        .endObject(),XContentType.JSON))
                .add(new IndexRequest("store_index", StoreDocument.DOCUMENT_TYPE, TEST_STORE_ID_2).source(jsonBuilder()
                        .startObject()
                        .field("id", TEST_STORE_ID_2)
                        .field("storeNumber", TEST_STORE_NUMBER_2)
                        .field("storeName", "someStoreName")
                        .field("storeDescription", "someStoreDescription")
                        .field("addressLine1", "someAddress1")
                        .field("addressLine2", "someAddress2")
                        .field("city", "someCity")
                        .field("state", "someState")
                        .field("phoneNumber", "number")
                        .field("storeHours", "storeHours")
                        .field("zipCode", "zipCode")
                        .field("displayable", false)
                        .field("eligibleForBOPS", true)
                        .endObject(),XContentType.JSON))
                .add(new IndexRequest("store_index", StoreDocument.DOCUMENT_TYPE, TEST_STORE_ID_3).source(jsonBuilder()
                        .startObject()
                        .field("id", TEST_STORE_ID_3)
                        .field("storeNumber", TEST_STORE_NUMBER_3)
                        .field("storeName", "someStoreName")
                        .field("storeDescription", "someStoreDescription")
                        .field("addressLine1", "someAddress1")
                        .field("addressLine2", "someAddress2")
                        .field("city", "someCity")
                        .field("state", "someState")
                        .field("phoneNumber", "number")
                        .field("storeHours", "storeHours")
                        .field("zipCode", "zipCode")
                        .field("displayable", false)
                        .field("eligibleForBOPS", true)
                        .endObject(),XContentType.JSON));

        bulkProcessor.close();

        client.indices().refresh(new RefreshRequest("store_index"), RequestOptions.DEFAULT);
    }

    public void addTestSkuToElasticSearch() throws IOException {

        BulkProcessor.Listener mock = mock(BulkProcessor.Listener.class);
        BulkProcessor bulkProcessor = BulkProcessor
                .builder((request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), mock)
                .setBulkActions(DISABLE_BULK_ACTIONS)
                .setConcurrentRequests(0)
                .build();

        bulkProcessor
                .add(new IndexRequest("store_sku_inventory_index", StoreSkuInventoryDocument.DOCUMENT_TYPE, "sku118570888:99888")
                        .source(
                                jsonBuilder()
                                        .startObject()
                                        .field("storeId", TEST_STORE_ID_4)
                                        .field("storeNumber", "99888")
                                        .field("quantity", 3)
                                        .field("inventoryLevelCode", "0")
                                        .field("tags", "[]")
                                        .field("bopsQuantity", 1)
                                        .field("skuId", "sku118570888")
                                        .field("locationNumber", "1025")
                                        .endObject()))
                .add(new IndexRequest("store_sku_inventory_index", StoreSkuInventoryDocument.DOCUMENT_TYPE, "sku118570889:99777")
                        .source(
                                jsonBuilder()
                                        .startObject()
                                        .field("storeId", TEST_STORE_ID_5)
                                        .field("storeNumber", "99777")
                                        .field("quantity", 7)
                                        .field("inventoryLevelCode", "1")
                                        .field("tags", "[]")
                                        .field("bopsQuantity", 1)
                                        .field("skuId", "sku118570889")
                                        .field("locationNumber", "1025")
                                        .endObject()))
                .add(new IndexRequest("store_sku_inventory_index", StoreSkuInventoryDocument.DOCUMENT_TYPE, "sku118570889:99666")
                        .source(
                                jsonBuilder()
                                        .startObject()
                                        .field("storeId", TEST_STORE_ID_6)
                                        .field("storeNumber", "99666")
                                        .field("quantity", 9)
                                        .field("inventoryLevelCode", "3")
                                        .field("tags", "[]")
                                        .field("bopsQuantity", 1)
                                        .field("skuId", "sku118570889")
                                        .field("locationNumber", "1025")
                                        .endObject()));


        bulkProcessor.close();
        client.indices().refresh(new RefreshRequest("store_sku_inventory_index"), RequestOptions.DEFAULT);
    }

    public void removeTestSkuFromElasticSearch()  {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new DeleteRequest("store_sku_inventory_index", StoreSkuInventoryDocument.DOCUMENT_TYPE, TEST_STORE_ID_4));
        bulkRequest.add(new DeleteRequest("store_sku_inventory_index", StoreSkuInventoryDocument.DOCUMENT_TYPE, TEST_STORE_ID_5));
        bulkRequest.add(new DeleteRequest("store_sku_inventory_index", StoreSkuInventoryDocument.DOCUMENT_TYPE, TEST_STORE_ID_6));
        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
