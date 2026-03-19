FROM registry.access.redhat.com/ubi9/openjdk-21:1.23

ENV LANGUAGE='en_US:en'

COPY --chown=185 docker-entrypoint.sh /deployments/docker-entrypoint.sh
RUN chmod +x /deployments/docker-entrypoint.sh

COPY --chown=185 target/expenses-1.0.0-SNAPSHOT.jar /deployments/expenses.jar

EXPOSE 8080
USER 185

ENTRYPOINT ["/deployments/docker-entrypoint.sh", "java", "-jar", "/deployments/expenses.jar"]

