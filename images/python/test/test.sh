#!/bin/bash

echo '{
  "code": "print(f\"Hello, {input()}!\")",
  "language": "python",
  "invocations": [
    {
      "args": [""],
      "std_in": "CodeGeet"
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/python:latest
