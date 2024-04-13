# Images

## Overview
**CodeGeet** executes code within Docker containers, using a separate Docker image for each supported language. 
Typically, each docker image is based on a standard base image for the respective language 
and includes coderunner for compiling and executing code.

### Pull Docker image:

```bash
docker pull codegeet/python:latest
``` 
or
```bash
docker pull codegeet/java:latest
```

### Run Docker container:

#### Python
```bash
echo '{
  "code": "print(f\"Hello, {input()}!\")",
  "language": "python",
  "invocations": [
    {
      "args": [""],
      "std_in": "CodeGeet"
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/python:latest
```

#### Java
```bash
echo '{
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello, \" + args[0] + \"!\"); }}",
  "language": "java",
  "invocations": [
    {
      "args": ["CodeGeet"]
    }
  ]
}' | docker run --rm -i -u codegeet -w /home/codegeet codegeet/java:latest
```

#### Output
```json
{
  "status" : "SUCCESS",
  "compilation" : {
    "details" : {
      "runtime" : 354,
      "memory" : 87340
    }
  },
  "invocations" : [ {
    "status" : "SUCCESS",
    "details" : {
      "runtime" : 31,
      "memory" : 45992
    },
    "std_out" : "Hello, CodeGeet!",
    "std_err" : ""
  } ]
}
```
or
```json
{
  "status" : "INVOCATION_ERROR",
  "compilation" : {
    "details" : {
      "runtime" : 282,
      "memory" : 96196
    }
  },
  "invocations" : [ {
    "status" : "INVOCATION_ERROR",
    "details" : {
      "runtime" : 23,
      "memory" : 45480
    },
    "std_out" : "",
    "std_err" : "Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException: Index 2 out of bounds for length 1\n\tat Main.main(Main.java:1)\nCommand exited with non-zero status 1\n"
  } ]
}
```