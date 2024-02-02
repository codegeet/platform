#!/bin/bash

echo '{
  "code": "console.log(`Hello, ${process.argv[2]}!`);",
  "file_name": "app.ts",
  "instructions": {
    "compile": "tsc app.ts",
    "exec": "node app.js"
  },
  "executions": [
    {
      "args": "CodeGeet",
      "std_in": ""
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/ts:latest
