## Store Service

### Setting up your dev environment

1.  Install docker
    * [Mac users](https://docs.docker.com/docker-for-mac/)
    * [Windows users](https://docs.docker.com/engine/installation/windows/)
    * [Linux users](https://docs.docker.com/engine/installation/linux/ubuntulinux/)

2.  Use this [link](https://docs.docker.com/compose/install/) to install docker compose

3.  Export JDBC connection string, user and password environment variables

    `export DB_CONNECTION_STRING="<Oracle database connection url goes here>"`

    `export DB_USER="ecom_inq"`

    `export DB_PASSWORD="<ecom_inq password>"`

4. Getting data from ATG
	 docker-compose -f ./etl/docker-pull-data-from-atg.yml up

    > **TIP:**
    * Add the export statements to your profile (.bashrc for example) so that you don't have to export each time.
    * For ecom_inq password refer [this APPMAN wiki page.](https://wiki.mysixthday.com/display/APPMAN/Password+for+ecom_inq+account)

### Running the application

1. To run the application in a docker container, in the dev-tools git repository run; 
    ```
    sh <path/to/dev-tools>/service.sh start store-service
    ```

2. To start the application with gradlew. 

    First start the application dependencies by running;
    
    ```
    sh <path/to/dev-tools>/service.sh start_dependencies store-service
    ```
    
    then run;

    ```
    ./gradlew -Dspring.profiles.active=<ENVIRONMENT> clean run
    ```

    Alternatively, run using the "local" profile with:

    `./gradlew clean bootRun`

>    **NOTE:** Where <ENVIRONMENT> is either `local` or `docker` or it's the ENV_VERSION_NAME configured by platform
e.g dev-int-2

### Running Tests

1. To run unit tests run

    `./gradlew clean test`

2. To run service tests run

    `./gradlew clean serviceTest`

3. To run integration tests run

    `./gradlew clean integrationTest`
4. To run provide contract test. Reach out to your TL for AritifactoryKey

	`./graldew providerContractTest -PpactArtifactoryApiKey=<<AritifactoryKey>>`

>    **NOTE:** Make sure store service is running first.

### Running dependency check

1. To run dependency check

    `./gradlew dependencyCheck`

### Query dynamo db using shell application (docker/local)

1. Load the shell application on browser by visiting 
    `http://localhost:8000/shell`
1. Execute the below javascript command to fetch data from dynamo db
    ```javascript
    // Queries for all items in the ImageTag table for images with the tag 'Database'
    var params = {
        TableName: 'docker_sixthday_store_inventory_by_sku',
        KeyConditionExpression: 'sku_id = :db',
        ExpressionAttributeValues: {
            ':db' : 'sku144430292'
        }
    };
    console.log("Querying the ImageTag table for all images with the tag 'Database'");
    docClient.query(params).eachPage(function(err, data) {
        if (err) ppJson(err); // an error occurred
        else if (data) ppJson(data); // successful response
    });
     ```
 

