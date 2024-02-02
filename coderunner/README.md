# Code Runner

## Overview

**Coderunner** is a command-line tool that runs inside every Codegeet Docker container 
and used for compiling and running code. 
When the container starts, **coderunner** reads input from `stdin` in JSON format, 
compiles and executes the code, and outputs the results to `stdout`, also in JSON format.

### Coderunner Input
```json
{
  "code": "print(f\"Hello, CodeGeet!\")",
  "file_name": "app.py",
  "instructions": {
    "exec": "python3 app.py"
  }
}
```

### Coderunner Output
```json
{
  "executions": [
    {
      "std_out": "Hello, CodeGeet!",
      "std_err": "",
      "exec_code": 0
    }
  ],
  "error": "",
  "exec_code": 0
}
```

## Play locally (Linux and MacOS)
To execute code snippets with **coderunner** on your local machine, 
you need to have the tools installed for your programming language, 
such as JDK for Java, Python interpreter for Python, or others as required.  
To work with JSON **coderunner** uses `jq`, a lightweight and powerful command-line JSON processor.
You need to install `jq` using the package manager.
#### MacOS:
```bash
brew install jq
```
#### Linux:
```bash
sudo apt-get install jq
```
Now you can try to execute some code (please pay attention if you have `python` or `python3`):
```bash
echo '{
  "code": "print(f\"Hello, CodeGeet!\")",
  "file_name": "app.py",
  "instructions": {
    "exec": "python3 app.py"
  }
}' | ./coderunner.sh
```
#### Result:
```json
{
  "executions": [
    {
      "std_out": "Hello, CodeGeet!"
    }
  ]
}
```
With **coderunner** your code can read from `std_in` and use command line `args`:

#### std_in:
```bash
echo '{
  "code": "print(f\"Hello, {input()}!\")",
  "file_name": "app.py",
  "instructions": {
    "exec": "python3 app.py"
  },
  "executions": [
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
  "file_name": "app.py",
  "instructions": {
    "exec": "python3 app.py"
  },
  "executions": [
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
  "file_name": "Main.java",
  "instructions": {
    "compile": "javac Main.java",
    "exec": "java Main"
  },
  "executions": [
    {
      "args": "one"
    },
    {
      "args": "another"
    }
  ]
}' | ./coderunner.sh
````
#### Output
```json:
{
  "executions": [
    {
      "std_out": "one",
      "std_err": "",
      "exec_code": 0
    },
    {
      "std_out": "another",
      "std_err": "",
      "exec_code": 0
    }
  ],
  "error": "",
  "exec_code": 0
}
````

### Author

As the author was not much familiar with Bash scripting, so **coderunner** has been written by ChatGPT.  

#### Prompt: 
```txt
Please help me write a shell script 
that takes JSON as input (example provided), 
saves the "code" to a file, and then runs 
`instructions.compile` and `instructions.exec`. 

I want it to accept input from stdin 
and return the output (example provided).
Also, please include a measurement 
of how much time it took to execute the code 
and return this information in the JSON output.
```
