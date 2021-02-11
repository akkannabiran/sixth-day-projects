#!/bin/sh

# This script is called by dev tools to setup your local consul environment.

consul kv put config/ctp-svc/data @${CTP_SVC_CONSUL_CONFIG_DIR}/default.yml;
consul kv put config/ctp-svc,docker/data @${CTP_SVC_CONSUL_CONFIG_DIR}/docker.yml;
