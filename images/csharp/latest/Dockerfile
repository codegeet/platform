#FROM mono:latest

FROM debian:bookworm-slim

MAINTAINER Vladimir Prudnikov <codegeet@gmail.com>

#<install mono>
ENV MONO_VERSION 6.12.0.182

RUN apt-get update \
  && apt-get install -y --no-install-recommends gnupg dirmngr ca-certificates \
  && rm -rf /var/lib/apt/lists/* \
  && export GNUPGHOME="$(mktemp -d)" \
  && gpg --batch --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 3FA7E0328081BFF6A14DA29AA6A19B38D3D831EF \
  && gpg --batch --export --armor 3FA7E0328081BFF6A14DA29AA6A19B38D3D831EF > /etc/apt/trusted.gpg.d/mono.gpg.asc \
  && gpgconf --kill all \
  && rm -rf "$GNUPGHOME" \
  && apt-key list | grep Xamarin \
  && apt-get purge -y --auto-remove gnupg dirmngr

RUN echo "deb https://download.mono-project.com/repo/debian stable-buster/snapshots/$MONO_VERSION main" > /etc/apt/sources.list.d/mono-official-stable.list \
  && apt-get update \
  && apt-get install -y mono-runtime

RUN apt-get update \
  && apt-get install -y binutils curl mono-devel ca-certificates-mono fsharp mono-vbnc nuget referenceassemblies-pcl \
  && rm -rf /var/lib/apt/lists/* /tmp/*
#</install mono>

RUN set -xe \
    && apt-get update \
    && apt-get install time -y --no-install-recommends \
    && apt-get install openjdk-17-jdk -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/* /tmp/*

RUN groupadd codegeet
RUN useradd -m -d /home/codegeet -g codegeet -s /bin/bash codegeet

ADD https://github.com/codegeet/platform/releases/download/0.1.0-SNAPSHOT/coderunner.jar /home/codegeet
RUN chown codegeet:codegeet /home/codegeet/coderunner.jar
RUN chmod +x /home/codegeet/coderunner.jar

USER codegeet
WORKDIR /home/codegeet

ENTRYPOINT ["java", "-jar", "coderunner.jar"]
