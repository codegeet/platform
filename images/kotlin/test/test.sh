#!/bin/bash

echo '{
  "code": "fun main() { println(\"Hello, ${readLine()}!\") }",
  "language": "kotlin",
  "invocations": [
    {
      "std_in": "CodeGeet"
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/kotlin:latest
