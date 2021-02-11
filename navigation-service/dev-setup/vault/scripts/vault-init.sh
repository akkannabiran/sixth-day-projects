#!/bin/sh

# This script is called by dev tools to setup your local vault secrets.

while !(vault version > /dev/null 2>&1); do sleep 2; echo 'Waiting for vault service to start-up...'; done;

VAULT_DEV_TOKEN=eafaa220-e9e5-f2f8-5372-901acaafffb5

vault login token=${VAULT_DEV_TOKEN}

# --- [Profile: default] Secrets ---

vault write secret/navigation-service @${NAVIGATION_SERVICE_VAULT_CONFIG_DIR}/default.yml
vault read secret/navigation-service
