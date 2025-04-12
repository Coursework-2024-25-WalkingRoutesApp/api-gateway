FROM amazoncorretto:23-alpine-jdk

WORKDIR /api-gateway
ADD build/libs/api-gateway-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
