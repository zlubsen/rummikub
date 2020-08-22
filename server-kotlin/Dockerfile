FROM openjdk:14-alpine

ENV JAR_FILE server-kotlin-1.0-SNAPSHOT-jar-with-dependencies.jar
ENV JAR_HOME /usr/rummikub

EXPOSE 8080

COPY target/$JAR_FILE $JAR_HOME/

WORKDIR $JAR_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar $JAR_FILE"]