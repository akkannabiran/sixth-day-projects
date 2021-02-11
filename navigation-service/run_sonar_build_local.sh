#!/usr/bin/env sh

./gradlew clean test sonarqube -Dsonar.host.url=http://sonar-nonprod.sixthdaycloudapps.com -Dsonar.projectKey=navigation-service-PR -Dsonar.projectName=navigation-service-pr

errorCount=$(curl -sL http://sonar-nonprod.sixthdaycloudapps.com/api/qualitygates/project_status?projectKey=navigation-service-PR 2>&1 | grep -o '"status":"[^"]*' | head -1 | grep -c "ERROR")

if [ $errorCount == 1 ]; then  ./gradlew sonarFailure ;  fi

echo "Check report at "http://sonar-nonprod.sixthdaycloudapps.com/overview?id=729