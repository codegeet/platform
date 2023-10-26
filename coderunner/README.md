# coderunner

## Overview
**coderunner** is a command-line tool that reads code in JSON format from the standard input (stdin),
compiles and executes the code, and then writes the result in JSON format to the standard output (stdout).
At present, **coderunner** shares many similarities with [glot](https://github.com/glotcode/code-runner).

## Supported languages
- JAVA
- ... more to come ;)

## Input (stdin)
```json
{
  "language_id": "java",
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

## Special Thanks
Special thanks to similar open-source projects:
- [glot](https://github.com/glotcode)
- [Judge0](https://github.com/judge0)