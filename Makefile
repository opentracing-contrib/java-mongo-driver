APP_NAME = opentracing-java-mongo-driver
VERSION ?= SNAPSHOT

default: help

.PHONY: build
build: ## Builds a release of broker commons
    mvn clean package -Dversion=$(VERSION)

.PHONY: deploy
deploy: build
    mvn deploy -Dversion=$(VERSION)

.PHONY: help
help: ## Help
    @echo "Please use 'make <target>' where <target> is ..."
    @grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
