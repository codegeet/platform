#!/bin/bash

echo '{
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello, \" + args[0] + \"!\"); }}",
  "language": "java",
  "invocations": [
    {
      "args": ["CodeGeet"]
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/java:latest
