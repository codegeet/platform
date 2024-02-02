#!/bin/bash

echo '{
  "code": "fun main() { println(\"Hello, ${readLine()}!\") }",
  "file_name": "app.kt",
  "instructions": {
    "compile": "kotlinc app.kt -include-runtime -d app.jar",
    "exec": "java -jar app.jar"
  },
  "executions": [
    {
      "args": "",
      "std_in": "CodeGeet"
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/kotlin:latest
