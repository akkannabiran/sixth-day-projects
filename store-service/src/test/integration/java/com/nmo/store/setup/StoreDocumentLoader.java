package com.sixthday.store.setup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sixthday.store.models.storeindex.StoreDocument;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

@Slf4j
public class StoreDocumentLoader {
    public static final int DISABLE_BULK_ACTIONS = -1;
    public static final String INDEX_NAME = "store_index";
    private RestHighLevelClient client;
    private List<StoreDocument>  storeDocuments = new ArrayList<>();
    public static final int DELAY_MILLIS = 500;

    public StoreDocumentLoader(RestHighLevelClient client) {
        this.client = client;
    }

    public StoreDocumentLoader add(StoreDocument storeDocumentJson) {
        storeDocuments.add(storeDocumentJson);
        return this;
    }

    public void save() {
        BulkProcessor.Listener mock = mock(BulkProcessor.Listener.class);
        BulkProcessor bulkProcessor = BulkProcessor
                .builder((request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), mock)
                .setBulkActions(DISABLE_BULK_ACTIONS)
                .setConcurrentRequests(0)
                .build();

        storeDocuments.forEach(storeDocument -> addStoreDocumentToBulkProcessor(bulkProcessor, storeDocument));
        bulkProcessor.close();
        try {
            client.indices().refresh(new RefreshRequest("store_index"), RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        BulkRequest bulkRequest = new BulkRequest();

        storeDocuments.forEach( storeDocument -> bulkRequest.add(new DeleteRequest("store_index", StoreDocument.DOCUMENT_TYPE, storeDocument.getId())));

        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addStoreDocumentToBulkProcessor(BulkProcessor bulkProcessor, StoreDocument storeDocument) {
        try {
            bulkProcessor.add(new IndexRequest(INDEX_NAME, StoreDocument.DOCUMENT_TYPE, storeDocument.getId()).source(storeDocument.asJsonString(), XContentType.JSON));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse store document as Json", e);
        }
    }

    
    public Map<String, Object> findById(String id) throws InterruptedException, IOException {
        Thread.sleep(DELAY_MILLIS);
        client.indices().refresh(new RefreshRequest(INDEX_NAME),RequestOptions.DEFAULT);
        GetResponse getResponse = client.get(new GetRequest(INDEX_NAME, StoreDocument.DOCUMENT_TYPE, id), RequestOptions.DEFAULT);
        Map<String, Object> sourceMap = getResponse.getSourceAsMap();
        sourceMap.put("version", getResponse.getVersion());
        return sourceMap;
    }
}
