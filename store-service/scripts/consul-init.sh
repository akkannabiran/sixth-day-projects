#!/bin/sh

# This script is called by dev tools to setup your local consul environment.

consul kv put config/store-service/data @${STORE_SERVICE_CONSUL_CONFIG_DIR}/default.yml;
consul kv put config/store-service,docker/data @${STORE_SERVICE_CONSUL_CONFIG_DIR}/docker.yml;