#!/usr/bin/env sh

./gradlew sonarqube -Dsonar.host.url=http://sonar-nonprod.sixthdaycloudapps.com -Dsonar.projectKey=category-sub-service-pr -Dsonar.projectName=category-sub-service-pr

errorCount=$(curl -sL http://sonar-nonprod.sixthdaycloudapps.com/api/qualitygates/project_status?projectKey=category-sub-service-pr  2>&1 | grep -o '"status":"[^"]*' | head -1 | grep -c "ERROR")

if [ $errorCount == 1 ]; then  ./gradlew sonarFailure ;  fi

echo "Check report at http://sonar-nonprod.sixthdaycloudapps.com/overview?id=9977"
