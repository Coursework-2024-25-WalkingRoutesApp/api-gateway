FROM amazoncorretto:23-alpine-jdk

WORKDIR /api-gateway
ARG VERSION
ADD build/libs/api-gateway-${VERSION}.jar api-gateway.jar

CMD ["java", "-jar", "api-gateway.jar"]
