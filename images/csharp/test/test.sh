#!/bin/bash

echo '{
  "code": "using System; class HelloWorld { static void Main(string[] args) { Console.WriteLine(\"Hello, \" + args[0]); } }",
  "language": "csharp",
  "invocations": [
    {
      "args": ["CodeGeet"]
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/csharp:latest
