FROM openjdk:11-jre-slim

RUN apt-get update && apt-get install -y tzdata
ENV TZ=Asia/Seoul

ARG JAR_FILE=build/libs/Yonsei-Golf-Server-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]