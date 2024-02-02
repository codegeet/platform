#!/bin/bash

echo '{
  "code": "print(f\"Hello, {input()}!\")",
  "file_name": "app.py",
  "instructions": {
    "compile": "",
    "exec": "python app.py"
  },
  "executions": [
    {
      "args": "",
      "std_in": "CodeGeet"
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/python:latest
