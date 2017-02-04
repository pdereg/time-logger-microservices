FROM openjdk:8-jre-alpine

ADD *.jar /app.jar

RUN sh -c 'touch /app.jar'
VOLUME /tmp
EXPOSE 8083
CMD java ${JAVA_OPTS} -jar /app.jar
