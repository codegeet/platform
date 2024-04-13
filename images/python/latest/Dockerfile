FROM python:3-slim

MAINTAINER Vladimir Prudnikov "codegeet@gmail.com"

RUN set -xe && \
    apt-get update && \
    apt-get install time -y --no-install-recommends && \
    apt-get install openjdk-17-jdk -y --no-install-recommends && \
    rm -rf /var/lib/apt/lists/* /tmp/*

RUN groupadd codegeet && \
    useradd -m -d /home/codegeet -g codegeet -s /bin/bash codegeet

ADD https://github.com/codegeet/platform/releases/download/0.1.0-SNAPSHOT/coderunner.jar /home/codegeet

RUN chown codegeet:codegeet /home/codegeet/coderunner.jar && \
    chmod +x /home/codegeet/coderunner.jar

USER codegeet
WORKDIR /home/codegeet

ENTRYPOINT ["java", "-jar", "coderunner.jar"]
