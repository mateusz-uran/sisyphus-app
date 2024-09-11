# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:17
ARG PROFILE=pipeline
ARG APP_VERSION=1.0.0
ARG PORT=8080
ARG CORS=http://localhost:9090
ARG SCRAPE=http://127.0.0.1:5858/scrape

WORKDIR /app
COPY --from=build /build/target/sisyphus-api-*.jar /app/

EXPOSE 8080

ENV ACTIVE_PROFILE=${PROFILE}
ENV JAR_VERSION=${APP_VERSION}
ENV SERVER_PORT=${PORT}
ENV CORS_ORIGINS=${CORS}
ENV SCRAPER_API=${SCRAPE}

CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} -Dserver.port=${SERVER_PORT} -Dapplication.cors.origins=${CORS_ORIGINS} -Dscraper.api=${SCRAPER_API} sisyphus-api-${JAR_VERSION}.jar