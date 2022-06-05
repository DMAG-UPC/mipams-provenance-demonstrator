FROM adoptopenjdk/openjdk11:alpine

RUN addgroup -S mipams && adduser -S mipams -G mipams

WORKDIR /app
RUN mkdir /app/assets
RUN mkdir /app/credentials
RUN chown -R mipams:mipams /app/assets
RUN chown -R mipams:mipams /app/credentials

RUN apk upgrade --update-cache --available && \
    apk add openssl && \
    rm -rf /var/cache/apk/*

COPY ./generateCredentials.sh /app/credentials/generateCredentials.sh
RUN chown -R mipams:mipams /app/credentials
RUN chmod +x /app/credentials/generateCredentials.sh

USER mipams:mipams
VOLUME /tmp
ARG JAR_FILE=target/demo-1.0.jar
ADD ${JAR_FILE} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]