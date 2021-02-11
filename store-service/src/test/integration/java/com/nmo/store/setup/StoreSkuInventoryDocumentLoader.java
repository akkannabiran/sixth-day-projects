package com.sixthday.store.setup;

import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class StoreSkuInventoryDocumentLoader {
    private static final String INDEX_NAME = "store_sku_inventory_index";
    public static final int DELAY_MILLIS = 500;
    private RestHighLevelClient client;

    public StoreSkuInventoryDocumentLoader(RestHighLevelClient client) {
        this.client = client;
    }

    public Map<String, Object> findById(String id) throws InterruptedException, IOException {
        Thread.sleep(DELAY_MILLIS);
        client.indices().refresh(new RefreshRequest(INDEX_NAME), RequestOptions.DEFAULT);
        GetResponse getResponse = client.get(new GetRequest(INDEX_NAME, StoreSkuInventoryDocument.DOCUMENT_TYPE, id), RequestOptions.DEFAULT);
        Map<String, Object> sourceMap = getResponse.getSourceAsMap();
        sourceMap.put("version", getResponse.getVersion());
        return sourceMap;
    }
}
