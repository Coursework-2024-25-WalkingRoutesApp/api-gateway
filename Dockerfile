FROM amazoncorretto:23-alpine-jdk

WORKDIR /api-gateway
ADD build/libs/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar

CMD ["java", "-jar", "api-gateway.jar"]
