#!/bin/sh

# This script is called by dev tools to setup your local vault secrets.

VAULT_DEV_TOKEN=eafaa220-e9e5-f2f8-5372-901acaafffb5

vault auth ${VAULT_DEV_TOKEN}

# --- [Profile: default] Secrets ---

vault write secret/category-service @${CATEGORY_SERVICE_VAULT_CONFIG_DIR}/default.yml
vault read secret/category-service