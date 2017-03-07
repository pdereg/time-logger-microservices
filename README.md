# Time Logger Microservices (UNDER CONSTRUCTION)
[![Build Status](https://travis-ci.org/pdereg/time-logger-microservices.svg?branch=master)](https://travis-ci.org/pdereg/time-logger-microservices)

This is a sample application backend I wrote in order to practice doing the microservice approach with [Spring](https://spring.io/) and [Netflix](https://cloud.spring.io/spring-cloud-netflix/)'s open-source clients. Whether such an architecture is necessary for a small application like this is a topic for another discussion :-)

## Overview
There are 3 web services, 1 discovery server and 1 API gateway (edge server). Web services only communicate with each other via a JSON REST interface and each has their own database (MongoDB in that case). Discovery server is Netflix's [Eureka](https://github.com/Netflix/eureka) service and is responsible for providing addresses of the registered web services. API gateway is Netflix's [Zuul](https://github.com/Netflix/zuul) service, which provides access from the external world into the system.

Authentication is done using standard [JSON Web Tokens](https://jwt.io/), which each web service can verify on their own.
