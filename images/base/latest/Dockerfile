FROM openjdk:21-jdk-slim

MAINTAINER Vladimir Prudnikov "codegeet@gmail.com"

# Add codegeet user
RUN groupadd codegeet
RUN useradd -m -d /home/codegeet -g codegeet -s /bin/rbash codegeet

# Install coderunner
# ADD https://github.com/codegeet/codegeet/releases/download/v0.1.0-beta/coderunner.jar /home/codegeet

COPY coderunner.jar /home/codegeet

RUN chown codegeet:codegeet /home/codegeet/coderunner.jar
RUN chmod +x /home/codegeet/coderunner.jar

USER codegeet

ENTRYPOINT ["java", "-jar", "coderunner.jar"]
