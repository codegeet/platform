#!/bin/bash

echo '{
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello, CodeGeet!\"); }}",
  "args": [],
  "file_name": "Main.java",
  "instructions": {
    "compile": "javac Main.java",
    "exec": "java Main"
  }
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/java:latest
