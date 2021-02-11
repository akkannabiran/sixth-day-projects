package com.sixthday.store.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.config.SubscriberConfiguration;
import com.sixthday.store.exceptions.StoreUpsertFailedException;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreMessage;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class StoreSyncRepository {
	private final RestHighLevelClient client;
	private ObjectMapper genericMapper;
	
	@Autowired
	SubscriberConfiguration subscriberConfiguration;

	@Autowired
	public StoreSyncRepository(SubscriberConfiguration subscriberConfiguration , RestHighLevelClient client) {
		this.subscriberConfiguration = subscriberConfiguration;
		this.client = client;
		this.genericMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	public void createOrUpdateStore(StoreDocument storeDocument, StoreMessage.EventType eventType)
			throws StoreUpsertFailedException {
		log.debug("StoreSyncRepository=\"CREATE_OR_UPDATE_STORE_REPOSITORY\", Status=\"Begin\", MessageId=\"{}\"",
				storeDocument.getId());
		// TO DO add logic to return if eventtype is not update if eventtype is not update return null
		Instant start = Instant.now();
		try {
			String document = genericMapper.writeValueAsString(storeDocument);
			IndexRequest indexRequest = createIndexRequest(storeDocument, document);
			UpdateRequest updateRequest = createUpdateRequestBuilder(storeDocument, indexRequest);
			executeAsyncUpdateRequest(updateRequest);
		} catch (JsonProcessingException e) {
			log.error("Error occurred while constructing the store update query", e);
		} finally {
			log.debug("StoreSyncRepositoryUpdate=\"CREATE_OR_UPDATE_STORE_REPOSITORY\", Status=\"End\", MessageId=\"{}\", DurationInMs=\"{}\"", storeDocument.getId(),
					Duration.between(start, Instant.now()).toMillis());
		}
	}

	protected void executeAsyncUpdateRequest(UpdateRequest updateRequest) {
		client.updateAsync(updateRequest, RequestOptions.DEFAULT, new ESUpdateActionListener(Instant.now(), MDC.getCopyOfContextMap()));
	}

	private UpdateRequest createUpdateRequestBuilder(StoreDocument storeDocument, IndexRequest indexRequest) throws JsonProcessingException {
		return new UpdateRequest(subscriberConfiguration.getElasticSearchConfig().getStoreIndexName(),
				StoreDocument.DOCUMENT_TYPE,
				storeDocument.getId()).doc(genericMapper.writeValueAsString(storeDocument),XContentType.JSON).upsert(indexRequest).docAsUpsert(true);
	}

	public IndexRequest createIndexRequest(StoreDocument storeDocument, String document) {
		return new IndexRequest(subscriberConfiguration.getElasticSearchConfig().getStoreIndexName(),
				StoreDocument.DOCUMENT_TYPE,
				storeDocument.getId()).
				source(document, XContentType.JSON);
	}
	
}
