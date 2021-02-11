#!/bin/sh
#Get vault token from s3
# The following VAULT_ACCESS_TOKEN_PATH check added to handle if the environment has s3 path
if [[ -z ${VAULT_ACCESS_TOKEN_PATH} ]]
then
# TODO: Remove this once platform has rolled a new stack in prep and prod :)
	if [ ${ENV_NAME} == "prep" ]
	then
		VAULT_ACCESS_TOKEN_PATH="s3://nm-vault-preprod/sixthday-ui/access_token.txt"
	elif [ ${ENV_NAME} == "prod" ]
	then
	    VAULT_ACCESS_TOKEN_PATH="s3://sixthday-vault-prod/product/access_token.txt"
	else
	    VAULT_ACCESS_TOKEN_PATH="s3://nm-vault/nm/${ENV_NAME}/access_token.txt"
	fi
fi

token=$(aws s3 cp ${VAULT_ACCESS_TOKEN_PATH} -)

if [[ -z "${token}" ]]; then
	echo "Failed to fetch vault token from s3 for ${ENV_NAME}..."
else
	echo "Using vault token from s3 bucket (${VAULT_ACCESS_TOKEN_PATH}) for ${ENV_NAME}"
	java -Xms1536m -Xmx1536m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly -verbose:gc -Dspring.profiles.active=${ENV_NAME},${ENV_VERSION_NAME} -Dspring.cloud.vault.token=${token} -jar /app.jar
fi
