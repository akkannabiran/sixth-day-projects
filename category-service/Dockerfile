FROM openjdk:8u191-jre-alpine3.8

ENV ENV_NAME default

RUN apk add --update python3 && \
    python3 -m ensurepip --upgrade && \
    pip3 install awscli && \
    rm -rf /var/cache/apk/*

COPY ./build/libs/category-service-0.0.1-SNAPSHOT.jar /app.jar
COPY ./startup.sh /startup.sh

RUN chmod +x /startup.sh

ENTRYPOINT /startup.sh