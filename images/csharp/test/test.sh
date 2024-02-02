#!/bin/bash

echo '{
  "code": "using System; class HelloWorld { static void Main(string[] args) { Console.WriteLine(\"Hello, \" + args[0]); } }",
  "file_name": "app.cs",
  "instructions": {
    "compile": "mcs -out:app.exe app.cs",
    "exec": "mono app.exe"
  },
  "executions": [
    {
      "args": ["CodeGeet"]
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/csharp:latest
