# KSQL UDFs
More details https://docs.ksqldb.io/en/latest/concepts/functions/

## Usage
* Clone this repository
* Get into ksql-udfs director
* Run the command ``` gradle clean shadowJar```
* This command will generate the JAR file under ksql-udfs/extensions folder
* Copy the JAR into KSQL server (any accessible directory)
* Set ```ksql.extension.dir=<directory-path>/``` in ksql-server.properties and restart KSQL server
* You're all set to use the function TOSTR in KSQL streams

## Troubleshoot
* Use ```SHOW FUNCTIONS``` and ```DESCRIBE FUNCTION TOSTR```