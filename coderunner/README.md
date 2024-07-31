# Code Runner

## Overview

**Coderunner** is a command-line tool that runs inside Codegeet Docker containers 
and used for compiling and running code. 
When the container starts, **coderunner** reads input from `stdin` in JSON format, 
compiles and executes the code, and outputs the results to `stdout`, also in JSON format.

### Json Input
```json
{
  "code": "print(f\"Hello, CodeGeet!\")",
  "language": "python"
}
```

### Json Output
```json
{
  "status": "SUCCESS",
  "invocations": [
    {
      "status": "SUCCESS",
      "std_out": "Hello, CodeGeet!\n"
    }
  ]
}
```

## Run locally (Linux and MacOS)
To execute code snippets using **coderunner** on your local machine, ensure you have installed the necessary dependencies for the language you want to run (e.g., install a Python interpreter for Python).  
Once dependencies are installed, you can try executing some code. For example, to run a Python snippet, use the following command:
```bash
echo '{
  "code": "print(f\"Hello, CodeGeet!\")",
  "language": "python"
}' | java -jar coderunner.jar
```
#### Result:
```json
{
  "status" : "SUCCESS",
  "invocations" : [
    {
      "status" : "SUCCESS",
      "std_out" : "Hello, CodeGeet!\n",
      "std_err" : ""
    }]
}
```
Your code can read from `std_in` and use command line `args`:

#### std_in:
```bash
echo '{
  "code": "print(f\"Hello, {input()}!\")",
  "language": "python",
  "invocations": [
    {
      "std_in": "CodeGeet"
    }
  ]
}' | ./coderunner.sh
```
#### args:
```bash
echo '{
  "code": "import sys; print(f\"Hello, {sys.argv[1]}!\")",
  "language": "python",
  "invocations": [
    {
      "args": ["CodeGeet"]
    }
  ]
}' | ./coderunner.sh
````

### Compilation and multiple executions
When code compilation is needed, **coderunner** will compile the code with the provided 'compile' instruction 
(e.g., `compile: javac Main.java`) and execute it with 'exec' (e.g., `exec: java Main`).
Additionally, you can have multiple executions of compiled code:
#### Example:
```bash
echo '{
  "code": "class Main { public static void main(String[] args) { System.out.print(args[0]); }}",
  "language": "java",
  "invocations": [
    {
      "args": ["one"]
    },
    {
      "args": ["another"]
    }
  ]
}' | ./coderunner.sh
````
#### Output
```json:
{
  "status" : "SUCCESS",
  "invocations" : [ {
    "status" : "SUCCESS",
    "std_out" : "one",
    "std_err" : ""
  }, {
    "status" : "SUCCESS",
    "std_out" : "another",
    "std_err" : ""
  } ]
}
````
### Statistics
If you have `/usr/bin/time` installed on Linux or `gtime` on Mac, **coderunner** will return memory statistics for the execution:
```json:
{
  "status" : "SUCCESS",
  "compilation" : {
    "details" : {
      "runtime" : 251,
      "memory" : 78672
    }
  },
  "invocations" : [ {
    "status" : "SUCCESS",
    "details" : {
      "runtime" : 48,
      "memory" : 34880
    },
    "std_out" : "one",
    "std_err" : ""
  }, {
    "status" : "SUCCESS",
    "details" : {
      "runtime" : 48,
      "memory" : 35040
    },
    "std_out" : "another",
    "std_err" : ""
  } ]
}
````

You can install `/usr/bin/time` or `gtime` by running:

```bash
brew install gnu-time
````
```bash
sudo apt install time
````
