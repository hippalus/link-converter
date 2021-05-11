FROM gcr.io/distroless/java:11
LABEL maintainer=hippalus

ARG JAR=target/*.jar

COPY ${JAR} link-converter.jar

ENTRYPOINT ["java","-jar","/link-converter.jar"]