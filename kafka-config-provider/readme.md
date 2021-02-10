# About kafka-config-provider
The purpose of this repository is, to externalize/manage and read the Kafka meta-data and configuration(s).
We are extending "ConfigProvider" interface from Kafka and providing our custom implementation.

# Getting Started
Use any Java IDE and import the project as maven

# Development Notes
Kafka "ConfigProvider" uses a Java pattern called "ServiceLoader" to load your custom implementation. 
Reference 
* https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
* /main/resources/META-INF/services

# Build
We would need a custom builder (JAR) to archive required custom implementation classes/JARs and dependency JARs in
right location within the JAR. 

# Deployment
Use maven "package" task to create a JAR and deploy the jAR as "adding new plugin" in Kafka world.
Reference 
* https://docs.confluent.io/current/connect/security.html#externalizing-secrets
* https://docs.confluent.io/current/connect/userguide.html#configprovider-interface

# Still Questions?
Reach out to Mohaideen.ShandhuMohammed@carefirst.com
