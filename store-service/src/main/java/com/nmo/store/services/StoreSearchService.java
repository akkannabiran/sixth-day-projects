package com.sixthday.store.services;

import com.sixthday.store.exceptions.DocumentRetrievalException;
import com.sixthday.store.models.*;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeinventoryindex.StoreSkuInventoryDocument;
import com.sixthday.store.repository.ElasticsearchRepository;
import com.sixthday.store.repository.GotWWWRepository;
import com.sixthday.store.repository.dynamodb.DynamoDBStoreInventoryRepository;
import com.sixthday.store.toggles.Features;
import com.toggler.core.toggles.Toggle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sixthday.store.config.Constants.Filters.IS_DISPLAYABLE;
import static com.sixthday.store.config.Constants.Filters.IS_ELIGIBLE_FOR_BOPS;
@Service
@RequestScope
public class StoreSearchService {

    private final GotWWWRepository gotWWWRepository;

    private final ElasticsearchRepository elasticsearchRepository;
    private DynamoDBStoreInventoryRepository dynamoDBStoreInventoryRepository;

    @Autowired
    public StoreSearchService(GotWWWRepository gotWWWRepository, ElasticsearchRepository elasticsearchRepository,
            DynamoDBStoreInventoryRepository dynamoDBStoreInventoryRepository) {
        this.gotWWWRepository = gotWWWRepository;
        this.elasticsearchRepository = elasticsearchRepository;
        this.dynamoDBStoreInventoryRepository = dynamoDBStoreInventoryRepository;
    }
    
    
    public List<sixthdayStore> getStoresFromElasticSearch(final String brandCode, final StoreSearchLocation storeSearchLocation, final Optional<Integer> radius, String skuId, Integer quantity) {
        List<String> storeNumbers = gotWWWRepository.getStores(brandCode, storeSearchLocation, radius);

        Future<List<StoreDocument>> storeDocumentsFuture = CompletableFuture.supplyAsync(() ->
                elasticsearchRepository.filterStoresForStoreNumbers(storeNumbers, IS_ELIGIBLE_FOR_BOPS));
      
        Map<String, StoreSkuInventoryDocument> skuMapByStoreNumber = elasticsearchRepository.getAllStoresSkus(storeNumbers, skuId)
                .stream().collect(Collectors.toMap(StoreSkuInventoryDocument::getStoreNumber, Function.identity()));

        return processStoreDataFromInventory(quantity, storeNumbers, storeDocumentsFuture, skuMapByStoreNumber);
    }
    
    @Toggle(name=Features.READ_SKU_STORES, fallback="getStoresFromElasticSearch")
    public List<sixthdayStore> getStores(final String brandCode, final StoreSearchLocation storeSearchLocation, final Optional<Integer> radius, String skuId, Integer quantity) {
        List<String> storeNumbers = gotWWWRepository.getStores(brandCode, storeSearchLocation, radius);

        Future<List<StoreDocument>> storeDocumentsFuture = CompletableFuture.supplyAsync(() ->
                elasticsearchRepository.filterStoresForStoreNumbers(storeNumbers, IS_ELIGIBLE_FOR_BOPS));
      
        Map<String, StoreSkuInventoryDocument> skuMapByStoreNumber = dynamoDBStoreInventoryRepository.getAllStoresSkus(storeNumbers, skuId)
                .stream().collect(Collectors.toMap(StoreSkuInventoryDocument::getStoreNumber, Function.identity()));

        return processStoreDataFromInventory(quantity, storeNumbers, storeDocumentsFuture, skuMapByStoreNumber);
    }

    public StoreSkuInventory getSKUsInventory(String storeId, String skuIds) {
        Iterable<StoreInventoryBySKUDocument> storeInventoryBySKUDocuments = dynamoDBStoreInventoryRepository.getSKUsInventory(
                new HashSet<>(Arrays.asList(skuIds.split(","))));

        List<SkuInventory> skuInventories = new ArrayList<>();
        Store store = new Store();

        storeInventoryBySKUDocuments.forEach(storeInventoryBySKUDocument -> {
            Optional<StoreInventoryItem> storeInventoryItemOptional = storeInventoryBySKUDocument.getStoreInventoryItems().stream().filter(storeInventoryItem -> storeId.equals(storeInventoryItem.getStoreNumber())).findAny();
            if (storeInventoryItemOptional.isPresent()) {
                skuInventories.add(new SkuInventory(storeInventoryBySKUDocument.getSkuId(),
                        storeInventoryItemOptional.get().getInventoryLevel(),
                        storeInventoryItemOptional.get().getQuantity(),
                        storeInventoryItemOptional.get().getBopsQuantity()));

                store.setLocationNumber(storeInventoryItemOptional.get().getLocationNumber());
                store.setStoreId(storeInventoryItemOptional.get().getStoreId());
                store.setStoreNumber(storeInventoryItemOptional.get().getStoreNumber());
            }
        });

        StoreSkuInventory storeSkuInventory = new StoreSkuInventory();
        storeSkuInventory.setStore(store.getStoreId() != null ? store : null);
        storeSkuInventory.setSkuInventories(skuInventories);

        return storeSkuInventory;
    }
    
	private List<sixthdayStore> processStoreDataFromInventory(Integer quantity, List<String> storeNumbers,
			Future<List<StoreDocument>> storeDocumentsFuture,
			Map<String, StoreSkuInventoryDocument> skuMapByStoreNumber) {
		try {
			List<StoreDocument> storeDocList = storeDocumentsFuture.get();
            return storeDocList.stream()
                .map(storeDocument -> {
                    if (skuMapByStoreNumber.containsKey(storeDocument.getStoreNumber())) {
                        StoreSkuInventoryDocument storeSkuInventoryDocument = skuMapByStoreNumber.get(storeDocument.getStoreNumber());
                        return sixthdayStore.from(storeDocument, Optional.of(new SkuAvailabilityInfo(storeSkuInventoryDocument, quantity)));
                    } else {
                        return sixthdayStore.from(storeDocument, Optional.empty());
                    }
                })
                .sorted(Comparator.comparingInt(sixthdayStore -> storeNumbers.indexOf(sixthdayStore.getStoreNumber())))
                .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new DocumentRetrievalException(e);
        }
	}

  

  public List<sixthdayStore> getStores(final String brandCode, final StoreSearchLocation storeSearchLocation, final Optional<Integer> radius) {
        List<String> storeNumbers = gotWWWRepository.getStores(brandCode, storeSearchLocation, radius);

        List<StoreDocument> storeDocuments = elasticsearchRepository.filterStoresForStoreNumbers(storeNumbers, IS_DISPLAYABLE);

        return storeDocuments.stream()
                .map(sixthdayStore::from)
                .sorted(Comparator.comparingInt(sixthdayStore -> storeNumbers.indexOf(sixthdayStore.getStoreNumber())))
                .collect(Collectors.toList());
    }

    public List<sixthdayStore> getStores(String storeId) {
        List<StoreDocument> storeDocuments = elasticsearchRepository.getStoresById(storeId);
        return storeDocuments.stream()
                .map(sixthdayStore::from)
                .collect(Collectors.toList());
    }
}
