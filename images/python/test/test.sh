#!/bin/bash

 echo '{
  "code": "print(\"Hello, CodeGeet!\")",
  "args": [],
  "file_name": "app.py",
  "instructions": {
    "build": "",
    "exec": "python app.py"
  }
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/python:latest
