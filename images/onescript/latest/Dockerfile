FROM debian:latest

RUN set -xe && \
    apt-get update && \
    apt-get install time -y --no-install-recommends && \
    apt-get install openjdk-17-jdk -y --no-install-recommends

RUN set -xe && \
    apt-get update && \
    apt-get install -y wget && \
    apt-get -y install locales

RUN wget https://github.com/EvilBeaver/OneScript/releases/download/v1.9.0/onescript-engine_1.9.0_all.deb

RUN apt-get install -y ./onescript-engine_1.9.0_all.deb

RUN apt-get clean &&  \
    rm -rf /var/lib/apt/lists/* /tmp/*

RUN sed -i '/ru_RU.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen

ENV LANG=ru_RU.UTF-8
ENV LANGUAGE=ru_RU:ru
ENV LC_ALL=ru_RU.UTF-8

RUN groupadd codegeet && \
    useradd -m -d /home/codegeet -g codegeet -s /bin/bash codegeet

ADD https://github.com/codegeet/platform/releases/download/0.1.0-SNAPSHOT/coderunner.jar /home/codegeet

RUN chown codegeet:codegeet /home/codegeet/coderunner.jar && \
    chmod +x /home/codegeet/coderunner.jar

USER codegeet
WORKDIR /home/codegeet

ENTRYPOINT ["java", "-jar", "coderunner.jar"]