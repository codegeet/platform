FROM openjdk:21-slim

MAINTAINER Vladimir Prudnikov "codegeet@gmail.com"

# Install Kotlin

ENV KOTLIN_VERSION=1.9.22
ENV KOTLIN_HOME=/opt/kotlin

RUN set -xe && \
    apt-get update &&  \
    apt-get install -y unzip wget --no-install-recommends

RUN wget https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip -O kotlin-compiler.zip && \
    unzip kotlin-compiler.zip -d /opt && \
    rm kotlin-compiler.zip && \
    mv /opt/kotlinc /opt/kotlin

ENV PATH="${KOTLIN_HOME}/bin:${PATH}"

# Verify Kotlin installation
RUN kotlin -version

# Configure Codegeet

RUN groupadd codegeet
RUN useradd -m -d /home/codegeet -g codegeet -s /bin/rbash codegeet

RUN set -xe && \
    apt-get update && \
    apt-get install jq -y --no-install-recommends

ADD https://github.com/codegeet/codegeet/releases/download/0.1.0-SNAPSHOT/coderunner.sh /home/codegeet
RUN chown codegeet:codegeet /home/codegeet/coderunner.sh
RUN chmod +x /home/codegeet/coderunner.sh

USER codegeet
WORKDIR /home/codegeet

CMD ["/home/codegeet/coderunner.sh"]
