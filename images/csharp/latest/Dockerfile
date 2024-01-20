FROM mono:latest

MAINTAINER Vladimir Prudnikov "codegeet@gmail.com"

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
