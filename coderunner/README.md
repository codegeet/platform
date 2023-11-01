# coderunner

## Overview
**coderunner** is a command-line tool that reads code in JSON format from the standard input (stdin),
compiles and executes the code, and then writes the result in JSON format to the standard output (stdout).
At present, **coderunner** shares many similarities with [glot](https://github.com/glotcode/code-runner).

## Supported languages
- JAVA
- PYTHON
- ... more to come ;)

## Input (stdin)
```json
{
  "language_id": "python",
  "content": "class Main {    public static void main(String[] args) {    System.out.print(\"Hello World!!!\"); }   }"
}
```

## Output (stdout)
```json
{
    "std_out":"Hello World!!!",
    "std_err":"",
    "error":""
}
```