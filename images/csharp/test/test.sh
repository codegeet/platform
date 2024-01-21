#!/bin/bash

echo '{
  "code": "using System; class HelloWorld { static void Main() { Console.WriteLine(\"Hello, CodeGeet!\"); } }",
  "args": [],
  "file_name": "app.cs",
  "instructions": {
    "compile": "mcs -out:app.exe app.cs",
    "exec": "mono app.exe"
  }
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/csharp:latest
