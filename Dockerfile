FROM openjdk:11
ARG JAR_FILE=./build/libs/bank.jar
ARG APM_JAR_FILE=elastic-apm-agent-1.34.0.jar
COPY ${JAR_FILE} bank.jar
COPY ${APM_JAR_FILE} elastic-apm-agent.jar
ENTRYPOINT [ "java", "-javaagent:./elastic-apm-agent.jar", "-Delastic.apm.service_name=bank", "-Delastic.apm.server_url=http://110.165.19.155:8200", "-Delastic.apm.application_packages=com.ms.*", "-Delastic.apm.transaction_sample_rate=1", "-Delastic.apm.enable_log_correlation=true", "-Delastic.apm.span_frames_min_duration=1ms", "-Delastic.apm.span_min_duration=0ms", "-Delastic.apm.trace_methods_duration_threshold=1ms", "-Delastic.apm.trace_methods=com.ms.*", "-jar", "/bank.jar"]


#-javaagent:./elastic-apm-agent-1.34.0.jar -Delastic.apm.service_name=bank -Delastic.apm.server_url=http://localhost:8200 -Delastic.apm.application_packages=com.ms.* -Delastic.apm.transaction_sample_rate=1 -Delastic.apm.enable_log_correlation=true -Delastic.apm.span_frames_min_duration=1ms -Delastic.apm.span_min_duration=0ms -Delastic.apm.trace_methods_duration_threshold=1ms -Delastic.apm.trace_methods=com.ms.*