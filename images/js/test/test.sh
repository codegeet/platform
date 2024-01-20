#!/bin/bash

echo '{
  "code": "console.log(\"Hello, CodeGeet!\");",
  "args": [],
  "file_name": "app.js",
  "instructions": {
    "build": "",
    "exec": "node app.js"
  }
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/js:latest