#!/usr/bin/env bash

gradle clean buildDocker -p api-gateway
gradle clean buildDocker -p discovery-server
gradle clean buildDocker -p account-service
gradle clean buildDocker -p activity-service
gradle clean buildDocker -p log-service

docker-compose up