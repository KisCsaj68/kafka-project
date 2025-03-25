SHELL := /bin/bash
# defining variables for the make file. Specifying default values if these values are not passed in by the caller
docker_tag ?= latest
components := api
docker_repository ?= kafka

.PHONY: run
run: docker-build create-database up

.PHONY: mvn-build
mvn-build:
	mvn clean install --file common/pom.xml
	for component in $(components); do \
  		mvn clean package --file $$component/pom.xml ; \
  	done

.PHONY: docker-build
docker-build:
	for component in $(components); do \
  		docker buildx build -t $(docker_repository)/$$component:$(docker_tag) . -f $$component/Dockerfile ; \
  	done

.PHONY: build
build: mvn-build docker-build

.PHONY: up
up:
	docker-compose up -d

.PHONY: logs
logs:
	docker-compose logs -f --since 5s

.PHONY: down
down:
	docker-compose down

.PHONY: create-database
create-database:
	docker-compose create postgres 
	docker-compose start postgres
	sleep 5 
	docker exec postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'tasks'" | grep -q 1 || docker exec postgres psql -U postgres -c "CREATE DATABASE tasks"

.PHONY: deletedb
deletedb: down
	rm -rf volumes/postgres

.PHONY: resetdb
resetdb: deletedb create-database