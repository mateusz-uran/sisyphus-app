# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:17
ARG PROFILE=dev
ARG APP_VERSION=1.0.0

WORKDIR /app
COPY --from=build /build/target/sisyphus-app-*.jar /app/

EXPOSE 8080

ENV DB_URL mongodb://mateuszuran:password@mongo_db:27017/sisyphus-db?authSource=admin
ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}

CMD java -jar -Dspring.profiles.actives=${ACTIVE_PROFILE} -Dspring.data.mongodb.uri=${DB_URL} sisyphus-app-${JAR_VERSION}.jar