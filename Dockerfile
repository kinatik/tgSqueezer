FROM openjdk:17

WORKDIR /app

COPY target/tgSqueezer-0.0.4-SNAPSHOT.jar /app/tgSqueezer.jar

ENTRYPOINT ["java", "-jar", "/app/tgSqueezer.jar"]
