#!/usr/bin/env bash
: 'logstash -f /etl/logstash/logstash-file-to-es.conf < ./etl/data/store.txt;
logstash -f /etl/logstash/logstash-file-to-es.conf < ./etl/data/storeSkuInventory.txt;
logstash-plugin install logstash-filter-aggregate;
logstash -w 1 -f /etl/logstash/logstash-store-events-file-to-es.conf < ./etl/data/events.txt;'

