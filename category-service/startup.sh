#!/bin/sh
#Get vault token from s3
token=$(aws s3 cp ${VAULT_ACCESS_TOKEN_PATH} -)

JAVA_OPTS="${JAVA_OPTS} -Xms1536m -Xmx1536m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly -verbose:gc"; export JAVA_OPTS

if [[ -z "${token}" ]]; then
    echo "Failed to fetch vault token from s3 for ${ENV_NAME}..."
else
    echo "Using vault token from s3 bucket (${VAULT_ACCESS_TOKEN_PATH}) for ${ENV_NAME}"
    java ${JAVA_OPTS} -Dspring.profiles.active=${ENV_NAME},${ENV_VERSION_NAME} -Dspring.cloud.vault.token=${token} -jar /app.jar
fi
