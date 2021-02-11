#!/usr/bin/env sh

./gradlew sonarqube -Dsonar.host.url=http://sonar-nonprod.nmgcloudapps.com -Dsonar.projectKey=category-service-pr -Dsonar.projectName=category-service-pr

errorCount=$(curl -sL http://sonar-nonprod.nmgcloudapps.com/api/qualitygates/project_status?projectKey=category-service-pr 2>&1 | grep -o '"status":"[^"]*' | head -1 | grep -c "ERROR")

if [ $errorCount == 1 ]; then  ./gradlew sonarFailure ;  fi

echo "Check report at "http://sonar-nonprod.nmgcloudapps.com/overview?id=9969