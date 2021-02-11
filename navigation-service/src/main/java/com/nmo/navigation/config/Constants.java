package com.sixthday.navigation.config;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Constants {
    public static final String AMAZON_S3_GET_SILO = "AMAZON_S3_GET_SILO";
    public static final String ACTION = "ACTION";
    public static final String API = "API";
    public static final String CONTENT_SYNC_ES_RESOURCE = "ElasticSearch";
    public static final String DELETE_CATEGORY_DOCUMENT_FROM_EC = "DELETE_CATEGORY_DOCUMENT_FROM_EC";
    public static final String ELASTICSEARCH_ASYNC_DELETE_LEFTNAV_DOCUMENT = "ELASTICSEARCH_ASYNC_DELETE_LEFTNAV_DOCUMENT";
    public static final String ELASTICSEARCH_ASYNC_UPDATE_CATEGORY_DOCUMENT = "ELASTICSEARCH_ASYNC_UPDATE_CATEGORY_DOCUMENT";
    public static final String GET_LEFTNAV_DOCUMENT_FROM_ES = "GET_LEFTNAV_DOCUMENT_FROM_ES";
    public static final String GET_LEFTNAV_DOCUMENT_FROM_EC = "GET_LEFTNAV_DOCUMENT_FROM_EC";
    public static final String GET_BRAND_LINKS = "GET_BRAND_LINKS";
    public static final String GET_BREADCRUMBS = "GET_BREADCRUMBS";
    public static final String GET_CATEGORY_DOCUMENTS_FROM_ES = "GET_CATEGORY_DOCUMENTS_FROM_ES";
    public static final String GET_CATEGORY_DOCUMENTS_FROM_EC = "GET_CATEGORY_DOCUMENTS_FROM_EC";
    public static final String GET_CATEGORY_DOCUMENT_FROM_ES = "GET_CATEGORY_DOCUMENT_FROM_ES";
    public static final String GET_BRAND_LINK_CATEGORY_DOCUMENTS = "GET_BRAND_LINK_CATEGORY_DOCUMENTS";
    public static final String GET_CATEGORY_DOCUMENT_FROM_EC = "GET_CATEGORY_DOCUMENT_FROM_EC";
    public static final String GET_INITIAL_MOBILE_SILOS = "GET_INITIAL_MOBILE_SILOS";
    public static final String GET_LEFT_NAV = "GET_LEFT_NAV";
    public static final String GET_HYBRID_LEFT_NAV = "GET_HYBRID_LEFT_NAV";
    public static final String GET_PLP_CATEGORY = "GET_PLP_CATEGORY";
    public static final String GET_SILOS = "GET_SILOS";
    public static final String MDC_CONTEXT_ID = "CS.contextId";
    public static final String MDC_DESTINATION_PROPERTY = "CS.NMODEST";
    public static final String MDC_ORIGIN_TIMESTAMP = "CS.originTimestamp";
    public static final String MDC_MESSAGE_ID = "CS.messageId";
    public static final String MDC_MESSAGE_TYPE = "CS.messageType";
    public static final String MDC_SOURCE_PROPERTY = "CS.NMOSRC";
    public static final String MDC_RESOURCE_PROPERTY = "CS.NMORESOURCE";
    public static final String PARSE_LEFTNAV_DOCUMENT = "PARSE_LEFTNAV_DOCUMENT";
    public static final String PARSE_CATEGORY_DOCUMENT = "PARSE_CATEGORY_DOCUMENT";
    public static final String PUBLISH_CATEGORY_MESSAGE_EVENT = "PUBLISH_CATEGORY_MESSAGE_EVENT";
    public static final String EVENT_MESSAGE = "";
    public static final String REPOSITORY = "REPOSITORY";
    public static final String SAVE_CATEGORY_DOCUMENT_TO_ES = "SAVE_CATEGORY_DOCUMENT_TO_ES";
    public static final String SAVE_CATEGORY_DOCUMENT_TO_EC = "SAVE_CATEGORY_DOCUMENT_TO_EC";
    public static final String SEPARATOR = "_";
    public static final String SOURCE_LEFT_NAV = "leftNav";
    public static final String SOURCE_TOP_NAV = "topNav";
    public static final String US_COUNTRY_CODE = "US";
    public static final String DESKTOP_NAV = "desktopNav";
    public static final String MOBILE_NAV = "mobileNav";
    public static final String MOBILE = "mobile";
    public static final String MOBILE_NAV_INITIAL = "mobileNavInitial";
    public static final String BRAND_LINKS = "brandLinks";
    public static final String GET_COMMON_NAV_RESPONSE = "GET_COMMON_NAV_RESPONSE";
    
    public static final String TEST_CATEGORY_GROUP = "B";
    

    public static final Marker CONTENT_SYNC_LOG_MAKER = MarkerFactory.getMarker("ContentSyncLogMarker");
    public static final Marker NO_MDC_LOG_MARKER = MarkerFactory.getMarker("NoMDCLogMarker");

    private Constants() {
    }
}