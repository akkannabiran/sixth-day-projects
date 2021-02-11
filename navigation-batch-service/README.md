# Prerequisites
* GIT 1.8.3.1 (or newer?)
* JDK 1.8
* Elastic Search
* Consul
* Vault

# Running Navigation-Batch-Service

1. Use the dev-tools git repository: `sh path/to/dev-tools/service.sh start navigation-batch-service`

## Application commands

Note: This project uses the gradle wrapper.

To get a list of the relevant commands, run:

    ./gradlew tasks

The most important sections are listed in the output under "Application tasks" and "Build tasks".

To build, execute:

    ./gradlew clean build

To run the application with the local (development) profile, execute:

    ./gradlew bootRun

To run the application with another profile, execute:

    # <profile_name> is an available profile defined in src/main/resources/bootstrap.yml
    ./gradlew run -Dspring.profiles.active=<profile_name>

Remember to run tests before pushing your code changes

    ./gradlew test consumerContractTest

To see more tasks, execute:

    ./gradlew task

## Updating Java dependencies

The [nebula.dependency-lock plugin](https://github.com/nebula-plugins/gradle-dependency-lock-plugin) is used to lock the transitive dependency graph.

If you make a change to any dependencies in the Gradle scripts, you need to generate a new lock file in order for your changes to take effect.

    ./gradlew generateGlobalLock saveGlobalLock

The updated `global.lock` file should be included in the same commit that includes the Gradle script changes.

### Query dynamo db using shell application (docker/local)

1. Load the shell application on browser by visiting 
    `http://localhost:8000/shell`
2. Execute the below javascript command to fetch first 100 items from dynamo db table
    ```javascript
    // Queries for fetching first 100 items from the "docker_nm_designer_by_id" table
    var params = {
        TableName: 'docker_nm_designer_by_id',
        Limit: 100
    };
    dynamodb.scan(params, function(err, data) {
        if (err) ppJson(err); // an error occurred
        else ppJson(data); // successful response
    });
     ```
3. Execute the following javascript command to fetch item from dynamo db table by key (e.g. designer_id == "Designer1")
   ```javascript
   var params = {
       TableName: 'docker_nm_designer_by_id',
       Key: { designer_id: "Designer1"}
   };
   docClient.get(params, function(err, data) {
       if (err) ppJson(err); // an error occurred
       else ppJson(data); // successful response
   });
   ```