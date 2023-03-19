FROM eclipse-temurin:17-jre
ADD ./target/universal/stage/ /opt/app/
ENV SERVICE_HOST es-gateway
ENTRYPOINT ["/opt/app/bin/es-gateway"]
