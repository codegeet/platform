FROM openjdk:21-slim

MAINTAINER Vladimir Prudnikov "codegeet@gmail.com"

RUN groupadd codegeet
RUN useradd -m -d /home/codegeet -g codegeet -s /bin/bash codegeet

RUN set -xe && \
    apt-get update && \
    apt-get install time -y --no-install-recommends

ADD https://github.com/codegeet/platform/releases/download/0.1.0-SNAPSHOT/coderunner.jar /home/codegeet
RUN chown codegeet:codegeet /home/codegeet/coderunner.jar
RUN chmod +x /home/codegeet/coderunner.jar

WORKDIR /home/codegeet

ENTRYPOINT ["java", "-jar", "coderunner.jar"]
CMD ["java", "-jar", "coderunner.jar"]
