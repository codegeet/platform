#!/bin/bash

echo '{
  "code": "using System; class HelloWorld { static void Main() { Console.WriteLine(\"Hello, World!\"); } }",
  "args": [],
  "file_name": "hw.cs",
  "instructions": {
    "build": "mcs -out:hw.exe hw.cs",
    "exec": "mono hw.exe"
  }
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/csharp:latest
