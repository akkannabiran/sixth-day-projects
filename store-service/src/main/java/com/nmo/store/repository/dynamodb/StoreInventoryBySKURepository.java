package com.sixthday.store.repository.dynamodb;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import com.sixthday.store.models.StoreInventoryBySKUDocument;

@EnableScan
public interface StoreInventoryBySKURepository extends CrudRepository<StoreInventoryBySKUDocument, String> {

}
