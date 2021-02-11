#!/bin/sh
#Get vault token from s3
token=$(aws s3 cp ${VAULT_ACCESS_TOKEN_PATH} -)

JAVA_OPTS="${JAVA_OPTS} -Xms1536m -Xmx1536m"; export JAVA_OPTS
JAVA_OPTS="${JAVA_OPTS} -Xdebug -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly -verbose:gc"; export JAVA_OPTS
echo "Starting service with java options: ${JAVA_OPTS}"

if [[ -z "${token}" ]]; then
	echo "Failed to fetch vault token from s3 for ${ENV_NAME}..."
	VAULT_ACCESS_TOKEN_PATH="s3://nm-vault/nm/${ENV_NAME}/access_token.txt"
	token=$(aws s3 cp ${VAULT_ACCESS_TOKEN_PATH} -)
	java -Dspring.cloud.vault.token=${token} -Dspring.profiles.active=${ENV_NAME} -jar /app.jar
else
	echo "Using vault token from s3 bucket (${VAULT_ACCESS_TOKEN_PATH}) for ${ENV_NAME}"
	java ${JAVA_OPTS} -Dspring.profiles.active=${ENV_NAME},${ENV_VERSION_NAME} -Dspring.cloud.vault.token=${token} -jar /app.jar
fi
