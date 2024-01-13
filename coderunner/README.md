# coderunner

## Overview
**Coderunner** is a command-line tool that reads input in JSON format from the standard input (stdin),
compiles and executes the code, and writes the result in JSON format to the standard output (stdout).

## Input (stdin)
```json
{
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello, \" + args[0] + \"!\"); }}",
  "args": ["you"],
  "fileName": "Main.java",
  "instructions": {
    "build": "javac Main.java",
    "exec": "java Main"
  }
}
```

## Output (stdout)
```json
{
  "std_out": "Hello, you!",
  "std_err": "",
  "error": "",
  "execCode": 0,
  "executionMillis": 35
}
```

## Play locally
Install java on your machine.  
Download `coderunner.jar` from [releases](https://github.com/codegeet/codegeet/releases)  

Execute:
```bash
echo '{                            
  "code": "class Main { public static void main(String[] args) { System.out.print(\"Hello, \" + args[0] + \"!\"); }}",
  "args": ["you"],
  "fileName": "Main.java",
  "instructions": {
    "build": "javac Main.java",
    "exec": "java Main"
  }
}' | java -jar coderunner.jar
```

Output:
```bash
{
  "std_out": "Hello, you!",
  "std_err": "",
  "error": "",
  "execCode": 0,
  "executionMillis": 35
}
```
