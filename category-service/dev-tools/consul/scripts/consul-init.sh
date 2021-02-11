#!/bin/sh

# This script is called by dev tools to setup your local consul environment.

consul kv put config/category-service/data @${CATEGORY_SERVICE_CONSUL_CONFIG_DIR}/default.yml;
consul kv put config/category-service,docker/data @${CATEGORY_SERVICE_CONSUL_CONFIG_DIR}/docker.yml;
