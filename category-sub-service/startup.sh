#!/bin/sh
#Get vault token from s3
token=$(aws s3 cp ${VAULT_ACCESS_TOKEN_PATH} -)

if [[ -z "${token}" ]]; then
    echo "Failed to fetch vault token from s3 for ${ENV_NAME}. Using local-dev vault token"
    java -jar -Dspring.profiles.active=${ENV_NAME} /app.jar
else
    echo "Using vault token from s3 bucket (${VAULT_ACCESS_TOKEN_PATH}) for ${ENV_NAME}"
    java -jar -Xms1536m -Xmx1536m -verbose:gc -Dspring.profiles.active=${ENV_NAME},${ENV_VERSION_NAME} -Dspring.cloud.vault.token=${token} /app.jar
fi
