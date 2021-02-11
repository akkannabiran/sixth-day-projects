#!/usr/bin/env bash

# This script is called by dev tools to create category and leftNav mapping in elasticsearch.

while ! timeout 1 echo > /dev/tcp/elasticsearch/9200; do sleep 5; echo 'Waiting for elasticsearch service to start-up...'; done;

curl -XPUT 'http://elasticsearch:9200/leftnav_index' --upload-file ./navigation-batch-service/elasticsearch/config/left_nav_elasticsearch_mapping.json --retry 2;