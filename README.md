# Time Logger Microservices 
[![Build Status](https://travis-ci.org/pdereg/time-logger-microservices.svg?branch=master)](https://travis-ci.org/pdereg/time-logger-microservices)

This is a sample application backend I wrote in order to practice doing the microservice approach with [Spring](https://spring.io/) and [Netflix](https://cloud.spring.io/spring-cloud-netflix/)'s open-source clients. Whether such an architecture is necessary for a small application like this is a topic for another discussion :-)

## Overview
There are 3 web services, 1 discovery server and 1 API gateway (edge server). 

* Web services expose REST interfaces for the resources they manage. Each service has its own database (MongoDB in this case).
* Discovery server is Netflix's [Eureka](https://github.com/Netflix/eureka) service and is responsible for providing addresses of the registered web services. 
* API gateway is Netflix's [Zuul](https://github.com/Netflix/zuul) service, which provides access from the external world into the system. It is also responsible for issuing authentication tokens.

Authentication is done using standard JSON Web Tokens, which each web service can verify on their own.

#### account-service
Account service manages users. It exposes a REST interface for performing CRUD operations on user accounts. Only administrators can create new users. Each user can view, update and delete only their own account.

#### activity-service
Activity service exposes a REST interface for performing CRUD operations on users' activities. Each user can define their own set of activities for which they can log time.

#### log-service
Log service allows users to log time spent on doing certain activities.

## Build
Application requires Java 1.8 and MongoDB to be present on the host machine. 

Web services can be started either manually or by using [docker-compose](https://docs.docker.com/compose/overview/). In the latter case, everything can be set up and started automatically by running the `run.sh` script:

```bash
git clone https://github.com/pdereg/time-logger-microservices.git && cd time-logger-microservices && ./run.sh
```