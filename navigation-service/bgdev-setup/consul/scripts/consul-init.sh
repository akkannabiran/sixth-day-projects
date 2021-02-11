#!/bin/sh

# This script is called by dev tools to setup your local consul environment.

while !(consul info > /dev/null 2>&1); do sleep 2; echo 'Waiting for consul service to start-up...'; done;

consul kv put config/navigation-service/data @${NAVIGATION_SERVICE_CONSUL_CONFIG_DIR}/default.yml;
consul kv put config/navigation-service,docker/data @${NAVIGATION_SERVICE_CONSUL_CONFIG_DIR}/docker.yml;
