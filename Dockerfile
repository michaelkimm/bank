FROM openjdk:11
ARG JAR_FILE=./build/libs/bank.jar
ARG APM_JAR_FILE=elastic-apm-agent-1.36.1.jar
COPY ${JAR_FILE} bank.jar
COPY ${APM_JAR_FILE} elastic-apm-agent.jar
#ENTRYPOINT ["java","-jar","/bank.jar"]
ENTRYPOINT [ "java",
             "-javaagent:C:\Github\bank\elastic-apm-agent-1.36.1.jar",
             "-Delastic.apm.service_name=bank",
             "-Delastic.apm.server_url=http://localhost:8200",
             "-Delastic.apm.environment=my-environment",
             "-Delastic.apm.application_packages=com.ms",
             "-Delastic.apm.secret_token=",
             "-jar bank.jar"
]