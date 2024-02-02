#!/bin/bash

echo '{
  "code": "console.log(`Hello, ${process.argv[2]}!`);",
  "file_name": "app.js",
  "instructions": {
    "exec": "node app.js"
  },
  "executions": [
    {
      "args": "CodeGeet",
      "std_in": ""
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/js:latest
