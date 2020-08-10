FROM openjdk:11-jre-slim
COPY target/opcua2kafka-2.0.0.jar /opt/opcua2kafka/opcua2kafka.jar
WORKDIR /opt/opcua2kafka
ENTRYPOINT ["java","-jar", "/opt/opcua2kafka/opcua2kafka.jar"]
