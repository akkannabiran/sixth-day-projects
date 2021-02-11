#!/usr/bin/env bash

# This script is called by dev tools to load category data into elasticsearch.

while ! timeout 1 echo > /dev/tcp/elasticsearch/9200; do sleep 5; echo 'Waiting for elasticsearch service to start-up...'; done;

logstash -f ./category-service/dev-tools/elasticsearch/config/logstash-category-file-to-es.conf < ./category-service/dev-tools/elasticsearch/data/category.txt;