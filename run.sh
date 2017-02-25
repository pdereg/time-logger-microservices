gradle clean buildDocker -p api-gateway
gradle clean buildDocker -p discovery-server
gradle clean buildDocker -p account-service
docker-compose up
