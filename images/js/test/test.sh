#!/bin/bash

echo '{
  "code": "console.log(`Hello, ${process.argv[2]}!`);",
  "language": "js",
  "invocations": [
    {
      "args": ["CodeGeet"]
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/js:latest
