FROM openjdk:17

WORKDIR /app

COPY target/pastebin-mine-rest-mongo-0.0.1-SNAPSHOT.jar ./pastebin-mine-rest-mongo.jar

EXPOSE 8080

CMD ["java", "-jar", "pastebin-mine-rest-mongo.jar"]

# Run:
#   'docker build -t pastebin-mine-rest-mongo-image .'
#   'docker-compose build'
#   'docker-compose up'