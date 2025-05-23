FROM maven:3.5-jdk-8 as builder

#Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

#build a release artifact
RUN mvn package -DskipTests

#Run the web service on container startup.
CMD ["java","-jar","/app/target/p_pao-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]