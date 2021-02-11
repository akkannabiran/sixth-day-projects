package com.sixthday.store.config;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Constants {

    public static class Logging {
        public static final String UNKNOWN = "UNKNOWN";
        public static final String TLTSID_COOKIE_KEY = "TLTSID";
        public static final String JSESSIONID_COOKIE_KEY = "JSESSIONID";
        public static final String DYN_USER_ID_COOKIE_KEY = "DYN_USER_ID";
        public static final String TRACE_ID = "trace_id";
        public static final String TRACE_ID_HEADER_TAG = "x-sixthday-trace-id";
        public static final String BRAND_CODE_KEY = "brand_code";
        public static final String SKU_ID = "sku_id";
        public static final String SKU_IDS = "sku_ids";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String MILE_RADIUS = "mile_radius";
        public static final String CALLER = "caller";
        public static final String STORE_ID = "store_id";
        public static final String STORE_QUEUE_NAME = "${sixthday-store-sub.queue-config.queue-name}";
        public static final String STORE_INVENTORY_QUEUE_NAME = "${sixthday-store-sub.queue-config.store-sku-inventory-queue-name}";
        public static final String STORE_INVENTORY_BY_SKU_QUEUE_NAME = "${sixthday-store-sub.store-inventory-by-sku-queue-config.queue-name}";
        public static final String STORE_INDEX_NAME = "${sixthday-store-api.elastic-search-config.store-index-name}";
        public static final String STORE_INVENTORY_INDEX_NAME = "${sixthday-store-api.elastic-search-config.store-sku-inventory-index-name}";
        public static final String STORE_INVENTORY_BY_SKU_DYNAMO_TABLE_NAME = "StoreInventoryBySku";
        public static final String PRODUCT_QUEUE_NAME = "${spring.cloud.stream.bindings.output.destination}";
        public static final String STORE_INVENTORY_BY_SKU_OUT_STREAM_NAME = "${spring.cloud.stream.bindings.storeInventoryBySKU.destination}";
        public static final String STORE_INVENTORY_BY_SKU_FOUNDATION_OUT_STREAM_NAME = "${spring.cloud.stream.bindings.storeInventoryBySKUFoundation.destination}";
        public static final String STORE_PRODUCTION_FOUNDATION_MODE = "${sixthday-store-sub.products-foundation-mode}";
        public static final String MDC_MESSAGE_ID = "messageId";
        public static final String MDC_MESSAGE_TYPE = "messageType";
        public static final String MDC_CONTEXT_ID = "contextId";
        public static final String MDC_ORIGIN_TIMESTAMP = "OriginTimestamp";
        public static final String MDC_SOURCE_PROPERTY = "sixthdaySRC";
        public static final String MDC_DESTINATION_PROPERTY = "sixthdayDEST";
        public static final String MDC_RESOURCE_PROPERTY = "sixthdayRESOURCE";
        public static final String CONTENT_SYNC_sixthday_RESOURCE = "ElasticSearch";
        public static final String CONTENT_SYNC_AWS_RESOURCE = "DynamoDB";
        public static final Marker CONTENT_SYNC_LOG_MAKER = MarkerFactory.getMarker("ContentSyncLogMarker");
        public static final Marker NO_MDC_LOG_MARKER = MarkerFactory.getMarker("NoMDCLogMarker");
    }

    public static class Filters {
        public static final String IS_ELIGIBLE_FOR_BOPS = "eligibleForBOPS";
        public static final String IS_DISPLAYABLE = "displayable";
    }

    public static class Events {
        public static final String API_EVENT = "API";
        public static final String REPOSITORY_EVENT = "REPOSITORY";
    }

    public static class Actions {
        public static final String STORE_SEARCH_GOT_WWW = "STORE_SEARCH_GOT_WWW";
        public static final String STORE_SEARCH_DYNAMO_DB = "STORE_SEARCH_DYNAMO_DB";
        public static final String STORE_SEARCH = "STORE_SEARCH";
        public static final String GET_STORE = "GET_STORE";
    }
}

