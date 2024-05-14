FROM openjdk:17

COPY ./build/libs/project-0.0.1-SNAPSHOT.jar ./plop.jar

ENV	PROFILE local

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${PROFILE}", "plop.jar"]
