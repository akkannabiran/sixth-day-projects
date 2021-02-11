#!/usr/bin/env sh

errorCount=$(curl -sL $1/api/qualitygates/project_status?projectKey=$2  2>&1 | grep -o '"status":"[^"]*' | head -1 | grep -c "ERROR")

if [ $errorCount == 1 ]
 then
    echo "Click to see the report $1/overview?id=$2"
    ./gradlew sonarFailure
 else
    echo "Sonar Quality - SUCCESS"
fi
