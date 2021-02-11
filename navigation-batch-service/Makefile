BINARY=build/libs/navigation-batch-service-0.0.1-SNAPSHOT.jar
VERSION="0.4.1"
BUILD_TIME=`date +%FT%T%z`
REPO_URL="168860074409.dkr.ecr.us-west-2.amazonaws.com"
REPO="service-browse"
LDFLAGS=-ldflags "-X main.Version=${VERSION} -X main.BuildTime=${BUILD_TIME}"
build:
	./gradlew build
push:
	# Tag the latest version with the ecr hostname/repo.
	docker tag ${REPO}:${VERSION} ${REPO_URL}/${REPO}:${VERSION}
	docker tag ${REPO}:${VERSION} ${REPO_URL}/${REPO}:latest
	docker push ${REPO_URL}/${REPO}:${VERSION}
	docker push ${REPO_URL}/${REPO}:latest
docker:
	docker build -t ${REPO}:${VERSION} .
deploy:
	echo "Deploy"
