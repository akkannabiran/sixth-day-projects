## Category Service

### Setting up your dev environment

1.  Install docker
    * [Mac users](https://docs.docker.com/docker-for-mac/)
    * [Windows users](https://docs.docker.com/engine/installation/windows/)
    * [Linux users](https://docs.docker.com/engine/installation/linux/ubuntulinux/)

2.  Use this [link](https://docs.docker.com/compose/install/) to install docker compose

3.  Clone the application from stash, run:

    ```
    git clone https://stash.sixthday.com/scm/faw/category-service.git
    ```
4.  Create an elastic search data directory and export it as an environment variable
    `mkdir ~/data/elasticsearch`

    `export ESDATA_DIR="~/data/elasticsearch"`

    > **NOTE:** Make sure you add project root (or parent(s)) as mountable volume in docker Preferences -> File Sharing,
                in case if your project is not in your home(~) directory.

5.  Export JDBC connection string, user and password environment variables

    `export DB_CONNECTION_STRING="<Oracle database connection url goes here>"`

    `export DB_USER="ecom_inq"`

    `export DB_PASSWORD="<ecom_inq password>"`

6. Getting data from ATG
	 docker-compose -f ./etl/docker-pull-data-from-atg.yml up

    > **TIP:**
    * Add the export statements to your profile (.bashrc for example) so that you don't have to export each time.
    * For ecom_inq password refer [this APPMAN wiki page.](https://wiki.sixthday.com/display/APPMAN/Password+for+ecom_inq+account)

### Running the application

1. Use the dev-tools git repository: `sh path/to/dev-tools/service.sh start category-service`

### Running Tests

1. To run unit tests run

    `./gradlew clean test`

2. To run service tests run

    `./gradlew clean serviceTest`

3. To run integration tests...
    - First start your dependencies (where "dt" is an alias to your dev-tools service command): dt start_dependencies category-service
    - Then run the integrationTest task using the local profile: ./gradlew -Dspring.profiles.active=local clean integrationTest
### Running performance tests
1. To run performance tests, execute below gradle command
    `./gradlew clean jmRun jmReport`
2. Optional: To edit the jmeter scripts, execute below gradle command
    `./gradlew clean jmGui`
### Running dependency check
1. To run dependency check
    `./gradlew dependencyCheck`
### Setup Elastic Search data in local
1. Install npm
Command: npm install amqp-tool -g
2. Verify amptool installed
Command: amqp-tool
3. Export messages from dev-int evironment to sixthday-category-dump(temp queue) 
Refer Continuously export a queue into a file for command in below provided site.
Command: amqp-tool --host dev-int-messaging-sixthday.sixthdaycloudapps.net -u admin -p dev -q sixthday-category-dump --export > category.json
4. Start navigation service with dependencies
Command: dt start_dependencies navigation-service
above command stop the navigation service once it starts the dependencies.
5. Start Navigation service
Command: ./gradlew bootRun
6. Import messages to Queue(sixthday-category) from category.json file
Refer Import all messages to a queue for command in below provided site.

Pease refer below site for more details: https://www.npmjs.com/package/amqp-tool
