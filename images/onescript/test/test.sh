#!/bin/bash

echo '{
  "code": "Message(\"Hello, \" + CommandLineArguments.Get(0) + \"!\");",
  "language": "onescript",
  "invocations": [
    {
      "args": ["CodeGeet"],
      "std_in": ""
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/onescript:latest
