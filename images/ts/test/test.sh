#!/bin/bash

echo '{
  "code": "console.log(\"Hello, CodeGeet!\");",
  "args": [],
  "file_name": "app.ts",
  "instructions": {
    "build": "tsc app.ts",
    "exec": "node app.js"
  }
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/ts:latest
