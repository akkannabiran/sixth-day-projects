# Kafka Utils
## For developers & contributors
* Clone this repository
```
git clone https://bitbkt.carefirst.com/scm/nex-fon/kakfa-utils.git
or
git clone ssh://git@bitbkt.carefirst.com/nex-fon/kakfa-utils.git
```
* Import this project in developer IDE
* Do maven clean, build and install (this step will install the project into your local maven repository)
* Make any changes and follow above step to update your local maven

## To use this project in your project
* Add maven dependency
```
<dependency>
    <groupId>com.carefirst</groupId>
    <artifactId>kafka-utils</artifactId>
    <version>0.0.1</version>
</dependency>
```
* Add component scan
```
@ComponentScan({"com.carefirst.kafka.consumer", "your project's root package"})
```
* Update your application.yml

Refer the syntax https://bitbkt.carefirst.com/projects/NEX-FON/repos/kakfa-utils/browse/src/main/resources/application.properties?at=refs%2Fheads%2Fdevelop
* Use @KafkaListener to consumer the messages
* Example
```
 @KafkaListener(
            topics = {"${application.kafka.listeners.consent.consumer.topic}"},
            groupId = "${application.kafka.listeners.consent.consumer.properties.group.id}",
            containerFactory = "consent_ContainerFactory")
```
* Dynamic configurations
```
topics, groupId and containerFactory
topics - A topic you need to consume the messages (make sure the ConsumerRecord payload is the same, NO String consumption)
groupId - Unqiue group ID per topic
containerFactory - <listener-key>_ContainerFactory
```
* Throw RecoverableMessageException or UnrecoverableMessageException based on exception you handle on your code.
* Below producers are optional, no value mentioned then the messages will be logged into application log
```
retry-producer
dl-producer
```