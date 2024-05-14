FROM openjdk:17

COPY ./build/libs/project-0.0.1-SNAPSHOT.jar ./plop.jar

ENTRYPOINT ["java", "-jar", "plop.jar"]
