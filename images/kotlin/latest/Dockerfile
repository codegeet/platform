FROM openjdk:21-slim

MAINTAINER Vladimir Prudnikov <codegeet@gmail.com>

#<install kotlin>
RUN set -xe && \
    apt-get update && \
    apt-get install unzip -y --no-install-recommends && \
    apt-get install wget -y --no-install-recommends

ENV KOTLIN_VERSION=1.9.22
ENV KOTLIN_HOME=/opt/kotlin

RUN wget https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip -O kotlin-compiler.zip && \
    unzip kotlin-compiler.zip -d /opt && \
    rm kotlin-compiler.zip && \
    mv /opt/kotlinc /opt/kotlin

ENV PATH="${KOTLIN_HOME}/bin:${PATH}"

RUN kotlin -version
# </install kotlin>

RUN apt-get update && \
    apt-get install time -y --no-install-recommends && \
    rm -rf /var/lib/apt/lists/* /tmp/*

RUN groupadd codegeet && \
    useradd -m -d /home/codegeet -g codegeet -s /bin/bash codegeet

ADD https://github.com/codegeet/platform/releases/download/0.1.0-SNAPSHOT/coderunner.jar /home/codegeet

RUN chown codegeet:codegeet /home/codegeet/coderunner.jar && \
    chmod +x /home/codegeet/coderunner.jar

USER codegeet
WORKDIR /home/codegeet

ENTRYPOINT ["java", "-jar", "coderunner.jar"]
