# coderunner

## Overview

**Coderunner** is a command-line tool that runs inside a Docker container. 
It's used for compiling and running code. 
When the container starts, **coderunner** reads input from `stdin` in JSON format. 
It compiles and executes the code, and outputs the results to `stdout`, also in JSON format.

### Coderunner Input
```json
{
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello, \" + args[0] + \"!\"); }}",
  "args": ["you"],
  "file_name": "Main.java",
  "instructions": {
    "compile": "javac Main.java",
    "exec": "java Main"
  }
}
```

### Coderunner Output
```json
{
  "std_out": "Hello, you!",
  "std_err": "",
  "error": "",
  "exec_code": 0,
  "exec_time": 35
}
```

## Play locally (Linux and MacOS)
To use **coderunner** locally and run code snippets on your local machine, 
you also need to have the necessary tools installed for your programming language, 
such as JDK for Java, Python interpreter for Python, or others as required.  
To work with JSON **coderunner** uses `jq`, a lightweight and powerful command-line JSON processor.
You can install `jq` using the package manager.
#### MacOS:
```bash
brew install jq
```
#### Linux:
```bash
sudo apt-get install jq
```

#### Execute:
```bash
echo '{                            
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello, \" + args[0] + \"!\"); }}",
  "args": ["you"],
  "file_name": "Main.java",
  "instructions": {
    "compile": "javac Main.java",
    "exec": "java Main"
  }
}' | ./coderunner.sh
```

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
