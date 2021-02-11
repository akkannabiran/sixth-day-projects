package com.sixthday.store.repository;

import com.sixthday.store.models.StoreSearchLocation;

import java.util.List;
import java.util.Optional;

public interface GotWWWRepositoryInterface {
    List<String> getStores(final String brandCode, final StoreSearchLocation storeSearchLocation, final Optional<Integer> radius);
}
