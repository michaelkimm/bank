FROM openjdk:11
ARG JAR_FILE=./build/libs/bank.jar
COPY ${JAR_FILE} bank.jar
ENTRYPOINT ["java","-jar","/bank.jar"]